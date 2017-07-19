package cloud.benchflow.datamanager.core.datarepository.cassandra

import java.net.InetAddress

import scala.collection.JavaConversions.{ asScalaBuffer, bufferAsJavaList }

import com.datastax.driver.core.Cluster
import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.stream.Materializer

class CassandraFromConfig(implicit val system: ActorSystem, val mat: Materializer) extends Cassandra {
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
        password)
      .build
  override lazy val keyspace = configuration.getString("cassandra.keyspace")
}

class CassandraImpl(
    val host: String,
    val username: String,
    val password: String,
    val keyspace: String, implicit val system: ActorSystem, val mat: Materializer) extends Cassandra {
  override val cluster =
    Cluster
      .builder
      .addContactPoint(host)
      .withCredentials(
        username,
        password)
      .build
}
