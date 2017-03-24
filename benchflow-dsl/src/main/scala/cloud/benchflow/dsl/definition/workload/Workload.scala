package cloud.benchflow.dsl.definition.workload

import cloud.benchflow.dsl.definition.types.percent.Percent
import cloud.benchflow.dsl.definition.workload.mix.Mix

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 10.03.17.
 */
case class Workload(
  workloadType: String, // TODO - define type?
  popularity: Option[Percent],
  interOperationTimings: Option[String], // TODO - define type
  operations: List[String], // TODO - adjust for http type
  mix: Option[Mix]
)
