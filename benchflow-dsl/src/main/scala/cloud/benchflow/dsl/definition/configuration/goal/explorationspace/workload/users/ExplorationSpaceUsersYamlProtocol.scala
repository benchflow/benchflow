package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.users

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler._
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
object ExplorationSpaceUsersYamlProtocol extends DefaultYamlProtocol {

  val ValuesKey = YamlString("values")

  private def keyString(key: YamlString) = "configuration.goal.exploration_space.workload.users." + key.value

  implicit object ExplorationSpaceUsersReadFormat extends YamlFormat[Try[ExplorationSpaceUsers]] {

    override def read(yaml: YamlValue): Try[ExplorationSpaceUsers] = {

      val yamlObject = yaml.asYamlObject

      for {

        values <- deserializationHandler(
          yamlObject.fields(ValuesKey).convertTo[List[Int]],
          keyString(ValuesKey))

      } yield ExplorationSpaceUsers(
        values = values)

    }

    override def write(obj: Try[ExplorationSpaceUsers]): YamlValue = unsupportedWriteOperation

  }

  implicit object ExplorationSpaceUsersWriteFormat extends YamlFormat[ExplorationSpaceUsers] {

    override def write(obj: ExplorationSpaceUsers): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        ValuesKey -> obj.values.toYaml)

    }

    override def read(yaml: YamlValue): ExplorationSpaceUsers = unsupportedReadOperation
  }

}
