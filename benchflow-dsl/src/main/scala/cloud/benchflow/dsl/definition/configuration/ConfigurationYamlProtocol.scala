package cloud.benchflow.dsl.definition.configuration

import cloud.benchflow.dsl.definition.configuration.goal.Goal
import cloud.benchflow.dsl.definition.configuration.goal.GoalYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.TerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.TerminationCriteriaYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecution
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecutionYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler._
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 10.03.17.
 */
object ConfigurationYamlProtocol extends DefaultYamlProtocol {

  val GoalKey = YamlString("goal")
  val UsersKey = YamlString("users")
  val WorkloadExecutionKey = YamlString("workload_execution")
  val StrategyKey = YamlString("strategy")
  val TerminationCriteriaKey = YamlString("termination_criteria")

  private def keyString(key: YamlString) = "configuration." + key.value

  implicit object ConfigurationReadFormat extends YamlFormat[Try[Configuration]] {

    override def read(yaml: YamlValue): Try[Configuration] = {

      val yamlObject = yaml.asYamlObject

      for {

        goal <- deserializationHandler(
          yamlObject.fields(GoalKey).convertTo[Try[Goal]].get,
          keyString(GoalKey))

        users <- deserializationHandler(
          yamlObject.getFields(UsersKey).headOption.map(_.convertTo[Int]),
          keyString(UsersKey))

        workloadExecution <- deserializationHandler(
          yamlObject.getFields(WorkloadExecutionKey).headOption.map(_.convertTo[Try[WorkloadExecution]].get),
          keyString(WorkloadExecutionKey))

        // TODO - define
        strategy <- deserializationHandler(
          Option(None),
          keyString(StrategyKey))

        terminationCriteria <- deserializationHandler(
          yamlObject.getFields(TerminationCriteriaKey).headOption.map(_.convertTo[Try[TerminationCriteria]].get),
          keyString(TerminationCriteriaKey))

      } yield Configuration(
        goal = goal,
        users = users,
        workloadExecution = workloadExecution,
        strategy = strategy,
        terminationCriteria = terminationCriteria)

    }

    override def write(configuration: Try[Configuration]): YamlValue = unsupportedWriteOperation

  }

  implicit object ConfigurationWriteFormat extends YamlFormat[Configuration] {

    override def write(obj: Configuration): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        GoalKey -> obj.goal.toYaml) ++
        obj.users.map(key => UsersKey -> key.toYaml) ++
        obj.workloadExecution.map(key => WorkloadExecutionKey -> key.toYaml) ++
        // TODO - add Strategy when defined
        obj.terminationCriteria.map(key => TerminationCriteriaKey -> key.toYaml)
    }

    override def read(yaml: YamlValue): Configuration = unsupportedReadOperation
  }

}
