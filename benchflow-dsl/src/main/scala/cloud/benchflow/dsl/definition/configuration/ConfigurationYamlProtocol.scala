package cloud.benchflow.dsl.definition.configuration

import cloud.benchflow.dsl.definition.configuration.goal.Goal
import cloud.benchflow.dsl.definition.configuration.goal.GoalYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.TerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.TerminationCriteriaYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecution
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecutionYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 10.03.17.
  */
object ConfigurationYamlProtocol extends DefaultYamlProtocol {

  val GoalKey = "goal"
  val UsersKey = "users"
  val WorkloadExecutionKey = "workload_execution"
  val TerminationCriteriaKey = "termination_criteria"

  implicit object ConfigurationFormat extends YamlFormat[Try[Configuration]] {

    override def read(yaml: YamlValue): Try[Configuration] = {

      val yamlObject = yaml.asYamlObject

      for {

        goal <- yamlObject.fields(YamlString(GoalKey)).convertTo[Try[Goal]]
        users <- Try(yamlObject.fields(YamlString(UsersKey)).convertTo[Int])
        workloadExecution <- Try(yamlObject.getFields(YamlString(WorkloadExecutionKey)).headOption.map(_.convertTo[Try[WorkloadExecution]].get))
        strategy <- Try(Option(None)) // TODO - define
        terminationCriteria <- Try(yamlObject.getFields(YamlString(TerminationCriteriaKey)).headOption.map(_.convertTo[Try[TerminationCriteria]].get))

      } yield Configuration(goal = goal,
        users = users,
        workloadExecution = workloadExecution,
        strategy = strategy,
        terminationCriteria = terminationCriteria
      )

    }


    override def write(configuration: Try[Configuration]): YamlValue = YamlObject(

      // TODO

      //      YamlString(GoalKey) -> configuration.goal.toYaml,
      //      YamlString(UsersKey) -> YamlNumber(configuration.users),
      //      YamlString(WorkloadExecutionKey) -> configuration.workloadExecution.toYaml,
      //      YamlString(TerminationCriteriaKey) -> configuration.terminationCriteria.toYaml

    )

  }

}
