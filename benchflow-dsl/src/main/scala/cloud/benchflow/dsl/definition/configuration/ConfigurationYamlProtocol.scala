package cloud.benchflow.dsl.definition.configuration

import cloud.benchflow.dsl.definition.configuration.goal.Goal
import cloud.benchflow.dsl.definition.configuration.goal.GoalYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.TerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.TerminationCriteriaYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecution
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecutionYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

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

  implicit object ConfigurationFormat extends YamlFormat[Try[Configuration]] {

    override def read(yaml: YamlValue): Try[Configuration] = {

      val yamlObject = yaml.asYamlObject

      for {

        goal <- deserializationHandler(
          yamlObject.fields(GoalKey).convertTo[Try[Goal]].get,
          keyString(GoalKey)
        )

        users <- deserializationHandler(
          yamlObject.getFields(UsersKey).headOption.map(_.convertTo[Int]),
          keyString(UsersKey)
        )

        workloadExecution <- deserializationHandler(
          yamlObject.getFields(WorkloadExecutionKey).headOption.map(_.convertTo[Try[WorkloadExecution]].get),
          keyString(WorkloadExecutionKey)
        )

        // TODO - define
        strategy <- deserializationHandler(
          Option(None),
          keyString(StrategyKey)
        )

        terminationCriteria <- deserializationHandler(
          yamlObject.getFields(TerminationCriteriaKey).headOption.map(_.convertTo[Try[TerminationCriteria]].get),
          keyString(TerminationCriteriaKey)
        )

      } yield Configuration(goal = goal,
        users = users,
        workloadExecution = workloadExecution,
        strategy = strategy,
        terminationCriteria = terminationCriteria
      )

    }


    override def write(configuration: Try[Configuration]): YamlValue = {

      val config = configuration.get

      var map = Map[YamlValue, YamlValue](
        GoalKey -> Try(config.goal).toYaml
      )

      if (config.users.isDefined)
        map += UsersKey -> YamlNumber(config.users.get)

      if (config.workloadExecution.isDefined)
        map += WorkloadExecutionKey -> Try(config.workloadExecution.get).toYaml

      // TODO - add Strategy when defined

      if (config.terminationCriteria.isDefined)
        map += TerminationCriteriaKey -> Try(config.terminationCriteria.get).toYaml

      YamlObject(map)

    }

  }

}
