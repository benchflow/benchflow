package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.users

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.users.UserValuesYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{ Assert, Test }
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitSuite

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
class UserValuesTest extends JUnitSuite {

  //TODO: when applying https://github.com/benchflow/benchflow/issues/397#issuecomment-314408192
  //evaluate if it is possible to refactor these tests to avoid duplicates with the behaviour added
  //using the MonotonicPositiveIntValuesRangeYamlProtocol, IntValuesRangeYamlProtocol and
  //ValuesRangeYamlProtocol traits

  @Test def valuesUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |values: [1,2,10,32]
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

  @Test def valuesDecreasingUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |values: [32,10,2,1]
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def valuesNegativeUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |values: [-1,2,10,32]
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [2,10]
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    terminationCriteria.get.values should contain allOf (2, 3, 4, 5, 6, 7, 8, 9, 10)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

  @Test def rangeTooManyUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10,15]
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeDecreasingUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [10,2]
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    terminationCriteria.get.values should contain allOf (10, 9, 8, 7, 6, 5, 4, 3, 2)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

  @Test def rangeNegativeUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [-1,1]
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeHeadZeroUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [0,1]
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeHeadEqualRangeLastUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,1]
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepNoIntegerUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10]
        |step: "+2.5"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepAdditionUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10]
        |step: "+2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    terminationCriteria.get.values should contain allOf (1, 3, 5, 7, 9, 10)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

  @Test def rangeStepAdditionNegativeUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [10,1]
        |step: "+-2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepAdditionIdentityElementUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10]
        |step: "+0"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepDivergeUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [10,1]
        |step: "+2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepNegativeUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [-10,1]
        |step: "+2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepMultiplicationUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10]
        |step: "*2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    terminationCriteria.get.values should contain allOf (1, 2, 4, 8, 10)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

  @Test def rangeHeadLowerThanLastMultiplicationUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [20,2]
        |step: "*2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepMultiplicationNegativeUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10]
        |step: "*-2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepMultiplicationIdentityElementUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10]
        |step: "*1"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeHeadMultiplicationIdentityElementsUserExplorationSpaceTest(): Unit = {

    val rangeHeadZeroYaml: String =
      """
        |range: [0,20]
        |step: "*2"
      """.stripMargin

    val terminationCriteriaZero = rangeHeadZeroYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteriaZero.isFailure)

  }

  @Test def rangeStepMultiplicationAbsorbtionElementUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10]
        |step: "*0"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepPowerUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [2,20]
        |step: "^2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    terminationCriteria.get.values should contain allOf (2, 4, 16, 20)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

  @Test def rangeHeadLowerThanLastPowerUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [20,2]
        |step: "^2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepPowerNegativeUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [2,20]
        |step: "^-2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeHeadPowerIdentityElementsUserExplorationSpaceTest(): Unit = {

    val rangeHeadZeroYaml: String =
      """
        |range: [0,20]
        |step: "^2"
      """.stripMargin

    val terminationCriteriaZero = rangeHeadZeroYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteriaZero.isFailure)

    val rangeHeadOneYaml: String =
      """
        |range: [1,20]
        |step: "^2"
      """.stripMargin

    val terminationCriteriaOne = rangeHeadOneYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteriaOne.isFailure)

  }

  @Test def rangeStepPowerIdentityElementsUserExplorationSpaceTest(): Unit = {

    val rangeStepPowerZeroYaml: String =
      """
        |range: [2,20]
        |step: "^0"
      """.stripMargin

    val terminationCriteriaZero = rangeStepPowerZeroYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteriaZero.isFailure)

    val rangeStepPowerOneYaml: String =
      """
        |range: [2,20]
        |step: "^1"
      """.stripMargin

    val terminationCriteria = rangeStepPowerOneYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepOperationNotSupportedUserExplorationSpaceTest(): Unit = {

    val rangeStepPowerZeroYaml: String =
      """
        |range: [2,20]
        |step: "&2"
      """.stripMargin

    val terminationCriteriaZero = rangeStepPowerZeroYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteriaZero.isFailure)

  }

}
