package cloud.benchflow.dsl.definition.yamlprotocol.valuesrange

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.step.RangeWithStep
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationExceptionMessage

import scala.util.{Failure, Try}

/**
 * @author Vincenzo Ferme <info@vincenzoferme.it>
 */
trait IntValuesRangeYamlProtocol extends ValuesRangeYamlProtocol {

  protected override def getStepList(range: List[Int], step: String): Try[List[Int]] = {

    val rangeValid = assertRangeIsValid(range)

    if (rangeValid.isFailure) {
      Failure(rangeValid.failed.get)
    } else {

      val (prefix: String, stepValue: Int) = getIntStep(step)

      generateStepList(range, stepValue, prefix)

    }

  }

  protected def getIntStep(step: String) = {

    val prefix = step.substring(0, 1)

    val triedStepValue = Try(step.substring(1).toInt)

    if (triedStepValue.isFailure) {
      Failure(BenchFlowDeserializationExceptionMessage("step must contain an integer"))
    } else {
      (prefix, triedStepValue.get)
    }

  }

  /*
   * Here we validate that the values we get from the user for step, range.head and range.tail
   * are compliant with the mathematical operator we apply on them.
   * We mostly check for Absorbing, Identity, Inverse and Negative elements
   *
   * NOTE: we might have some redundancy with checks in other methods, but this enhance the
   * reusability of this method and make it generic for all int cases
   */
  protected def generateStepList(range: List[Int], stepValue: Int, prefix: String): Try[List[Int]] = prefix match {

    case "+" =>
      val increasingRange = assertIncreasingRange(range)

      if (stepValue == 0) {
        Failure(BenchFlowDeserializationExceptionMessage("step function cannot be +0"))
      } else if (increasingRange.isFailure) {
        increasingRange
      } else {
        RangeWithStep.stepList(increasingRange.get, stepValue, (a: Int, b: Int) => a + b)
      }

    case "*" =>
      val increasingRange = assertIncreasingRange(range)

      if (stepValue == 0 || stepValue == 1) {
        Failure(BenchFlowDeserializationExceptionMessage("step function cannot be *0 or *1"))
      } else if (range.head == 0) {
        Failure(BenchFlowDeserializationExceptionMessage("first element in the list cannot be *0"))
      } else if (increasingRange.isFailure) {
        increasingRange
      } else {
        RangeWithStep.stepList(increasingRange.get, stepValue, (a: Int, b: Int) => a * b)
      }

    case "^" =>
      val increasingRange = assertIncreasingRange(range)

      if (stepValue <= 0 || stepValue == 1) {
        Failure(BenchFlowDeserializationExceptionMessage("step function cannot be ^-N, ^0 or ^1"))
      } else if (range.head == 0 || range.head == 1) {
        Failure(BenchFlowDeserializationExceptionMessage("first element in the list cannot be ^0 or ^1"))
      } else if (increasingRange.isFailure) {
        increasingRange
      } else {
        RangeWithStep.stepList(increasingRange.get, stepValue, (a: Int, b: Int) => Math.pow(a, b).intValue)
      }

    case _ => Failure(BenchFlowDeserializationExceptionMessage("step operation not supported"))
  }

}
