package cloud.benchflow.dsl.definition.configuration.goal

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.ExplorationSpace
import cloud.benchflow.dsl.definition.configuration.goal.goaltype.GoalType.GoalType

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
case class Goal(
  goalType: GoalType,
  observation: Option[Any], // TODO - define type
  explorationSpace: Option[ExplorationSpace])
