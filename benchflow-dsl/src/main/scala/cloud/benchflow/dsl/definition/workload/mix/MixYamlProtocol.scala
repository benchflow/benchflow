package cloud.benchflow.dsl.definition.workload.mix

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.deserializationHandler
import cloud.benchflow.dsl.definition.types.percent.Percent
import cloud.benchflow.dsl.definition.types.percent.PercentYamlProtocol._
import net.jcazevedo.moultingyaml._

import scala.util.Try

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  *         Created on 21/07/16.
  */
object MixYamlProtocol extends DefaultYamlProtocol {

  val MaxDeviationKey = YamlString("max_deviation")
  val FixedSequenceKey = YamlString("fixed_sequence")
  val FlatKey = YamlString("flat")
  val SequencesKey = YamlString("sequences")
  val MatrixKey = YamlString("matrix")

  private def keyString(key: YamlString) = "workload.mix." + key.value

  implicit object MixYamlFormat extends YamlFormat[Try[Mix]] {

    override def read(yaml: YamlValue): Try[Mix] = {

      val yamlObject = yaml.asYamlObject

      for {

        maxDeviation <- deserializationHandler(
          yamlObject.getFields(MaxDeviationKey).headOption.map(_.convertTo[Try[Percent]].get),
          keyString(MaxDeviationKey)
        )

        // TODO - test this properly. The order is important so SequenceyKey must come before FlatKey

        mix <- deserializationHandler(
          yamlObject match {
            case _ if yamlObject.getFields(FixedSequenceKey).nonEmpty =>
              FixedSequenceMix(yamlObject.getFields(FixedSequenceKey).headOption.map(_.convertTo[Seq[String]]).get)

            case _ if yamlObject.getFields(SequencesKey).nonEmpty => FlatSequenceMix(
              yamlObject.getFields(FlatKey).headOption.map(_.convertTo[Seq[Try[Percent]]].map(_.get)).get,
              yamlObject.getFields(SequencesKey).headOption.map(_.convertTo[Seq[Seq[String]]]).get
            )

            case _ if yamlObject.getFields(FlatKey).nonEmpty =>
              FlatMix(yamlObject.getFields(FlatKey).headOption.map(_.convertTo[Seq[Try[Percent]]].map(_.get)).get)

            case _ if yamlObject.getFields(MatrixKey).nonEmpty =>
              MatrixMix(yamlObject.getFields(MatrixKey).headOption.map(_.convertTo[Seq[Seq[Try[Percent]]]].map(_.map(_.get))).get)

            // TODO - handle case when a valid key is missing
          },
          keyString(MaxDeviationKey)
        )

      } yield Mix(maxDeviation = maxDeviation, mix = mix)

    }

    override def write(obj: Try[Mix]): YamlValue = {

      // TODO - test this properly

      val mix = obj.get

      var map = Map[YamlValue, YamlValue]()

      if (mix.maxDeviation.isDefined)
        map += MaxDeviationKey -> Try(mix.maxDeviation.get).toYaml

      mix.mix match {

        case fixed: FixedSequenceMix =>
          map += FixedSequenceKey -> fixed.mix.toYaml

        case flat: FlatMix =>
          map += FlatKey -> flat.mix.map(v => Try(v)).toYaml

        case flatSeq: FlatSequenceMix =>
          map += FlatKey -> flatSeq.mix.map(v => Try(v)).toYaml
          map += SequencesKey -> flatSeq.sequences.toYaml

        case matrix: MatrixMix =>
          map += MatrixKey -> matrix.mix.map(seq => seq.map(v => Try(v))).toYaml

      }

      YamlObject(map)

    }

  }

}
