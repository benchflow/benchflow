package cloud.benchflow.dsl

import cloud.benchflow.dsl.definition.properties.Properties

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 18/07/16.
  */
package object definition {

  /***
    * Abstract operation model
    */
  abstract class Operation(val name: String, val data: Option[String])

  /**
    * Possible mixes
    */
  sealed abstract class Mix(deviation: Option[Double])

  //This mix maintains the state of execution.
  //It chooses the next operation based on the current operation and a given probability ratio.
  case class MatrixMixRow(row: Seq[Double])
  case class MatrixMix(rows: Seq[MatrixMixRow], deviation: Option[Double]) extends Mix(deviation)

  //This mix randomly chooses the next operation to execute based on given probability for the mix.
  case class FlatMix(opsMix: Seq[Double], deviation: Option[Double]) extends Mix(deviation)

  //The fixed sequence does what it says. There is no randomness. The operations are called in sequence.
  case class FixedSequenceMix(sequence: Seq[String], deviation: Option[Double]) extends Mix(deviation)

  //This mix allows random selection of fixed sequences (as opposed to random selection of an operation in FlatMix).
  case class FlatSequenceMixRow(row: Seq[String])
  case class FlatSequenceMix(opsMix: Seq[Double],
                             rows: Seq[FlatSequenceMixRow],
                             deviation: Option[Double]) extends Mix(deviation)

  sealed trait DriverMetric //TODO: possible values will be: ops/sec, req/s(?)

  case class DriverConfiguration(max90th: Option[Double], mix: Option[Mix], popularity: Option[Float])


  /**
    * Abstract driver model
    */
  abstract class Driver[A <: Operation](val properties: Option[Properties],
                                               val operations: Seq[A],
                                               val configuration: Option[DriverConfiguration])


  case class TotalTrials(trials: Int)

  case class LoadFunction(rampUp: Int, steadyState: Int, rampDown: Int)

}
