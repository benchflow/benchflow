package cloud.benchflow.dsl.definition.configuration

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowExperimentTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecution

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
case class BenchFlowExperimentConfiguration(
  users: Option[Int],
  workloadExecution: Option[WorkloadExecution],
  terminationCriteria: Option[BenchFlowExperimentTerminationCriteria])
