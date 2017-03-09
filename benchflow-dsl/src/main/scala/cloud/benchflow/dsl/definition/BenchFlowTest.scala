package cloud.benchflow.dsl.definition

import cloud.benchflow.dsl.definition.configuration.ParameterDefinition

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 18/07/16.
  */

sealed trait GoalType
object GoalType {

  def apply(goalType: String) = goalType match {
    case "config" => Config
    case "custom" => Custom
  }

}
case object Config extends GoalType
case object Custom extends GoalType

//TODO: if needed, improve types for explored and observed
//will probably need to implement a type for each observable metric
case class Goal(goalType: GoalType,
                params: Seq[ParameterDefinition[_]],
                explored: Map[String, Seq[String]],
                observed: Option[Map[String, Seq[String]]])


case class BenchFlowTest(name: String,
                         description: String,
                         sut: Sut,
                         trials: TotalTrials,
                         goal: Goal,
                         drivers: Seq[Driver[_ <: Operation]],
                         loadFunction: LoadFunction,
                         properties: Option[Properties],
                         sutConfiguration: SutConfiguration)
