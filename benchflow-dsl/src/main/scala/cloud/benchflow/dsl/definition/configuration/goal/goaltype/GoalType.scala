package cloud.benchflow.dsl.definition.configuration.goal.goaltype

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-22
 */
object GoalType extends Enumeration {
  type GoalType = Value
  val Load = Value("load")
  //  val OptimalConfiguration = Value("optimal_configuration")
  val ExhaustiveExploration = Value("exhaustive_exploration")
  val PredictiveExploration = Value("predictive_exploration")
}
