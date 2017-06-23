package cloud.benchflow.dsl.definition.workload

import cloud.benchflow.dsl.definition.types.percent.Percent
import cloud.benchflow.dsl.definition.workload.drivertype.DriverType
import cloud.benchflow.dsl.definition.workload.interoperationtimingstype.InterOperationsTimingType
import cloud.benchflow.dsl.definition.workload.mix.Mix

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 10.03.17.
 */
case class Workload(
  driverType: DriverType,
  popularity: Option[Percent],
  interOperationTimings: Option[InterOperationsTimingType],
  operations: List[String], // TODO - adjust for http type
  mix: Option[Mix])
