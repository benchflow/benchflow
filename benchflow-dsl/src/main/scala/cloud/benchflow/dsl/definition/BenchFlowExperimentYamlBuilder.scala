package cloud.benchflow.dsl.definition

import cloud.benchflow.dsl.definition.BenchFlowExperimentYamlProtocol._
import net.jcazevedo.moultingyaml._

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
class BenchFlowExperimentYamlBuilder(experiment: BenchFlowExperiment) {

  def numUsers(numUsers: Int): BenchFlowExperimentYamlBuilder = {

    val newConfiguration = experiment.configuration.copy(Some(numUsers))

    val newExperiment = experiment.copy(configuration = newConfiguration)

    new BenchFlowExperimentYamlBuilder(newExperiment)
  }

  def build(): String = {
    experiment.toYaml.asYamlObject.prettyPrint
  }

}
