package cloud.benchflow.dsl.definition.configuration.terminationcriteria

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.ExperimentTerminationCriteria

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
case class ExperimentOnlyTerminationCriteria(
  experiment: ExperimentTerminationCriteria
)
