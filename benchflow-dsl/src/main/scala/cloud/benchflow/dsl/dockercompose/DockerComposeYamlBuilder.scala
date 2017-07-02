package cloud.benchflow.dsl.dockercompose

import cloud.benchflow.dsl.definition.types.bytes.Bytes
import cloud.benchflow.dsl.definition.types.bytes.BytesYamlProtocol._
import net.jcazevedo.moultingyaml.{YamlString, YamlValue, _}

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-25
 */
class DockerComposeYamlBuilder(dockerComposeYamlValue: YamlValue) {

  val ServicesKey = YamlString("services")
  val MemLimitKey = YamlString("mem_limit")
  val EnvironmentKey = YamlString("environment")

  def memLimit(serviceName: String, memLimit: Bytes): DockerComposeYamlBuilder = {

    val serviceKey = serviceName.toYaml

    val servicesYamlObject = dockerComposeYamlValue.asYamlObject.fields(ServicesKey).asYamlObject

    val serviceYamlObject = servicesYamlObject.fields(serviceKey).asYamlObject

    // add/change the memory limit
    val serviceConfigurationYamlObject = YamlObject(serviceYamlObject.fields + (MemLimitKey -> memLimit.toYaml))

    // construct new docker compose
    val newYamlObject = YamlObject(dockerComposeYamlValue.asYamlObject.fields +
      (ServicesKey -> YamlObject(servicesYamlObject.fields
        + (serviceKey -> serviceConfigurationYamlObject))))

    new DockerComposeYamlBuilder(newYamlObject)

  }

  //  def cpus()

  def environmentVariable(serviceName: String, variableName: String, variableValue: String): DockerComposeYamlBuilder = {

    val serviceKey = serviceName.toYaml

    val servicesYamlObject = dockerComposeYamlValue.asYamlObject.fields(ServicesKey).asYamlObject

    val serviceYamlObject = servicesYamlObject.fields(serviceKey).asYamlObject

    val environmentList = serviceYamlObject.fields(EnvironmentKey).asInstanceOf[YamlArray].elements.toList

    // add/change the environment
    val newEnvironmentList = YamlString(s"$variableName=$variableValue") ::
      environmentList.filterNot(value => value.convertTo[String].contains(s"$variableName="))

    // construct new docker compose
    val newYamlObject = YamlObject(dockerComposeYamlValue.asYamlObject.fields +
      (ServicesKey -> YamlObject(servicesYamlObject.fields
        + (serviceKey -> YamlObject(serviceYamlObject.fields
          + (EnvironmentKey -> newEnvironmentList.toYaml))))))

    new DockerComposeYamlBuilder(newYamlObject)

  }

  def build(): String = {
    dockerComposeYamlValue.prettyPrint
  }

}
