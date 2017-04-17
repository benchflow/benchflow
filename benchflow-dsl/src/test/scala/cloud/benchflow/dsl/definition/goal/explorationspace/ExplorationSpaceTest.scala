package cloud.benchflow.dsl.definition.goal.explorationspace

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.ExplorationSpace
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.ExplorationSpaceYamlProtocol._
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite
import net.jcazevedo.moultingyaml._

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
class ExplorationSpaceTest extends JUnitSuite {

  private val completeExplorationSpaceYaml: String =
    """workload:
      | users:
      |   values: [1,2,10,32]
    """.stripMargin

  @Test def completeUserExplorationSpace(): Unit = {

    val triedExplorationSpace = completeExplorationSpaceYaml.parseYaml.convertTo[Try[ExplorationSpace]]

    Assert.assertTrue(triedExplorationSpace.isSuccess)

    val explorationSpaceYaml = triedExplorationSpace.get.toYaml

    Assert.assertTrue(explorationSpaceYaml.prettyPrint.contains("workload"))

  }

}
