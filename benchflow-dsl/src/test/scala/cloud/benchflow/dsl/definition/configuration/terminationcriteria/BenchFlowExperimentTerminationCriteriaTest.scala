package cloud.benchflow.dsl.definition.configuration.terminationcriteria

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowExperimentTerminationCriteriaYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-17
 */
class BenchFlowExperimentTerminationCriteriaTest extends JUnitSuite {

  private val completeTerminationCriteriaYaml: String =
    """
      |experiment:
      | type: fixed
      | number_of_trials: 5
    """.stripMargin

  @Test def completeTerminationCriteria(): Unit = {

    val terminationCriteria = completeTerminationCriteriaYaml.parseYaml.convertTo[Try[BenchFlowExperimentTerminationCriteria]]

    Assert.assertTrue(terminationCriteria.isSuccess)

  }

}
