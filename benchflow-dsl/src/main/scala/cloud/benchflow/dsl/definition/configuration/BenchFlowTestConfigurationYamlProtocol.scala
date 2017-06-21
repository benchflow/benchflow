package cloud.benchflow.dsl.definition.configuration

import cloud.benchflow.dsl.definition.BenchFlowTestYamlProtocol.ConfigurationKey
import cloud.benchflow.dsl.definition.configuration.goal.Goal
import cloud.benchflow.dsl.definition.configuration.goal.GoalYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.strategy.ExplorationStrategy
import cloud.benchflow.dsl.definition.configuration.strategy.ExplorationStrategyYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowTestTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowTestTerminationCriteriaYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecution
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecutionYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler._
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 10.03.17.
 */
object BenchFlowTestConfigurationYamlProtocol extends DefaultYamlProtocol {

  val GoalKey = YamlString("goal")
  val UsersKey = YamlString("users")
  val WorkloadExecutionKey = YamlString("workload_execution")
  val StrategyKey = YamlString("strategy")
  val TerminationCriteriaKey = YamlString("termination_criteria")

  val Level = s"${ConfigurationKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object ConfigurationReadFormat extends YamlFormat[Try[BenchFlowTestConfiguration]] {

    override def read(yaml: YamlValue): Try[BenchFlowTestConfiguration] = {

      val yamlObject = yaml.asYamlObject

      for {

        goal <- deserializationHandler(
          yamlObject.fields(GoalKey).convertTo[Try[Goal]].get,
          keyString(GoalKey))

        users <- deserializationHandler(
          yamlObject.getFields(UsersKey).headOption.map(_.convertTo[Int]),
          keyString(UsersKey))

        workloadExecution <- deserializationHandler(
          yamlObject.fields(WorkloadExecutionKey).convertTo[Try[WorkloadExecution]].get,
          keyString(WorkloadExecutionKey))

        strategy <- deserializationHandler(
          yamlObject.getFields(StrategyKey).headOption.map(_.convertTo[Try[ExplorationStrategy]].get),
          keyString(StrategyKey))

        terminationCriteria <- deserializationHandler(
          yamlObject.fields(TerminationCriteriaKey).convertTo[Try[BenchFlowTestTerminationCriteria]].get,
          keyString(TerminationCriteriaKey))

      } yield BenchFlowTestConfiguration(
        goal = goal,
        users = users,
        workloadExecution = workloadExecution,
        strategy = strategy,
        terminationCriteria = terminationCriteria)

    }

    override def write(configuration: Try[BenchFlowTestConfiguration]): YamlValue = unsupportedWriteOperation

  }

  implicit object ConfigurationWriteFormat extends YamlFormat[BenchFlowTestConfiguration] {

    override def write(obj: BenchFlowTestConfiguration): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        GoalKey -> obj.goal.toYaml) ++
        obj.users.map(key => UsersKey -> key.toYaml) +
        (WorkloadExecutionKey -> obj.workloadExecution.toYaml) ++
        obj.strategy.map(key => StrategyKey -> key.toYaml) +
        (TerminationCriteriaKey -> obj.terminationCriteria.toYaml)
    }

    override def read(yaml: YamlValue): BenchFlowTestConfiguration = unsupportedReadOperation
  }

}
