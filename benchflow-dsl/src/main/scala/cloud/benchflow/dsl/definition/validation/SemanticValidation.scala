package cloud.benchflow.dsl.definition.validation

import cloud.benchflow.dsl.definition.BenchFlowTest
import cloud.benchflow.dsl.definition.configuration.goal.goaltype.GoalType.LOAD

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-09-18
 */
object SemanticValidation {

  private val userMissing = "Missing user specification. "

  def validateTest(benchFlowTest: BenchFlowTest): (Boolean, StringBuilder) = {

    val validationErrorMessages = new StringBuilder

    validationErrorMessages.append("Semantic validation failed. ")
    var isValid = true

    // check that number of users is defined if exploration goal
    isValid = ifExplorationThenUsers(benchFlowTest, validationErrorMessages)
    // check if load that users are specified
    isValid = ifLoadGoalThenUsers(benchFlowTest, validationErrorMessages) && isValid
    // check that exploration space is defined if such goal
    isValid = ifExplorationThenExplorationSpace(benchFlowTest, validationErrorMessages) && isValid
    // check that at least one workload is defined
    isValid = workloadIsDefined(benchFlowTest, validationErrorMessages) && isValid

    (isValid, validationErrorMessages)

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

  private def ifExplorationThenExplorationSpace(benchFlowTest: BenchFlowTest, validationErrorMessages: StringBuilder): Boolean = {

    var isValid = true

    if (benchFlowTest.configuration.goal.goalType != LOAD && benchFlowTest.configuration.goal.explorationSpace.isEmpty) {
      isValid = false
      validationErrorMessages.append("Missing exploration space specification. ")
    }

    isValid
  }

  private def workloadIsDefined(benchFlowTest: BenchFlowTest, validationErrorMessages: StringBuilder): Boolean = {

    var isValid = true

    if (benchFlowTest.workload.isEmpty) {
      isValid = false
      validationErrorMessages.append("Missing workload specification. ")
    }

    isValid

  }

}
