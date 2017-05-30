package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.explorationvalues

import net.jcazevedo.moultingyaml._
import org.junit.{ Assert, Test }
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitSuite
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.explorationvalues.ExplorationValuesIntYamlProtocol._

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
class IntValuesTest extends JUnitSuite {

  private val valuesUserExplorationSpaceYaml: String =
    """
      |values: [1,2,10,32]
    """.stripMargin

  private val rangeUserExplorationSpaceYaml: String =
    """
      |range: [1,10]
    """.stripMargin

  @Test def valuesUserExplorationSpaceTest(): Unit = {

    val terminationCriteria = valuesUserExplorationSpaceYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

  @Test def rangeUserExplorationSpaceTest(): Unit = {

    val terminationCriteria = rangeUserExplorationSpaceYaml.parseYaml.convertTo[Try[IntValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    terminationCriteria.get.values should contain allOf (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

}
