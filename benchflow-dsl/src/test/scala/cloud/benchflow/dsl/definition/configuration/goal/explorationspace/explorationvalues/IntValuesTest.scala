package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.explorationvalues

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.explorationvalues.ExplorationValuesIntYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{ Assert, Test }
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitSuite

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
class IntValuesTest extends JUnitSuite {

  @Test def valuesUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |values: [1,2,10,32]
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

  @Test def rangeUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10]
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    terminationCriteria.get.values should contain allOf (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

  @Test def rangeStepAdditionUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10]
        |step: "+2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    terminationCriteria.get.values should contain allOf (1, 3, 5, 7, 9, 10)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

  @Test def rangeStepSubtractionUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [10,1]
        |step: "-2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    terminationCriteria.get.values should contain allOf (10, 8, 6, 4, 2, 1)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

  @Test def rangeStepMultiplicationUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10]
        |step: "*2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[IntValues]]

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

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    terminationCriteria.get.values should contain allOf (1, -2, 4, -8, 10)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

  @Test def rangeStepPowerUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [2,20]
        |step: "^2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[IntValues]]

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

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepAdditionIdentityElementUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10]
        |step: "+0"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepSubtractionIdentityElementUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10]
        |step: "-0"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepMultiplicationIdentityElementUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [1,10]
        |step: "*1"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepPowerIdentityElementUserExplorationSpaceTest(): Unit = {

    val rangeStepPowerZeroYaml: String =
      """
        |range: [2,20]
        |step: "^0"
      """.stripMargin

    val terminationCriteriaZero = rangeStepPowerZeroYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteriaZero.isFailure)

    val rangeStepPowerOneYaml: String =
      """
        |range: [2,20]
        |step: "^1"
      """.stripMargin

    val terminationCriteria = rangeStepPowerOneYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

  @Test def rangeStepDivergeUserExplorationSpaceTest(): Unit = {

    val rangeStepYaml: String =
      """
        |range: [10,1]
        |step: "+2"
      """.stripMargin

    val terminationCriteria = rangeStepYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteria.isFailure)

  }

}
