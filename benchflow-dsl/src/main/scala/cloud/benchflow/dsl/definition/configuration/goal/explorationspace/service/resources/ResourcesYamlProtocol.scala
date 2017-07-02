package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.service.resources

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.explorationvalues.ByteValues
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.explorationvalues.ExplorationValuesBytesYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.service.ServiceExplorationSpaceYamlProtocol
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.service.ServiceExplorationSpaceYamlProtocol.ResourcesKey
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
object ResourcesYamlProtocol extends DefaultYamlProtocol {

  val MemoryKey = YamlString("memory")

  val Level = s"${ServiceExplorationSpaceYamlProtocol.Level}.${ResourcesKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object ResourcesReadFormat extends YamlFormat[Try[Resources]] {

    override def read(yaml: YamlValue): Try[Resources] = {

      val yamlObject = yaml.asYamlObject

      for {

        memory <- deserializationHandler(
          yamlObject.getFields(MemoryKey).headOption.map(_.convertTo[Try[ByteValues]].get),
          keyString(MemoryKey))

      } yield Resources(
        memory = memory)

    }

    override def write(obj: Try[Resources]): YamlValue = unsupportedWriteOperation
  }

  implicit object ResourcesWriteFormat extends YamlFormat[Resources] {

    override def write(obj: Resources): YamlValue = YamlObject {
      Map[YamlValue, YamlValue]() ++
        obj.memory.map(key => MemoryKey -> key.toYaml)
    }

    override def read(yaml: YamlValue): Resources = unsupportedReadOperation
  }

}
