package cloud.benchflow.datamanager.core.datarepository.cassandra

import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

import org.slf4j.LoggerFactory

import com.datastax.driver.core.{ Cluster, PreparedStatement }

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.{ Attributes, Materializer }
import akka.stream.alpakka.cassandra.scaladsl.{ CassandraSink, CassandraSource }
import akka.stream.scaladsl.Source

trait Cassandra {
  val cluster: Cluster
  val keyspace: String
  val fetchSize = 20

  val logger = LoggerFactory.getLogger(this.getClass())

  implicit lazy val session = cluster.connect(keyspace)
  implicit val system: ActorSystem // = ActorSystem()
  implicit val mat: Materializer // = ActorMaterializer()
  val queryStore = scala.collection.mutable.Map.empty[String, Try[PreparedStatement]]

  type Json = String

  def tables: Iterable[String] =
    cluster.getMetadata.getKeyspace(keyspace).getTables.map(_.getName)

  def getJsonStream(table: String, experimentId: String): Option[Source[Json, NotUsed]] = {
    val query = s"SELECT JSON * FROM $table WHERE experiment_id = ?"
    val preparedQuery = queryStore.getOrElseUpdate(
      table,
      Try(session.prepare(query)))
    val result = preparedQuery match {
      case Success(preparedQuery) =>
        val stmt = preparedQuery
          .bind(experimentId)
          .setFetchSize(fetchSize)

        /*
         * from: http://www.datastax.com/dev/blog/whats-new-in-cassandra-2-2-json-support
         * "The results for SELECT JSON will only include a single column named [json]."
         */
        val stream =
          CassandraSource(stmt)
            .map(_.getString("[json]"))
            .log("cassandra-source")
            .withAttributes(Attributes.logLevels(onElement = Logging.InfoLevel))
        Success(stream)
      case Failure(error) =>
        logger.warn(s"Error in query: [$query] caused by [$error] Skipping it.")
        Failure(error)
    }
    result.toOption
  }

  def putJsonStream(table: String, stream: Source[Json, NotUsed]): Future[Done] = {
    val preparedStatement = session.prepare(s"INSERT INTO $table JSON ?")
    val statementBinder = (json: Json, statement: PreparedStatement) => statement.bind(json)
    val sink = CassandraSink[String](parallelism = 2, preparedStatement, statementBinder)
    stream
      .log("cassandra-sink")
      .withAttributes(Attributes.logLevels(onElement = Logging.InfoLevel))
      .runWith(sink)
  }
}
