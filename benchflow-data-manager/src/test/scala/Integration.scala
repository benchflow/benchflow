import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream }
import java.util.Date
import java.util.concurrent.atomic.AtomicLong

import scala.collection.mutable.ArrayBuffer

import org.scalatest.{ Matchers, WordSpecLike }

import com.datastax.driver.core.Cluster

import akka.stream.ActorMaterializer
import akka.testkit.TestKit

import cloud.benchflow.datamanager.core.BackupManager
import cloud.benchflow.datamanager.core.backupstorage.{ BackupFile, BackupStorage }
import cloud.benchflow.datamanager.core.datarepository.cassandra.{ Cassandra, CassandraFromConfig }
import cloud.benchflow.datamanager.core.datarepository.objectstorage.{ ExperimentObjectStorage, ObjectStat }
import cloud.benchflow.datamanager.service.resources.RootResource
import cloud.benchflow.datamanager.service.api.Job

class IntegrationTests
    extends WordSpecLike
    with Matchers
    with CassandraTest {

  override def afterAll: Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }

  object dbProvider extends Cassandra {
    val cluster = IntegrationTests.this.cluster
    val keyspace = "test"
    lazy val mat = IntegrationTests.this.mat
    lazy val system = IntegrationTests.this.system
  }

  object TestBackupStorage extends BackupStorage {
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

  object TestObjectStorage extends ExperimentObjectStorage {
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

  val backupManager = new BackupManager(dbProvider, TestBackupStorage, TestObjectStorage)

  "Application" should {
    val application = new RootResource(backupManager)

    "backup experiment data" in {
      val experimentId = "1"

      val data = populate

      val content = "some text"
      TestObjectStorage.putObject("test", "1-something", new ByteArrayInputStream(content.getBytes), content.length, "plain/text")

      /*
       * This call triggers asynchronous actions via the use of actors.
       * Be careful when writing assertions associated with the result of this call.
       */
      val result =
        application.backup(experimentId)

      result.getId should be(0)
      result.getBackupId.get should be(0)

      TestBackupStorage
        .listFiles(0, "minio")
        .flatMap(_.headOption.map(_.id))
        .map(id => {
          val out = new ByteArrayOutputStream
          TestBackupStorage.downloadFile(id, out)
          new String(out.toByteArray) should be {
            content
          }
        })
    }
  }
}
