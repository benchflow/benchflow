package cloud.benchflow.dsl.definition.configuration

import cloud.benchflow.dsl.definition.configuration.goal.Goal
import cloud.benchflow.dsl.definition.configuration.strategy.ExplorationStrategy
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowTestTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecution

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
case class BenchFlowTestConfiguration(
  goal: Goal,
  users: Option[Int],
  workloadExecution: WorkloadExecution,
  strategy: Option[ExplorationStrategy],
  terminationCriteria: BenchFlowTestTerminationCriteria)
