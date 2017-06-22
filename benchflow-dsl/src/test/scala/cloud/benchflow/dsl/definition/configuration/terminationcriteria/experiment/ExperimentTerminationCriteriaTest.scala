package cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.ExperimentTerminationCriteriaYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

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

    val experimentTerminationCriteria = fixedterminationCriteriaYaml.parseYaml.convertTo[Try[ExperimentTerminationCriteria]]

    Assert.assertTrue(experimentTerminationCriteria.isSuccess)

    val experimentTerminationCriteriaYaml = experimentTerminationCriteria.get.toYaml

    Assert.assertTrue(experimentTerminationCriteriaYaml.prettyPrint.contains("type: fixed"))

  }

  @Test def invalidTerminationCriteriaType(): Unit = {

    val invalidTerminationCriteriaTypeYaml: String =
      """type: invalid
        |number: 5""".stripMargin

    val experimentTerminationCriteria = invalidTerminationCriteriaTypeYaml.parseYaml.convertTo[Try[ExperimentTerminationCriteria]]

    Assert.assertTrue(experimentTerminationCriteria.isFailure)

  }

}
