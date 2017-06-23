package cloud.benchflow.dsl.definition.configuration.terminationcriteria

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowTestTerminationCriteriaYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-17
 */
class BenchFlowTestTerminationCriteriaTest extends JUnitSuite {

  private val completeTerminationCriteriaYaml: String =
    """
      |test:
      | max_time: 1h
      |experiment:
      | type: fixed
      | number_of_trials: 5
    """.stripMargin

  @Test def completeTerminationCriteria(): Unit = {

    val terminationCriteria = completeTerminationCriteriaYaml.parseYaml.convertTo[Try[BenchFlowTestTerminationCriteria]]

    Assert.assertTrue(terminationCriteria.isSuccess)

  }

}
