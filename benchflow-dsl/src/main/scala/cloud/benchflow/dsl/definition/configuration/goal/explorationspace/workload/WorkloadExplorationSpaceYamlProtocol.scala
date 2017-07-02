package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.ExplorationSpaceYamlProtocol
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.ExplorationSpaceYamlProtocol.WorkloadKey
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.users.UserValues
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.users.UserValuesYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
object WorkloadExplorationSpaceYamlProtocol extends DefaultYamlProtocol {

  val UsersKey = YamlString("users")

  val Level = s"${ExplorationSpaceYamlProtocol.Level}.${WorkloadKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object WorkloadExplorationSpaceReadFormat extends YamlFormat[Try[WorkloadExplorationSpace]] {

    override def read(yaml: YamlValue): Try[WorkloadExplorationSpace] = {

      val yamlObject = yaml.asYamlObject

      for {

        users <- deserializationHandler(
          yamlObject.getFields(UsersKey).headOption.map(_.convertTo[Try[UserValues]].get),
          keyString(UsersKey))

      } yield WorkloadExplorationSpace(
        users = users)

    }

    override def write(obj: Try[WorkloadExplorationSpace]): YamlValue = unsupportedWriteOperation

  }

  implicit object WorkloadExplorationSpaceWriteFormat extends YamlFormat[WorkloadExplorationSpace] {

    override def write(obj: WorkloadExplorationSpace): YamlValue = YamlObject {

      Map[YamlValue, YamlValue]() ++
        obj.users.map(key => UsersKey -> key.toYaml)

    }

    override def read(yaml: YamlValue): WorkloadExplorationSpace = unsupportedReadOperation
  }

}
