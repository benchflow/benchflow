package cloud.benchflow.dsl.definition.goal

import cloud.benchflow.dsl.definition.configuration.goal.Goal
import cloud.benchflow.dsl.definition.configuration.goal.GoalYamlProtocol._
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite
import net.jcazevedo.moultingyaml._

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
class GoalTest extends JUnitSuite {

  private val completeGoalYaml: String =
    """type: load
      |
      |exploration_space:
      | workload:
      |   users:
      |     values: [1,2,10,32]
    """.stripMargin

  @Test def completeGoal(): Unit = {

    val triedGoal = completeGoalYaml.parseYaml.convertTo[Try[Goal]]

    Assert.assertTrue(triedGoal.isSuccess)

    val goalYaml = triedGoal.get.toYaml

    Assert.assertTrue(goalYaml.prettyPrint.contains("exploration_space:"))

    Assert.assertTrue(goalYaml.prettyPrint.contains("type:"))

  }

}