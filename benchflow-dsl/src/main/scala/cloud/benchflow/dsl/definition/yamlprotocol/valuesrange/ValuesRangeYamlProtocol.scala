package cloud.benchflow.dsl.definition.yamlprotocol.valuesrange

import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationExceptionMessage
import net.jcazevedo.moultingyaml.YamlString

import scala.util.{ Failure, Success, Try }

/**
 * @author Vincenzo Ferme <info@vincenzoferme.it>
 */
trait ValuesRangeYamlProtocol {

  protected val ValuesKey = YamlString("values")
  protected val RangeKey = YamlString("range")
  protected val StepKey = YamlString("step")

  protected def assertRangeIsValid(range: List[Int]): Try[List[Int]] = {

    if (range.size != 2 || range.head == range.last) {
      Failure(BenchFlowDeserializationExceptionMessage("range must be specified as a List of 2 different elements"))
    } else {
      Success(range)
    }

  }

  protected def assertIncreasingRange(range: List[Int]): Try[List[Int]] = {

    if (range.head >= range.last) {
      Failure(BenchFlowDeserializationExceptionMessage("range must be increasing"))
    } else {
      Success(range)
    }

  }

  protected def assertDecreasingRange(range: List[Int]): Try[List[Int]] = {

    if (range.head <= range.last) {
      Failure(BenchFlowDeserializationExceptionMessage("range must be decreasing"))
    } else {
      Success(range)
    }

  }

  protected def isValuesIncreasingOrder(values: List[Int]) = {
    values.sliding(2).forall {
      case List(x, y) => x < y
    }
  }

  protected def getRangeList(range: List[Int]): Try[List[Int]]

  protected def getStepList(range: List[Int], step: String): Try[List[Int]]

  protected def assertValidValues(values: List[Int]): Try[List[Int]]

}
