package cloud.benchflow.dsl.definition.configuration.terminationcriteria

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.ExperimentTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.ExperimentTerminationCriteriaYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation }
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
object ExperimentOnlyTerminationCriteriaYamlProtocol extends DefaultYamlProtocol {

  val ExperimentKey = YamlString("experiment")

  private def keyString(key: YamlString) = "configuration.termination_criteria" + key.value

  implicit object ExperimentOnlyTerminationCriteriaReadFormat extends YamlFormat[Try[ExperimentOnlyTerminationCriteria]] {
    override def read(yaml: YamlValue): Try[ExperimentOnlyTerminationCriteria] = {

      val yamlObject = yaml.asYamlObject

      for {

        experiment <- deserializationHandler(
          yamlObject.fields(ExperimentKey).convertTo[Try[ExperimentTerminationCriteria]].get,
          keyString(ExperimentKey))

      } yield ExperimentOnlyTerminationCriteria(experiment = experiment)

    }

    override def write(terminationCriteriaTry: Try[ExperimentOnlyTerminationCriteria]): YamlValue = unsupportedWriteOperation
  }

  implicit object ExperimentOnlyTerminationCriteriaWriteFormat extends YamlFormat[ExperimentOnlyTerminationCriteria] {
    override def write(obj: ExperimentOnlyTerminationCriteria): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        ExperimentKey -> obj.experiment.toYaml)
    }

    override def read(yaml: YamlValue): ExperimentOnlyTerminationCriteria = unsupportedReadOperation
  }

}
