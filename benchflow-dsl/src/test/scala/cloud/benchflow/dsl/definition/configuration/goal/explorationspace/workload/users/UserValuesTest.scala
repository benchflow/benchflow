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
        |range: [1,10]
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    terminationCriteria.get.values should contain allOf (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

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
        |range: [10,1]
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeNegativeUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [-1,1]
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

  @Test def rangeStepPowerNegativeUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [2,20]
        |step: "^-2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[UserValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepPowerIdentityElementUserExplorationSpaceTest(): Unit = {

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

}
