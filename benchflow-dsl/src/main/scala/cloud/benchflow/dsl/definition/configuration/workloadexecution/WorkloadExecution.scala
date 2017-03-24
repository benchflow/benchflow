package cloud.benchflow.dsl.definition.configuration.workloadexecution

import cloud.benchflow.dsl.definition.types.time.Time

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
case class WorkloadExecution(
  rampUp: Time,
  steadyState: Time,
  rampDown: Time
)
