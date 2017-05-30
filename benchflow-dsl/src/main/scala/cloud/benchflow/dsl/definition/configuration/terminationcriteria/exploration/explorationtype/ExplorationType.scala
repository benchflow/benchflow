package cloud.benchflow.dsl.definition.configuration.terminationcriteria.exploration.explorationtype

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-22
 */
object ExplorationType extends Enumeration {
  type ExplorationType = Value
  val Fixed = Value("fixed")
  val Time = Value("time")
  val Precision = Value("precision")
  val MaxFailed = Value("max_failed")
}
