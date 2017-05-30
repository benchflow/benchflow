package cloud.benchflow.dsl.definition.configuration.terminationcriteria

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.ExperimentTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.exploration.ExplorationTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.test.TestTerminationCriteria

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
case class BenchFlowTestTerminationCriteria(
  exploration: Option[ExplorationTerminationCriteria],
  test: TestTerminationCriteria,
  experiment: ExperimentTerminationCriteria)
