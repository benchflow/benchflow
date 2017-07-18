package cloud.benchflow.dsl.definition.yamlprotocol.valuesrange

import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationExceptionMessage

import scala.util.{Failure, Success, Try}

/**
  * @author Vincenzo Ferme <info@vincenzoferme.it>
  */
trait MonotonicPositiveIntValuesRangeYamlProtocol extends IntValuesRangeYamlProtocol {

  /*
   * We must only have increasing positive values for Monotonic Positive
   */
  protected override def assertValidValues(values: List[Int]): Try[List[Int]] = {

    // check that all values are in increasing order
    // TODO: maybe remove because of https://github.com/benchflow/benchflow/issues/397#issuecomment-314408192
    val increasingOrder = isValuesIncreasingOrder(values)

    //TODO: optimize to check first for the head value

    //Monotonic
    if (!increasingOrder) {
      Failure(BenchFlowDeserializationExceptionMessage("values must be increasing"))
    //Positive
    } else if (values.head <= 0) {
      Failure(BenchFlowDeserializationExceptionMessage("the first value must be > 0"))
    } else {
      Success(values)
    }

  }

  /*
   * We must ensure that we can not get negative values when applying the step
   */
  protected override def getStepList(range: List[Int], step: String): Try[List[Int]] = {

    val rangeValid = assertRangeIsValid(range)

    if (rangeValid.isFailure) {
      Failure(rangeValid.failed.get)
    } else {

      val (prefix: String, stepValue: Int) = getIntStep(step)

      if( stepValue>0 )
        generateStepList(range, stepValue, prefix)
      else
        Failure(BenchFlowDeserializationExceptionMessage("step function value must be >0"))

    }

  }

  /*
   * We must only have positive values for Monotonic Positive
   */
  protected override def assertRangeIsValid(range: List[Int]): Try[List[Int]] = {

    val rangeValid = super.assertRangeIsValid(range)

    if (rangeValid.isSuccess) {
      //Positive
      if (range.head <= 0) {
        Failure(BenchFlowDeserializationExceptionMessage("range head value must be > 0"))
      } else {
        Success(range)
      }
    } else {
      Failure(rangeValid.failed.get)
    }

  }

}
