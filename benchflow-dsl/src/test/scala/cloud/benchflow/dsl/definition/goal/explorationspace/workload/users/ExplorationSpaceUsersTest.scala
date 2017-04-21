package cloud.benchflow.dsl.definition.goal.explorationspace.workload.users

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.users.ExplorationSpaceUsers
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.users.ExplorationSpaceUsersYamlProtocol._
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite
import net.jcazevedo.moultingyaml._

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
class ExplorationSpaceUsersTest extends JUnitSuite {

  private val completeUserExplorationSpaceYaml: String =
    """
      |values: [1,2,10,32]
    """.stripMargin

  @Test def completeUserExplorationSpace(): Unit = {

    val terminationCriteria = completeUserExplorationSpaceYaml.parseYaml.convertTo[Try[ExplorationSpaceUsers]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

}
