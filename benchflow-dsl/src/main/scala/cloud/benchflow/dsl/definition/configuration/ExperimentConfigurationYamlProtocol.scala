package cloud.benchflow.dsl.definition.configuration

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.ExperimentOnlyTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.ExperimentOnlyTerminationCriteriaYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecution
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecutionYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 10.03.17.
 */
object ExperimentConfigurationYamlProtocol extends DefaultYamlProtocol {

  val UsersKey = YamlString("users")
  val WorkloadExecutionKey = YamlString("workload_execution")
  val StrategyKey = YamlString("strategy")
  val TerminationCriteriaKey = YamlString("termination_criteria")

  private def keyString(key: YamlString) = "configuration." + key.value

  implicit object ExperimentConfigurationReadFormat extends YamlFormat[Try[ExperimentConfiguration]] {

    override def read(yaml: YamlValue): Try[ExperimentConfiguration] = {

      val yamlObject = yaml.asYamlObject

      for {

        users <- deserializationHandler(
          yamlObject.getFields(UsersKey).headOption.map(_.convertTo[Int]),
          keyString(UsersKey)
        )

        workloadExecution <- deserializationHandler(
          yamlObject.getFields(WorkloadExecutionKey).headOption.map(_.convertTo[Try[WorkloadExecution]].get),
          keyString(WorkloadExecutionKey)
        )

        terminationCriteria <- deserializationHandler(
          yamlObject.getFields(TerminationCriteriaKey).headOption.map(_.convertTo[Try[ExperimentOnlyTerminationCriteria]].get),
          keyString(TerminationCriteriaKey)
        )

      } yield ExperimentConfiguration(
        users = users,
        workloadExecution = workloadExecution,
        terminationCriteria = terminationCriteria
      )

    }

    override def write(configuration: Try[ExperimentConfiguration]): YamlValue = unsupportedWriteOperation

  }

  implicit object ExperimentConfigurationWriteFormat extends YamlFormat[ExperimentConfiguration] {

    override def write(obj: ExperimentConfiguration): YamlValue = YamlObject {

      Map[YamlValue, YamlValue]() ++
        obj.users.map(key => UsersKey -> key.toYaml) ++
        obj.workloadExecution.map(key => WorkloadExecutionKey -> key.toYaml) ++
        obj.terminationCriteria.map(key => TerminationCriteriaKey -> key.toYaml)

    }

    override def read(yaml: YamlValue): ExperimentConfiguration = unsupportedReadOperation
  }

}
