package cloud.benchflow.dsl.definition.configuration.goal

import cloud.benchflow.dsl.definition.configuration.goal.GoalYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
class GoalTest extends JUnitSuite {

  private val completeGoalYaml: String =
    """type: exploration
      |
      |exploration_space:
      | workload:
      |   users:
      |     values: [1,2,10,32]
      |
    """.stripMargin

  private val wrongGoalYaml: String =
    """type: some_goal
      |
      |exploration_space:
      | workload:
      |   users:
      |     values: [1,2,10,32]
      |
    """.stripMargin

  @Test def completeGoal(): Unit = {

    val triedGoal = completeGoalYaml.parseYaml.convertTo[Try[Goal]]

    Assert.assertTrue(triedGoal.isSuccess)

    val goalYaml = triedGoal.get.toYaml

    Assert.assertTrue(goalYaml.prettyPrint.contains("exploration_space:"))

    Assert.assertTrue(goalYaml.prettyPrint.contains("type: exploration"))

  }

  @Test def wrongGoal(): Unit = {

    val triedGoal = wrongGoalYaml.parseYaml.convertTo[Try[Goal]]

    Assert.assertTrue(triedGoal.isFailure)

  }

}
