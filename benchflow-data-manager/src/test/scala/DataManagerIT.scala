import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream }
import java.util.Date
import java.util.concurrent.atomic.AtomicLong

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

import com.datastax.driver.core.{ PreparedStatement, SimpleStatement }

import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, Materializer }
import akka.stream.alpakka.cassandra.scaladsl.{ CassandraSink, CassandraSource }
import akka.stream.scaladsl.{ Sink, Source }
import akka.testkit.TestKit

import cloud.benchflow.datamanager.core.BackupManager
import cloud.benchflow.datamanager.core.backupstorage.{ BackupFile, BackupStorage }
import cloud.benchflow.datamanager.core.datarepository.cassandra.Cassandra
import cloud.benchflow.datamanager.core.datarepository.objectstorage.{ ExperimentObjectStorage, ObjectStat }
import cloud.benchflow.datamanager.service.DataManagerApplication
import cloud.benchflow.datamanager.service.configurations.DataManagerConfiguration
import cloud.benchflow.datamanager.service.configurations.factory.{ CassandraServiceFactory, GoogleDriveServiceFactory, MinioServiceFactory }
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.setup.Environment
import io.dropwizard.testing.DropwizardTestSupport
import io.dropwizard.testing.DropwizardTestSupport.ServiceListener
import io.dropwizard.testing.ResourceHelpers
import javax.ws.rs.client.Client
import javax.ws.rs.core.Response

class DataManagerIT
    extends WordSpecLike
    with Matchers
    with CassandraDependency
    with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  override def beforeAll: Unit = {
    super.beforeAll()
    dropwizardSupport.before
  }

  override def afterAll: Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
    dropwizardSupport.after
  }

  lazy val dropwizardSupport: DropwizardTestSupport[DataManagerConfiguration] = {
    val application = new DropwizardTestSupport[DataManagerConfiguration](
      classOf[DataManagerApplication],
      ResourceHelpers.resourceFilePath("configuration.yml"))

    /*
     * Injecting mocked dependencies
     */
    application.addListener(new ServiceListener[DataManagerConfiguration]() {
      override def onRun(configuration: DataManagerConfiguration, environment: Environment, rule: DropwizardTestSupport[DataManagerConfiguration]) = {
        configuration.setMinioServiceFactory(new MinioServiceFactory {
          override def build = ObjectStorageMock
        })
        configuration.setGoogleDriveServiceFactory(new GoogleDriveServiceFactory {
          override def build = BackupStorageMock
        })
        configuration.setCassandraServiceFactory(new CassandraServiceFactory {
          override def build(system: ActorSystem, mat: Materializer) = dbProvider
        })
      }
    })
    application
  }

  object dbProvider extends Cassandra {
    val cluster = DataManagerIT.this.cluster
    val keyspace = "test"
    lazy val mat = DataManagerIT.this.mat
    lazy val system = DataManagerIT.this.system
  }

  object BackupStorageMock extends BackupStorage {
    val backupIdCounter = new AtomicLong
    val fileIdCounter = new AtomicLong

    val storage = scala.collection.mutable.Map.empty[(Long, String), ArrayBuffer[BackupFile]]
    val files = scala.collection.mutable.Map.empty[String, Array[Byte]]

    def listFiles(backupId: Long, serviceName: String, folderHierarchy: List[String]): Option[List[BackupFile]] =
      storage.get((backupId, serviceName)).map(_.toList)

    def downloadFile(fileId: String, out: OutputStream): Unit = {
      files.get(fileId).map(out.write(_))
      Unit
    }

    def uploadFile(
      backupId: Long,
      serviceName: String,
      folderHierarchy: List[String],
      input: InputStream,
      length: Long,
      fileName: String,
      contentType: String): Unit = {
      val id = fileIdCounter.getAndIncrement
      val file = BackupFile(id.toString, fileName, contentType)
      storage.getOrElseUpdate((backupId, serviceName), ArrayBuffer()) += file
      val buffer = new Array[Byte](length.toInt)
      input.read(buffer, 0, length.toInt)
      files(id.toString) = buffer
    }

    def nextId: Long = backupIdCounter.getAndIncrement
  }

  object ObjectStorageMock extends ExperimentObjectStorage {
    override val defaultBucket = "test"

    val storage = scala.collection.mutable.Map.empty[String, (ObjectStat, InputStream)]

    def getObjectStat(bucketName: String, objectName: String): Option[ObjectStat] =
      storage.get(objectName).map { case (stat, _) => stat }

    def getObject(bucketName: String, objectName: String): Option[(ObjectStat, InputStream)] =
      storage.get(objectName)

    def putObject(bucketName: String, objectName: String, stream: InputStream, size: Long, contentType: String): Option[Unit] = {
      storage(objectName) = (ObjectStat(bucketName, objectName, new Date, size, contentType), stream)
      Some(Unit)
    }

    def listObjects(bucketName: String, prefix: String, recursive: Boolean): List[ObjectStat] = {
      val startingWithPrefix = storage filterKeys (_.startsWith(prefix))
      val stats = startingWithPrefix.values.map { case (stat, _) => stat }
      stats.toList
    }
  }

  val backupManager = new BackupManager(dbProvider, BackupStorageMock, ObjectStorageMock)

  "Lower Cassandra" should {
    "insert and retrieve data as json" in {
      val data = List(
        """{"experiment_id": "my_id1"}""",
        """{"experiment_id": "my_id2"}""")
      val source = Source(data)
      val preparedStatement = session.prepare("INSERT INTO test.test JSON ?")
      val statementBinder = (json: String, statement: PreparedStatement) => statement.bind(json)
      val sink = CassandraSink[String](parallelism = 2, preparedStatement, statementBinder)
      val future = source.runWith(sink)
      val result = Await.result(future, 3.seconds)
      val fetchSize = 20
      val stmt =
        new SimpleStatement(s"SELECT JSON * FROM test.test")
          .setFetchSize(fetchSize)
      // val stmt = session
      // .prepare(s"SELECT JSON * FROM test.test WHERE experiment_id = ?")
      // .bind("1")
      // .setFetchSize(20)

      val r = CassandraSource(stmt).runWith(Sink.seq)
      val retrieved = Await.result(r, 3.seconds)
      retrieved.map(_.getString("[json]")) should contain theSameElementsAs data
    }
  }

  "BackupManager" should {

    "backup experiment data" in {
      val experimentId = "1"

      val content = "some text"
      ObjectStorageMock.putObject("test", "1-something", new ByteArrayInputStream(content.getBytes), content.length, "plain/text")

      /*
       * This call triggers asynchronous actions via the use of actors.
       * Be careful when writing assertions associated with the result of this call.
       */
      val result =
        backupManager.backupExperiment(experimentId);

      result._1 should be(0)
      result._2 should be(0)

      BackupStorageMock
        .listFiles(0, "minio")
        .flatMap(_.headOption.map(_.id))
        .map(id => {
          val out = new ByteArrayOutputStream
          BackupStorageMock.downloadFile(id, out)
          new String(out.toByteArray) should be {
            content
          }
        })
    }

    "start application and check nonexisting URL" in {
      val client: Client = new JerseyClientBuilder(dropwizardSupport.getEnvironment).build("test client")

      val address = s"http://localhost:${dropwizardSupport.getLocalPort}/nonexisting"
      val response: Response = client.target(address)
        .request
        .get

      val expectedHttpStatus = 404
      response.getStatus should be { expectedHttpStatus }
    }
  }
}
