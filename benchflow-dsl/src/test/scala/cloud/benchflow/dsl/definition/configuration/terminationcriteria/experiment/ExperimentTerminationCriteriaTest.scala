package cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment

import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite
import net.jcazevedo.moultingyaml._
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.ExperimentTerminationCriteriaYamlProtocol._

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-17
 */
class ExperimentTerminationCriteriaTest extends JUnitSuite {

  private val fixedterminationCriteriaYaml: String =
    """type: fixed
      |number: 5""".stripMargin

  @Test def fixedTerminationCriteria(): Unit = {

    val experimentTerminationCriteria: Try[ExperimentTerminationCriteria] = fixedterminationCriteriaYaml.parseYaml.convertTo[Try[ExperimentTerminationCriteria]]

    Assert.assertTrue(experimentTerminationCriteria.isSuccess)

  }

}
