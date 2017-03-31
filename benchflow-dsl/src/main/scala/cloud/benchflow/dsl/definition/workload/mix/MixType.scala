package cloud.benchflow.dsl.definition.workload.mix

import cloud.benchflow.dsl.definition.types.percent.Percent

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-03-17
 */
abstract class MixType
case class FixedSequenceMix(mix: Seq[String]) extends MixType
case class FlatMix(mix: Seq[Percent]) extends MixType
case class FlatSequenceMix(mix: Seq[Percent], sequences: Seq[Seq[String]]) extends MixType
case class MatrixMix(mix: Seq[Seq[Percent]]) extends MixType
