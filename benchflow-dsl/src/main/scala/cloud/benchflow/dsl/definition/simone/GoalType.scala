package cloud.benchflow.dsl.definition.simone

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-03-17
 */
sealed trait GoalType

object GoalType {

  def apply(goalType: String): GoalType = goalType match {
    case "config" => Config
    case "custom" => Custom
  }

}
case object Config extends GoalType
case object Custom extends GoalType
