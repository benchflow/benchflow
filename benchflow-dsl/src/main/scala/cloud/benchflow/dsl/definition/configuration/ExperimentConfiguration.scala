package cloud.benchflow.dsl.definition.configuration

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.ExperimentOnlyTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecution

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
case class ExperimentConfiguration(
  users: Option[Int],
  workloadExecution: Option[WorkloadExecution],
  terminationCriteria: Option[ExperimentOnlyTerminationCriteria]
)
