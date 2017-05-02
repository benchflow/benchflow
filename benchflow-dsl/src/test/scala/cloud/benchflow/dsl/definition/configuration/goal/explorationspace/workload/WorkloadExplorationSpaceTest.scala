package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.WorkloadExplorationSpaceYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
class WorkloadExplorationSpaceTest extends JUnitSuite {

  private val completeWorkloadExplorationSpaceYaml: String =
    """users:
      | values: [1,2,10,32]
    """.stripMargin

  @Test def completeUserExplorationSpace(): Unit = {

    val workloadExplorationSpace = completeWorkloadExplorationSpaceYaml.parseYaml.convertTo[Try[WorkloadExplorationSpace]]

    Assert.assertTrue(workloadExplorationSpace.isSuccess)

    val workloadExplorationSpaceYaml = workloadExplorationSpace.get.toYaml

    Assert.assertTrue(workloadExplorationSpaceYaml.prettyPrint.contains("users"))

  }

}
