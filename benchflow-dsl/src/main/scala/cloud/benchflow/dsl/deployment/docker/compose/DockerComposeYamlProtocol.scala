package cloud.benchflow.dsl.deployment.docker.compose

import cloud.benchflow.dsl.deployment.docker.service._
import cloud.benchflow.dsl.deployment.docker.service.ServiceYamlProtocol._
import net.jcazevedo.moultingyaml._

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 05/07/16.
 */
object DockerComposeYamlProtocol extends DefaultYamlProtocol {

  implicit val implicitNetworkConfigFormat = yamlFormat1(NetworkConfig)

  implicit object NetworksYamlFormat extends YamlFormat[Networks] {

    override def read(yaml: YamlValue): Networks = {
      Networks(
        yaml.asYamlObject.fields.map {
          case (YamlString(netName), YamlObject(obj)) =>
            (netName, YamlObject(obj).convertTo[NetworkConfig])
          case _ => ???
        })
    }

    override def write(nets: Networks): YamlValue = {
      YamlObject(
        nets.nets.map {
          case (netName, netConfig) =>
            netName.toYaml -> netConfig.toYaml
          case _ => throw new SerializationException("Can't serialize networks")
        })
    }

  }

  implicit object DockerComposeYamlFormat extends YamlFormat[DockerCompose] {

    override def read(yaml: YamlValue): DockerCompose = {

      def readService(yaml: YamlValue): (String, Service) = {
        val serviceName = yaml.asYamlObject.fields.head._1.convertTo[String]
        (serviceName, yaml.convertTo[Service])
      }

      val version = yaml.asYamlObject.fields.get(YamlString("version")).get.convertTo[String]
      val services = yaml.asYamlObject.fields.get(YamlString("services")).get match {
        case YamlObject(yamlServices) => yamlServices.map {
          case (sName, sFields) =>
            val s = YamlObject(sName -> sFields).convertTo[Service]
            s.name -> s
        }
        case _ => throw new DeserializationException("Illegal format for services")
      }
      val networks = yaml.asYamlObject.fields.get(YamlString("networks")).map(_.convertTo[Networks])

      new DockerCompose(
        version = version,
        services = services,
        networks = networks)

    }

    override def write(dc: DockerCompose): YamlValue = {

      val parsedNets = dc.networks.toYaml

      parsedNets match {

        case YamlNull =>

          YamlObject(
            YamlString("services") -> {
              YamlObject(dc.services.map {
                case (serviceName, serviceObj) =>
                  serviceName.toYaml -> serviceObj.toYaml.asYamlObject.fields.values.head
              })
            },
            YamlString("version") -> dc.version.toYaml)

        case _ =>

          YamlObject(
            YamlString("services") -> {
              YamlObject(dc.services.map {
                case (serviceName, serviceObj) =>
                  serviceName.toYaml -> serviceObj.toYaml.asYamlObject.fields.values.head
              })
            },
            YamlString("networks") -> parsedNets,
            YamlString("version") -> dc.version.toYaml)

      }

    }

  }

}
