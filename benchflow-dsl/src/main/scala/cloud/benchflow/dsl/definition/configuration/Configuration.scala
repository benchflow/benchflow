package cloud.benchflow.dsl.definition.configuration

import cloud.benchflow.dsl.definition.configuration.goal.Goal
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.TerminationCriteria
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecution

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
case class Configuration(
  goal: Goal,
  users: Option[Int],
  workloadExecution: Option[WorkloadExecution],
  strategy: Option[Any], // TODO - define type
  terminationCriteria: Option[TerminationCriteria]
)
