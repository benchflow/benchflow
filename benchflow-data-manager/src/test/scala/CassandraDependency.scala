import org.scalatest.{ BeforeAndAfterAll, Suite }
import org.slf4j.LoggerFactory

import com.datastax.driver.core.{ Cluster, Session }
import com.palantir.docker.compose.DockerComposeRule
import com.palantir.docker.compose.connection.waiting.HealthChecks

object DockerDependencies {
  val logger = LoggerFactory.getLogger(this.getClass())

  val dockerComposeFile = "src/test/resources/docker-compose.yml"
  val cassandraAddress = "127.0.0.1"
  val cassandraPort = 9042
  val serviceName = "cassandra"
  val username = "cassandra"
  val password = "cassandra"

  val docker = {
    val docker = DockerComposeRule.builder
      .file(dockerComposeFile)
      .waitingForService(serviceName, HealthChecks.toHaveAllPortsOpen())
      .build
    docker.before // start container
    docker
  }

  val maxAttempts = 10
  val waitInterval = 10000
  val (cluster, session) = tryConnect(maxAttempts, waitInterval)

  def buildCluster: Cluster =
    Cluster
      .builder
      .addContactPoint(cassandraAddress)
      .withPort(cassandraPort)
      .withCredentials(
        username,
        password)
      .build

  def tryConnect(maxRetries: Int, interval: Int): (Cluster, Session) = {
    try {
      logger.info(s"Trying to connect to Cassandra...")
      val cluster = buildCluster
      (cluster, cluster.connect)
    } catch {
      case (e: Exception) =>
        logger.info(s"Cassandra is not ready yet: [${e.getMessage}].")
        logger.info(s"Exponential backoff... Waiting ${interval}ms to try again...")
        Thread.sleep(interval);
        if (maxRetries > 0) {
          tryConnect(maxRetries - 1, interval * 2)
        } else {
          throw new Exception(s"Giving up: $e")
        }
    }
  }
}

trait CassandraDependency
    extends BeforeAndAfterAll {

  this: Suite =>

  val cluster = DockerDependencies.cluster
  implicit val session = DockerDependencies.session

  override def afterAll(): Unit = {
    super.afterAll()
    session.execute("DROP TABLE IF EXISTS test.test;")
    session.execute("DROP KEYSPACE IF EXISTS test;")

    // stop container
    DockerDependencies.docker.after
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
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

}
