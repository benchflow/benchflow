package cloud.benchflow.dsl.definition.configuration

import org.junit.{ Assert, Test }
import net.jcazevedo.moultingyaml._
import cloud.benchflow.dsl.definition.configuration.BenchFlowTestConfigurationYamlProtocol._

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-22
 */
class BenchFlowTestConfigurationTest {

  val configurationYaml: String =
    """
      |goal:
      |  type: exhaustive_exploration
      |
      |  exploration_space:
      |    camunda:
      |      resources:
      |        memory:
      |          range: [1g, 5g]
      |      environment:
      |        SIZE_OF_THREADPOOL: ["1","2","3","5"]
      |    workload:
      |      users:
      |        values: [5, 10, 15, 20]
      |
      |strategy:
      |  selection: one-at-a-time
      |
      |workload_execution:
      |   ramp_up: 0s
      |   steady_state: 60s
      |   ramp_down: 0s
      |
      |termination_criteria:
      |  test:
      |    max_time: 1h
      |
      |  experiment:
      |    type: fixed
      |    number: 3
    """.stripMargin

  @Test def defaultValuesTest(): Unit = {

    val triedConfiguration = configurationYaml.parseYaml.convertTo[Try[BenchFlowTestConfiguration]]

    Assert.assertTrue(triedConfiguration.isSuccess)

    val convertedconfigurationYaml = triedConfiguration.get.toYaml

    Assert.assertTrue(convertedconfigurationYaml.prettyPrint.contains("settings:"))
    Assert.assertTrue(convertedconfigurationYaml.prettyPrint.contains("stored_knowledge: true"))

  }

}
