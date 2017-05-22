package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.service

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.ExplorationSpaceYamlProtocol
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.service.resources.Resources
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.service.resources.ResourcesYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation }
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
object ServiceExplorationSpaceYamlProtocol extends DefaultYamlProtocol {

  val ResourcesKey = YamlString("resources")
  val EnvironmentKey = YamlString("environment")

  val Level = s"${ExplorationSpaceYamlProtocol.Level}.(some service)"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object ServiceExplorationSpaceReadFormat extends YamlFormat[Try[ServiceExplorationSpace]] {

    override def read(yaml: YamlValue): Try[ServiceExplorationSpace] = {

      val yamlObject = yaml.asYamlObject

      for {

        resources <- deserializationHandler(
          yamlObject.getFields(ResourcesKey).headOption.map(_.convertTo[Try[Resources]].get),
          keyString(ResourcesKey))

        environment <- deserializationHandler(
          yamlObject.getFields(EnvironmentKey).headOption.map(_.convertTo[Map[String, List[String]]]),
          keyString(EnvironmentKey))

      } yield ServiceExplorationSpace(
        resources = resources,
        environment = environment)

    }

    override def write(obj: Try[ServiceExplorationSpace]): YamlValue = unsupportedWriteOperation

  }

  implicit object ServiceExplorationSpaceWriteFormat extends YamlFormat[ServiceExplorationSpace] {

    override def write(obj: ServiceExplorationSpace): YamlValue = YamlObject {

      Map[YamlValue, YamlValue]() ++
        obj.resources.map(key => ResourcesKey -> key.toYaml) ++
        obj.environment.map(key => EnvironmentKey -> key.toYaml)

    }

    override def read(yaml: YamlValue): ServiceExplorationSpace = unsupportedReadOperation
  }

}
