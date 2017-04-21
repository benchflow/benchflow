package cloud.benchflow.dsl.definition.configuration.goal.explorationspace

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.WorkloadExplorationSpace
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.WorkloadExplorationSpaceYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler._
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
object ExplorationSpaceYamlProtocol extends DefaultYamlProtocol {

  val WorkloadKey = YamlString("workload")

  private def keyString(key: YamlString) = "configuration.goal.exploration_space.workload." + key.value

  implicit object ExplorationSpaceReadFormat extends YamlFormat[Try[ExplorationSpace]] {

    override def read(yaml: YamlValue): Try[ExplorationSpace] = {

      val yamlObject = yaml.asYamlObject

      for {

        workload <- deserializationHandler(
          yamlObject.getFields(WorkloadKey).headOption.map(_.convertTo[Try[WorkloadExplorationSpace]].get),
          keyString(WorkloadKey))

      } yield ExplorationSpace(
        workload = workload)

    }

    override def write(obj: Try[ExplorationSpace]): YamlValue = unsupportedWriteOperation
  }

  implicit object ExplorationSpaceWriteFormat extends YamlFormat[ExplorationSpace] {
    override def write(obj: ExplorationSpace): YamlValue = YamlObject {

      Map[YamlValue, YamlValue]() ++
        obj.workload.map(key => WorkloadKey -> key.toYaml)
    }

    override def read(yaml: YamlValue): ExplorationSpace = unsupportedReadOperation
  }

}
