package cloud.benchflow.dsl.deployment.docker.compose

import cloud.benchflow.dsl.deployment.docker.service._
import org.scalatest.{FlatSpec, Matchers}

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 18/07/16.
 */
class ComposeSpec extends FlatSpec with Matchers {

  import net.jcazevedo.moultingyaml._
  import DockerComposeYamlProtocol._
  import scala.collection.mutable.{Map => MutableMap}

  "Compose File" should "parse correctly" in {

    val dockerCompose =
      """
        |version: '2'
        |services:
        |  camunda:
        |    image: camunda_image
        |    mem_limit: 5g
        |    environment:
        |      - VAR=5
        |  other.service:
        |     image: other.image
        |networks:
        |  foo:
        |    driver: custom
      """.stripMargin.parseYaml.convertTo[DockerCompose]

    val parsedDockerCompose = DockerCompose(
      version = "2",
      networks = Some(
        Networks(
          Map("foo" -> NetworkConfig("custom"))
        )
      ),
      services = Map(
        "camunda" -> Service(
          name = "camunda",
          image = Some(Image("camunda_image")),
          memLimit = Some(MemLimit(5, GigaByte)),
          environment = Environment(
            MutableMap(
              "VAR" -> "5"
            )
          )
        ),
        "other.service" -> Service(
          name = "other.service",
          image = Some(Image("other.image")),
          environment = Environment(MutableMap.empty)
        )
      )
    )

    println(parsedDockerCompose.toYaml.prettyPrint)

    dockerCompose should be(parsedDockerCompose)
  }

}
