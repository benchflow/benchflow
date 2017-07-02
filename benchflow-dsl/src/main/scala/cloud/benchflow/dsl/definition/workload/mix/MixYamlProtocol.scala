package cloud.benchflow.dsl.definition.workload.mix

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation}
import cloud.benchflow.dsl.definition.types.percent.Percent
import cloud.benchflow.dsl.definition.types.percent.PercentYamlProtocol._
import cloud.benchflow.dsl.definition.workload.WorkloadYamlProtocol
import cloud.benchflow.dsl.definition.workload.WorkloadYamlProtocol.MixKey
import net.jcazevedo.moultingyaml._

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 14.03.17.
 */
object MixYamlProtocol extends DefaultYamlProtocol {

  val MaxDeviationKey = YamlString("max_deviation")
  val FixedSequenceKey = YamlString("fixed_sequence")
  val FlatKey = YamlString("flat")
  val SequencesKey = YamlString("sequences")
  val MatrixKey = YamlString("matrix")

  val Level = s"${WorkloadYamlProtocol.Level}.${MixKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object MixReadFormat extends YamlFormat[Try[Mix]] {

    override def read(yaml: YamlValue): Try[Mix] = {

      val yamlObject = yaml.asYamlObject

      for {

        maxDeviation <- deserializationHandler(
          yamlObject.getFields(MaxDeviationKey).headOption.map(_.convertTo[Try[Percent]].get),
          keyString(MaxDeviationKey))

        // TODO - test this properly. The order is important so SequenceyKey must come before FlatKey

        mix <- deserializationHandler(
          yamlObject match {
            case _ if yamlObject.getFields(FixedSequenceKey).nonEmpty =>
              FixedSequenceMix(yamlObject.getFields(FixedSequenceKey).headOption.map(_.convertTo[Seq[String]]).get)

            case _ if yamlObject.getFields(SequencesKey).nonEmpty => FlatSequenceMix(
              yamlObject.getFields(FlatKey).headOption.map(_.convertTo[Seq[Try[Percent]]].map(_.get)).get,
              yamlObject.getFields(SequencesKey).headOption.map(_.convertTo[Seq[Seq[String]]]).get)

            case _ if yamlObject.getFields(FlatKey).nonEmpty =>
              FlatMix(yamlObject.getFields(FlatKey).headOption.map(_.convertTo[Seq[Try[Percent]]].map(_.get)).get)

            case _ if yamlObject.getFields(MatrixKey).nonEmpty =>
              MatrixMix(yamlObject.getFields(MatrixKey).headOption.map(_.convertTo[Seq[Seq[Try[Percent]]]].map(_.map(_.get))).get)

            // TODO - handle case when a valid key is missing
          },
          keyString(YamlString("mix"))) // generic string since we don't know exact key

      } yield Mix(maxDeviation = maxDeviation, mix = mix)

    }

    override def write(obj: Try[Mix]): YamlValue = unsupportedWriteOperation

  }

  implicit object MixWriteFormat extends YamlFormat[Mix] {

    override def write(obj: Mix): YamlValue = YamlObject {

      // TODO - test this properly

      Map[YamlValue, YamlValue]() ++ {
        obj.mix match {

          case fixed: FixedSequenceMix => List(FixedSequenceKey -> fixed.mix.toYaml)
          case flat: FlatMix => List(FlatKey -> flat.mix.toYaml)
          case flatSeq: FlatSequenceMix => List(FlatKey -> flatSeq.mix.toYaml, SequencesKey -> flatSeq.sequences.toYaml)
          case matrix: MatrixMix => List(MatrixKey -> matrix.mix.toYaml)

        }
      } ++ obj.maxDeviation.map(key => MaxDeviationKey -> key.toYaml)

    }

    override def read(yaml: YamlValue): Mix = unsupportedReadOperation

  }

}
