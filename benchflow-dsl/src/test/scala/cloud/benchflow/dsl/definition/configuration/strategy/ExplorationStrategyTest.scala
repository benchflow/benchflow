package cloud.benchflow.dsl.definition.configuration.strategy

import cloud.benchflow.dsl.definition.configuration.strategy.ExplorationStrategyYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-21
 */
class ExplorationStrategyTest extends JUnitSuite {

  private val strategySelectionCompleteYaml: String =
    """
      |selection: complete
    """.stripMargin

  private val strategyAllYaml: String =
    """
      |selection: complete
      |regression: mars
      |validation: random-validation-set
    """.stripMargin

  @Test def strategySelectionComplete(): Unit = {

    val triedStrategy = strategySelectionCompleteYaml.parseYaml.convertTo[Try[ExplorationStrategy]]

    Assert.assertTrue(triedStrategy.isSuccess)

    val selectionCompleteYaml = triedStrategy.get.toYaml

    Assert.assertTrue(selectionCompleteYaml.prettyPrint.contains("selection: complete"))

  }

  @Test def strategyAll(): Unit = {

    val triedStrategy = strategyAllYaml.parseYaml.convertTo[Try[ExplorationStrategy]]

    Assert.assertTrue(triedStrategy.isSuccess)

    val selectionCompleteYaml = triedStrategy.get.toYaml

    Assert.assertTrue(selectionCompleteYaml.prettyPrint.contains("selection: complete"))
    Assert.assertTrue(selectionCompleteYaml.prettyPrint.contains("regression: mars"))
    Assert.assertTrue(selectionCompleteYaml.prettyPrint.contains("validation: random-validation-set"))

  }

}
