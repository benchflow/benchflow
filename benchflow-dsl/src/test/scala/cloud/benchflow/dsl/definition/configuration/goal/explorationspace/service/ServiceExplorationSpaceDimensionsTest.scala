package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.service

import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite
import net.jcazevedo.moultingyaml._
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.service.ServiceExplorationSpaceYamlProtocol._

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
class ServiceExplorationSpaceDimensionsTest extends JUnitSuite {

  private val serviceExplorationSpace: String =
    """
      |resources:
      |  memory:
      |    range: [1g,5g]
      |environment:
      |  AN_ENUM: [value1, value2, value3]
    """.stripMargin

  @Test def completeUserExplorationSpace(): Unit = {

    val workloadExplorationSpace = serviceExplorationSpace.parseYaml.convertTo[Try[ServiceExplorationSpace]]

    Assert.assertTrue(workloadExplorationSpace.isSuccess)

    val workloadExplorationSpaceYaml = workloadExplorationSpace.get.toYaml

    Assert.assertTrue(workloadExplorationSpaceYaml.prettyPrint.contains("memory"))

    Assert.assertTrue(workloadExplorationSpaceYaml.prettyPrint.contains("AN_ENUM"))

  }

}
