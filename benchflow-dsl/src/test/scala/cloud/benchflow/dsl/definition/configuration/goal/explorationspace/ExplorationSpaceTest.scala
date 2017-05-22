package cloud.benchflow.dsl.definition.configuration.goal.explorationspace

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.ExplorationSpaceYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
class ExplorationSpaceTest extends JUnitSuite {

  private val workloadUsersExplorationSpaceYaml: String =
    """workload:
      | users:
      |   values: [1,2,10,32]
    """.stripMargin

  private val servicesExplorationSpaceYaml: String =
    """some_service:
      |  resources:
      |    memory:
      |      values: [1g,2g,10g,32g]
      |  environment:
      |    AN_ENUM : [value1, value2, value3]
    """.stripMargin

  @Test def workloadUserExplorationSpace(): Unit = {

    val triedExplorationSpace = workloadUsersExplorationSpaceYaml.parseYaml.convertTo[Try[ExplorationSpace]]

    Assert.assertTrue(triedExplorationSpace.isSuccess)

    val explorationSpaceYaml = triedExplorationSpace.get.toYaml

    Assert.assertTrue(explorationSpaceYaml.prettyPrint.contains("workload"))

  }

  @Test def servicesExplorationSpace(): Unit = {

    val triedExplorationSpace = servicesExplorationSpaceYaml.parseYaml.convertTo[Try[ExplorationSpace]]

    Assert.assertTrue(triedExplorationSpace.isSuccess)

    val explorationSpaceYaml = triedExplorationSpace.get.toYaml

    Assert.assertTrue(explorationSpaceYaml.prettyPrint.contains("AN_ENUM"))

  }

}
