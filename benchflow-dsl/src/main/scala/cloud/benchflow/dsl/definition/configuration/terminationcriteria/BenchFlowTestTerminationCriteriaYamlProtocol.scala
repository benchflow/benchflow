package cloud.benchflow.dsl.definition.configuration.terminationcriteria

import cloud.benchflow.dsl.definition.configuration.BenchFlowTestConfigurationYamlProtocol
import cloud.benchflow.dsl.definition.configuration.BenchFlowTestConfigurationYamlProtocol.TerminationCriteriaKey
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.ExperimentTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.ExperimentTerminationCriteriaYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.exploration.ExplorationTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.exploration.ExplorationTerminationCriteriaYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.test.TestTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.test.TestTerminationCriteriaYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
object BenchFlowTestTerminationCriteriaYamlProtocol extends DefaultYamlProtocol {

  val ExplorationKey = YamlString("exploration")
  val TestKey = YamlString("test")
  val ExperimentKey = YamlString("experiment")

  val Level = s"${BenchFlowTestConfigurationYamlProtocol.Level}.${TerminationCriteriaKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object TerminationCriteriaReadFormat extends YamlFormat[Try[BenchFlowTestTerminationCriteria]] {
    override def read(yaml: YamlValue): Try[BenchFlowTestTerminationCriteria] = {

      val yamlObject = yaml.asYamlObject

      for {

        exploration <- deserializationHandler(
          yamlObject.getFields(ExplorationKey).headOption.map(_.convertTo[Try[ExplorationTerminationCriteria]].get),
          keyString(ExplorationKey))

        test <- deserializationHandler(
          yamlObject.fields(TestKey).convertTo[Try[TestTerminationCriteria]].get,
          keyString(TestKey))

        experiment <- deserializationHandler(
          yamlObject.fields(ExperimentKey).convertTo[Try[ExperimentTerminationCriteria]].get,
          keyString(ExperimentKey))

      } yield BenchFlowTestTerminationCriteria(
        exploration = exploration,
        test = test,
        experiment = experiment)

    }

    override def write(terminationCriteriaTry: Try[BenchFlowTestTerminationCriteria]): YamlValue = unsupportedWriteOperation
  }

  implicit object TerminationCriteriaWriteFormat extends YamlFormat[BenchFlowTestTerminationCriteria] {
    override def write(obj: BenchFlowTestTerminationCriteria): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        TestKey -> obj.test.toYaml,
        ExperimentKey -> obj.experiment.toYaml)

    }

    override def read(yaml: YamlValue): BenchFlowTestTerminationCriteria = unsupportedReadOperation
  }

}
