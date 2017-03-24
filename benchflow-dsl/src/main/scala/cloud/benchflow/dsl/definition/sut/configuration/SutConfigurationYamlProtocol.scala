package cloud.benchflow.dsl.definition.sut.configuration

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.deserializationHandler
import cloud.benchflow.dsl.definition.sut.configuration.targetservice.TargetService
import cloud.benchflow.dsl.definition.sut.configuration.targetservice.TargetServiceYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
object SutConfigurationYamlProtocol extends DefaultYamlProtocol {

  val TargetServiceKey = YamlString("target_service")
  val DeploymentKey = YamlString("deployment")

  private def keyString(key: YamlString) = "sut.configuration." + key.value

  implicit object SutConfigurationFormat extends YamlFormat[Try[SutConfiguration]] {

    override def read(yaml: YamlValue): Try[SutConfiguration] = {

      val yamlObject = yaml.asYamlObject

      for {

        targetService <- deserializationHandler(
          yamlObject.fields(TargetServiceKey).convertTo[Try[TargetService]].get,
          keyString(TargetServiceKey)
        )

        deployment <- deserializationHandler(
          yamlObject.fields(DeploymentKey).convertTo[Map[String, String]],
          keyString(DeploymentKey)
        )

      } yield SutConfiguration(
        targetService = targetService,
        deployment = deployment
      )

    }

    override def write(obj: Try[SutConfiguration]): YamlValue = {

      val sutConfig = obj.get

      val map = Map[YamlValue, YamlValue](
        TargetServiceKey -> Try(sutConfig.targetService).toYaml,
        DeploymentKey -> sutConfig.deployment.toYaml
      )

      YamlObject(map)

    }

  }

}
