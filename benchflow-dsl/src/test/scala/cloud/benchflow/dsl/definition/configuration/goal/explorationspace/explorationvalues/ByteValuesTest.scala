package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.explorationvalues

import net.jcazevedo.moultingyaml._
import org.junit.{ Assert, Test }
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitSuite
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.explorationvalues.ExplorationValuesBytesYamlProtocol._

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
class ByteValuesTest extends JUnitSuite {

  private val valuesYaml: String =
    """
      |values: [1m,2m,10m,32m]
    """.stripMargin

  private val rangeYaml: String =
    """
      |range: [1g,10g]
    """.stripMargin

  @Test def valuesUserExplorationSpaceTest(): Unit = {

    val terminationCriteria = valuesYaml.parseYaml.convertTo[Try[ByteValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

  @Test def rangeUserExplorationSpaceTest(): Unit = {

    val terminationCriteria = rangeYaml.parseYaml.convertTo[Try[ByteValues]]

    Assert.assertTrue(terminationCriteria.isSuccess)

    terminationCriteria.get.values.map(_.toString) should contain allOf ("1g", "2g", "3g", "4g", "5g", "6g", "7g", "8g", "9g", "10g")

    val terminationCriteriaYaml = terminationCriteria.get.toYaml

    Assert.assertTrue(terminationCriteriaYaml.prettyPrint.contains("values"))

  }

}
