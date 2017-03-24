package cloud.benchflow.dsl.definition.configuration.terminationcriteria

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.ExperimentTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.ExperimentTerminationCriteriaYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.test.TestTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.test.TestTerminationCriteriaYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
object TerminationCriteriaYamlProtocol extends DefaultYamlProtocol {

  val TestKey = YamlString("test")
  val ExperimentKey = YamlString("experiment")

  private def keyString(key: YamlString) = "configuration.termination_criteria" + key.value

  implicit object TerminationCriteriaReadFormat extends YamlFormat[Try[TerminationCriteria]] {
    override def read(yaml: YamlValue): Try[TerminationCriteria] = {

      val yamlObject = yaml.asYamlObject

      for {

        test <- deserializationHandler(
          yamlObject.fields(TestKey).convertTo[Try[TestTerminationCriteria]].get,
          keyString(TestKey)
        )

        experiment <- deserializationHandler(
          yamlObject.fields(ExperimentKey).convertTo[Try[ExperimentTerminationCriteria]].get,
          keyString(ExperimentKey)
        )

      } yield TerminationCriteria(test = test, experiment = experiment)

    }

    override def write(terminationCriteriaTry: Try[TerminationCriteria]): YamlValue = unsupportedWriteOperation
  }

  implicit object TerminationCriteriaWriteFormat extends YamlFormat[TerminationCriteria] {
    override def write(obj: TerminationCriteria): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        TestKey -> obj.test.toYaml,
        ExperimentKey -> obj.experiment.toYaml
      )

    }

    override def read(yaml: YamlValue): TerminationCriteria = unsupportedReadOperation
  }

}
