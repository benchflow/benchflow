package datarepository.cassandra

import java.net.InetAddress

import scala.collection.JavaConversions.{ asScalaBuffer, bufferAsJavaList, collectionAsScalaIterable }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

import com.datastax.driver.core.{ Cluster, PreparedStatement }
import com.google.inject.ImplementedBy
import com.typesafe.config.ConfigFactory

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.cassandra.scaladsl.{ CassandraSink, CassandraSource }
import akka.stream.scaladsl.Source
import javax.inject.Inject
import play.api.Logger

class CassandraFromConfig @Inject() (implicit val system: ActorSystem, val mat: Materializer) extends Cassandra {
  lazy val configuration = ConfigFactory.load()
  lazy val hosts = configuration.getStringList("cassandra.host").map(InetAddress.getByName)
  lazy val username = configuration.getString("cassandra.username")
  lazy val password = configuration.getString("cassandra.password")
  override lazy val cluster =
    Cluster
      .builder
      .addContactPoints(hosts)
      .withCredentials(
        username,
        password
      )
      .build
  override lazy val keyspace = configuration.getString("cassandra.keyspace")
}

@ImplementedBy(classOf[CassandraFromConfig])
trait Cassandra {
  val cluster: Cluster
  val keyspace: String
  val fetchSize = 20

  val logger = Logger(this.getClass())

  implicit lazy val session = cluster.connect(keyspace)
  implicit val system: ActorSystem // = ActorSystem()
  implicit val mat: Materializer // = ActorMaterializer()
  val queryStore = scala.collection.mutable.Map.empty[String, Try[PreparedStatement]]

  type Json = String

  def tables: Iterable[String] =
    cluster.getMetadata.getKeyspace(keyspace).getTables.map(_.getName)

  def getJsonStream(table: String, experimentId: String): Option[Source[String, NotUsed]] = {
    val query = s"SELECT JSON * FROM $table WHERE experiment_id = ?"
    val preparedQuery = queryStore.getOrElseUpdate(
      table,
      Try(session.prepare(query))
    )
    val result = preparedQuery match {
      case Success(preparedQuery) =>
        val stmt = preparedQuery
          .bind(experimentId)
          .setFetchSize(fetchSize)

        /*
         * from: http://www.datastax.com/dev/blog/whats-new-in-cassandra-2-2-json-support
         * "The results for SELECT JSON will only include a single column named [json]."
         */
        Success(CassandraSource(stmt).map(_.getString("[json]")))
      case Failure(error) =>
        logger.warn(s"Error in query: $query\nSkipping it.\nError: $error")
        Failure(error)
    }
    result.toOption
  }

  def putJsonStream(table: String, stream: Source[Json, NotUsed]): Future[Done] = {
    val preparedStatement = session.prepare(s"INSERT INTO $table JSON ?")
    val statementBinder = (json: Json, statement: PreparedStatement) => statement.bind(json)
    val sink = CassandraSink[String](parallelism = 2, preparedStatement, statementBinder)
    stream.runWith(sink)
  }
}
