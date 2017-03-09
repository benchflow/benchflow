package cloud.benchflow.dsl.deployment.docker.service

import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 18/07/16.
  */
class ServiceSpec extends FlatSpec with Matchers {

  import cloud.benchflow.dsl.deployment.docker.service.ServiceYamlProtocol._
  import net.jcazevedo.moultingyaml._

  import scala.collection.mutable.{Map => MutableMap}

  "Docker Service" should "parse correctly" in {

    val service =
      """
        |camunda:
        |  image: camunda_image
        |  cpuset: 0,1,2,3
        |  mem_limit: 5g
        |  environment:
        |  - constraint:node==bull
        |  - VAR=5
        |  - OTHER_VAR=http://google.com
        |  volumes_from:
        |  - db:ro
        |  - other
        |  depends_on:
        |  - otherService
        |  pid: host
      """.stripMargin.parseYaml.convertTo[Service]

    val parsedService = Service(
      name = "camunda",
      image = Some(Image("camunda_image")),
      environment = Environment(
        MutableMap(
          "constraint" -> "bull",
          "VAR" -> "5",
          "OTHER_VAR" -> "http://google.com"
        )
      ),
      memLimit = Some(MemLimit(limit = 5, unit = GigaByte)),
      cpuSet = Some(CpuSet(4)),
      volumesFrom = Some(VolumesFrom(Vector(("db", Some(ReadOnly)), ("other", None)))),
      dependsOn = Some(DependsOn(Vector("otherService"))),
      pid = Some(Pid("host"))
    )

    service should be (parsedService)

  }



}
