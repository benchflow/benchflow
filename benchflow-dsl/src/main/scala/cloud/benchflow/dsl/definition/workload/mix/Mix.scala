package cloud.benchflow.dsl.definition.workload.mix

import cloud.benchflow.dsl.definition.types.percent.Percent

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 15.03.17.
 */
case class Mix(maxDeviation: Option[Percent], mix: MixType)

