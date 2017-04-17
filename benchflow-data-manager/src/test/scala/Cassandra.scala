import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers, Suite }

import com.datastax.driver.core.{ Cluster, PreparedStatement, SimpleStatement }

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.{ CassandraSink, CassandraSource }
import akka.stream.scaladsl.{ Sink, Source }

/**
 * All the tests must be run with a local Cassandra running on default port 9042.
 */
trait CassandraTest
    extends Suite
    with BeforeAndAfterEach
    with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  val cluster = Cluster.builder.addContactPoint("127.0.0.1").withPort(9042).build
  implicit val session = cluster.connect()

  override def beforeEach(): Unit = {
    session.execute(
      """
        |CREATE KEYSPACE IF NOT EXISTS test WITH replication = {
        |  'class': 'SimpleStrategy',
        |  'replication_factor': '1'
        |};
      """.stripMargin)
    session.execute(
      """
        |CREATE TABLE IF NOT EXISTS test.test (
        |    experiment_id text PRIMARY KEY
        |);
      """.stripMargin)
  }

  override def afterEach(): Unit = {
    session.execute("DROP TABLE IF EXISTS test.test;")
    session.execute("DROP KEYSPACE IF EXISTS test;")
  }

  override def afterAll(): Unit =
    Await.result(system.terminate(), 5.seconds)

  def populate() =
    (1 until 103).map { i =>
      session.execute(s"INSERT INTO test.test(experiment_id) VALUES ('$i')")
      i
    }

}

class LowerCassandra extends FlatSpec
    with Matchers
    with CassandraTest {

  "Lower Cassandra" should "insert and retrieve data as json" in {
    val data = List(
      """{"experiment_id": "my_id1"}""",
      """{"experiment_id": "my_id2"}""" // """{"experiment_id": "my_id1", "source_process_instance_id": "3ff77405-ed01-11e5-beb9-7a98732174cb", "duration": 1, "end_time": "1970-01-01 00:00:00.000Z", "process_definition_id": "invoice-collaboration:1:771d26cd-ed00-11e5-beb9-7a98732174cb", "process_name": "invoice-collaboration", "start_time": "1970-01-01 00:00:00.000Z", "to_ignore": false, "trial_id": "camundaZZZZZZMV_ZZZZZZOX"}""",
      // """{"experiment_id": "my_id2", "source_process_instance_id": "3ff77405-ed01-11e5-beb9-7a98732174cb", "duration": 1, "end_time": "1970-01-01 00:00:00.000Z", "process_definition_id": "invoice-collaboration:1:771d26cd-ed00-11e5-beb9-7a98732174cb", "process_name": "invoice-collaboration", "start_time": "1970-01-01 00:00:00.000Z", "to_ignore": false, "trial_id": "camundaZZZZZZMV_ZZZZZZOX"}"""
      )
    val source = Source(data)
    val preparedStatement = session.prepare("INSERT INTO test.test JSON ?")
    val statementBinder = (json: String, statement: PreparedStatement) => statement.bind(json)
    val sink = CassandraSink[String](parallelism = 2, preparedStatement, statementBinder)
    val future = source.runWith(sink)
    val result = Await.result(future, 3.seconds)
    val stmt =
      new SimpleStatement(s"SELECT JSON * FROM test.test")
        .setFetchSize(20)
    // val stmt = session
    // .prepare(s"SELECT JSON * FROM test.test WHERE experiment_id = ?")
    // .bind("1")
    // .setFetchSize(20)

    val r = CassandraSource(stmt).runWith(Sink.seq)
    val retrieved = Await.result(r, 3.seconds)
    retrieved.map(_.getString("[json]")) should contain theSameElementsAs data
  }

}
