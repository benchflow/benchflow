package cloud.benchflow.dsl.definition

import cloud.benchflow.dsl.definition.simone.properties.Properties
import cloud.benchflow.dsl.definition.workload.mix.Mix

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 18/07/16.
 */
package object simone {

  /**
   * *
   * Abstract operation model
   */
  abstract class Operation(val name: String, val data: Option[String])

  sealed trait DriverMetric //TODO: possible values will be: ops/sec, req/s(?)

  case class DriverConfiguration(max90th: Option[Double], mix: Option[Mix], popularity: Option[Float])

  /**
   * Abstract driver model
   */
  abstract class Driver[A <: Operation](
    val properties: Option[Properties],
    val operations: Seq[A],
    val configuration: Option[DriverConfiguration])

  case class TotalTrials(trials: Int)

  case class LoadFunction(rampUp: Int, steadyState: Int, rampDown: Int)

}
