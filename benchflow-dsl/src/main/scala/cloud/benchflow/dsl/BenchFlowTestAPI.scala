package cloud.benchflow.dsl

import cloud.benchflow.dsl.definition.BenchFlowTest
import cloud.benchflow.dsl.definition.BenchFlowTestYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.{ BenchFlowDeserializationException, BenchFlowDeserializationExceptionMessage }
import cloud.benchflow.dsl.definition.configuration.goal.goaltype.GoalType.LOAD
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
    val validationErrorMessages = new StringBuilder
    val isValid = validateTest(test, validationErrorMessages)

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

  private val userMissing = "Missing user specification. "

  def validateTest(benchFlowTest: BenchFlowTest, validationErrorMessages: StringBuilder): Boolean = {

    validationErrorMessages.append("Semantic validation failed. ")
    var isValid = true

    // TODO - validate semantic in separate function on the object

    // check that number of users is defined if exploration goal
    isValid = ifExplorationThenUsers(benchFlowTest, validationErrorMessages)
    // check if load that users are specified
    isValid = ifLoadGoalThenUsers(benchFlowTest, validationErrorMessages) && isValid
    // check that exploration space is defined if such goal
    isValid = ifExplorationThenExplorationSpace(benchFlowTest, validationErrorMessages) && isValid
    // check that at least one workload is defined
    isValid = workloadIsDefined(benchFlowTest, validationErrorMessages) && isValid

    isValid
  }

  private def ifExplorationThenUsers(benchFlowTest: BenchFlowTest, validationErrorMessages: StringBuilder): Boolean = {

    var isValid = true

    if (benchFlowTest.configuration.goal.goalType != LOAD && benchFlowTest.configuration.users.isEmpty) {
      benchFlowTest.configuration.goal.explorationSpace match {
        case Some(explorationSpace) => explorationSpace.workload match {
          case Some(workload) => if (workload.users.isEmpty) {
            isValid = false
            validationErrorMessages.append(userMissing)
          }
          case None =>
            isValid = false
            validationErrorMessages.append(userMissing)
        }
        case None =>
          isValid = false
          validationErrorMessages.append(userMissing)
      }
    }

    isValid
  }

  private def ifLoadGoalThenUsers(benchFlowTest: BenchFlowTest, validationErrorMessages: StringBuilder): Boolean = {
    var isValid = true

    if (benchFlowTest.configuration.goal.goalType == LOAD && benchFlowTest.configuration.users.isEmpty) {
      isValid = false
      validationErrorMessages.append(userMissing)
    }

    isValid

  }

  def ifExplorationThenExplorationSpace(benchFlowTest: BenchFlowTest, validationErrorMessages: StringBuilder): Boolean = {

    var isValid = true

    if (benchFlowTest.configuration.goal.goalType != LOAD && benchFlowTest.configuration.goal.explorationSpace.isEmpty) {
      isValid = false
      validationErrorMessages.append("Missing exploration space specification. ")
    }

    isValid
  }

  def workloadIsDefined(benchFlowTest: BenchFlowTest, validationErrorMessages: StringBuilder): Boolean = {

    var isValid = true

    if (benchFlowTest.workload.isEmpty) {
      isValid = false
      validationErrorMessages.append("Missing workload specification. ")
    }

    isValid

  }

}
