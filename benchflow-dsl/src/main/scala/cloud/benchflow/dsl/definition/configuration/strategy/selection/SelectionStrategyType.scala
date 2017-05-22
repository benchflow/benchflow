package cloud.benchflow.dsl.definition.configuration.strategy.selection

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-21
 */
object SelectionStrategyType extends Enumeration {
  type SelectionStrategyType = Value
  val OneAtATime = Value("one-at-a-time")
  val RandomBreakDown = Value("random_breakdown")
  val BoundaryFirst = Value("boundary_first")
  //  val AdaptiveEquidistantBreakdown = Value("adaptive_equidistant_breakdown")
  //  val AdaptiveRandomBreakDown = Value("adaptive_random_breakdown")
}
