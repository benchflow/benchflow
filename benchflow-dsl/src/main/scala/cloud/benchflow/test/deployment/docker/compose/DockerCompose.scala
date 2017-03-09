package cloud.benchflow.test.deployment.docker.compose

import cloud.benchflow.test.deployment.docker.compose.DockerComposeYamlProtocol._
import cloud.benchflow.test.deployment.docker.service.Service
import net.jcazevedo.moultingyaml._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 05/07/16.
  */

case class NetworkConfig(driver: String)
case class Networks(nets: Map[String, NetworkConfig])

case class DockerCompose(services: Map[String, Service],
                         version: String,
                         networks: Option[Networks]) {

  override def toString = this.toYaml.prettyPrint

}
object DockerCompose {

  def fromYaml(yaml: String): DockerCompose = yaml.parseYaml.convertTo[DockerCompose]

  def toYaml(dc: DockerCompose): String = dc.toYaml.prettyPrint

}
