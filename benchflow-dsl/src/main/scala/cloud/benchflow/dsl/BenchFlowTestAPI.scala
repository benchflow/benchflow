package cloud.benchflow.dsl

import cloud.benchflow.dsl.definition.BenchFlowTest
import cloud.benchflow.dsl.definition.BenchFlowTestYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.{ BenchFlowDeserializationException, BenchFlowDeserializationExceptionMessage }
import cloud.benchflow.dsl.definition.validation.SemanticValidation
import net.jcazevedo.moultingyaml._

import scala.util.{ Failure, Success, Try }

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 10.03.17.
 */
object BenchFlowTestAPI {

  /**
   * To serialize/deserialize YAML this library uses https://github.com/jcazevedo/moultingyaml.
   */

  /**
   *
   * @param testDefinitionYaml
   * @throws cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
   * @return
   */
  @throws(classOf[BenchFlowDeserializationException])
  @throws(classOf[BenchFlowDeserializationExceptionMessage])
  def testFromYaml(testDefinitionYaml: String): BenchFlowTest = {

    // validates syntax
    // TODO - document why we wrap in a Try (e.g. because of library and deserialization)
    val triedTest: Try[BenchFlowTest] = testDefinitionYaml.parseYaml.convertTo[Try[BenchFlowTest]]

    val test = triedTest match {
      case Success(t) => t
      case Failure(ex) => throw ex
    }

    // validate semantic in separate function on the object
    val (isValid, validationErrorMessages) = SemanticValidation.validateTest(test)

    if (!isValid) {
      throw BenchFlowDeserializationExceptionMessage(validationErrorMessages.toString())
    }

    test
  }

  def testToYamlString(benchFlowTest: BenchFlowTest): String = {

    // TODO - validate semantic in separate function on the object

    // write to YAML
    val testYaml: YamlObject = benchFlowTest.toYaml.asYamlObject

    testYaml.prettyPrint

  }

}
