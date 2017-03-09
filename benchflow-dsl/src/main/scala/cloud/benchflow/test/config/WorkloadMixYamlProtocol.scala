package cloud.benchflow.test.config

import net.jcazevedo.moultingyaml._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 21/07/16.
  */
trait WorkloadMixYamlProtocol extends DefaultYamlProtocol {

  implicit object MatrixMixRowYamlFormat extends YamlFormat[MatrixMixRow] {
    override def write(matrixMixRow: MatrixMixRow): YamlValue = matrixMixRow.row.toYaml

    override def read(yaml: YamlValue): MatrixMixRow = {
      yaml.asInstanceOf[YamlArray] match {
        case YamlArray(elems) => MatrixMixRow(elems.map(_.convertTo[Double]))
        case _ => throw new DeserializationException("Incorrect format for matrix mix row")
      }
    }
  }

  implicit object FlatSequenceMixRowYamlFormat extends YamlFormat[FlatSequenceMixRow] {
    override def write(flatSeqMixRow: FlatSequenceMixRow): YamlValue = flatSeqMixRow.row.toYaml

    override def read(yaml: YamlValue): FlatSequenceMixRow = {
      yaml.asInstanceOf[YamlArray] match {
        case YamlArray(elems) => FlatSequenceMixRow(elems.map(_.convertTo[String]))
        case _ => throw new DeserializationException("Incorrect format for flatSequence mix row")
      }
    }
  }

  implicit object FlatSequenceMixYamlFormat extends YamlFormat[FlatSequenceMix] {
    override def write(flatSeqMix: FlatSequenceMix): YamlValue = {
      YamlObject(
        YamlString("flatSequence") ->
          YamlObject(
            YamlString("sequences") -> flatSeqMix.rows.toYaml,
            YamlString("flat") -> flatSeqMix.opsMix.toYaml
          ),
        YamlString("deviation") -> flatSeqMix.deviation.toYaml
      )
    }

    override def read(yaml: YamlValue): FlatSequenceMix = {

      val flatSequenceBody = yaml.asYamlObject.fields.get(YamlString("flatSequence")).get
      val flat = flatSequenceBody.asYamlObject.getFields(
        YamlString("flat")
      ).head match {
        case YamlArray(flatProbs) => flatProbs.map(_.convertTo[Double])
        case _ => throw new DeserializationException("Missing or incorrect format for sequence mix flat probabilities field")
      }

      val sequences = flatSequenceBody.asYamlObject.getFields(
        YamlString("sequences")
      ).head match {
        case YamlArray(rows) => rows.map(_.convertTo[FlatSequenceMixRow])
        case _ => throw new DeserializationException("Missing or incorrect format for sequence mix sequences field")
      }

      val deviation = yaml.asYamlObject.getFields(
        YamlString("deviation")
      ).headOption match {
        case Some(YamlNumber(dev: Double)) => Some(dev)
        case Some(YamlNumber(dev: Int)) => Some(dev.toDouble)
        case Some(YamlNumber(dev: Float)) => Some(dev.toDouble)
        case _ => None
      }

      FlatSequenceMix(deviation = deviation, rows = sequences, opsMix = flat)
    }
  }

  implicit object MatrixMixYamlFormat extends YamlFormat[MatrixMix] {

    override def write(matrixMix: MatrixMix): YamlValue = {
      YamlObject(
        YamlString("matrix") -> matrixMix.rows.toYaml,
        YamlString("deviation") -> matrixMix.deviation.toYaml
      )
    }

    override def read(yaml: YamlValue): MatrixMix = {
      val matrixRows = yaml.asYamlObject.fields.get(YamlString("matrix")).get match {
        case YamlArray(rows) => rows.map(_.convertTo[MatrixMixRow])
        case _ => throw new DeserializationException("Incorrect format for matrix mix")
      }

      val deviation = yaml.asYamlObject.getFields(
        YamlString("deviation")
      ).headOption match {
        case Some(YamlNumber(dev: Double)) => Some(dev)
        case Some(YamlNumber(dev: Int)) => Some(dev.toDouble)
        case Some(YamlNumber(dev: Float)) => Some(dev.toDouble)
        case _ => None
      }

      MatrixMix(matrixRows, deviation)
    }
  }

  implicit object FlatMixYamlFormat extends YamlFormat[FlatMix] {

    override def write(flatMix: FlatMix): YamlValue = {
      YamlObject(
        YamlString("flat") -> flatMix.opsMix.toYaml,
        YamlString("deviation") -> flatMix.deviation.toYaml
      )
    }

    override def read(yaml: YamlValue): FlatMix = {

      val deviation = yaml.asYamlObject.getFields(
        YamlString("deviation")
      ).headOption match {
        case Some(YamlNumber(dev: Double)) => Some(dev)
        case Some(YamlNumber(dev: Int)) => Some(dev.toDouble)
        case Some(YamlNumber(dev: Float)) => Some(dev.toDouble)
        case _ => None
      }

      yaml.asYamlObject.getFields(
        YamlString("flat")
      ).head match {
        case YamlArray(probs) => FlatMix(probs.map(_.convertTo[Double]), deviation)
        case _ => throw new DeserializationException("Unexpected format for flat mix")
      }

    }
  }

  implicit object SequenceMixYamlFormat extends YamlFormat[FixedSequenceMix] {
    override def write(fixedSeq: FixedSequenceMix): YamlValue = {
      YamlObject(
        YamlString("fixedSequence") -> fixedSeq.sequence.toYaml,
        YamlString("deviation") -> fixedSeq.deviation.toYaml
      )
    }

    override def read(yaml: YamlValue): FixedSequenceMix = {

      val deviation = yaml.asYamlObject.getFields(
        YamlString("deviation")
      ).headOption match {
        case Some(YamlNumber(dev: Double)) => Some(dev)
        case Some(YamlNumber(dev: Int)) => Some(dev.toDouble)
        case Some(YamlNumber(dev: Float)) => Some(dev.toDouble)
        case _ => None
      }

      yaml.asYamlObject.getFields(
        YamlString("fixedSequence")
      ).head match {
        case YamlArray(sequence) => FixedSequenceMix(sequence.map(_.convertTo[String]), deviation)
        case _ => throw new DeserializationException("Unexpected format for sequence mix")
      }

    }
  }

  implicit object MixYamlFormat extends YamlFormat[Mix] {
    override def read(yaml: YamlValue): Mix = ???

    override def write(mix: Mix): YamlValue = {
      mix match {
        case matrix: MatrixMix => MatrixMixYamlFormat.write(matrix)
        case flat: FlatMix => FlatMixYamlFormat.write(flat)
        case flatSeq: FlatSequenceMix => FlatSequenceMixYamlFormat.write(flatSeq)
        case fixed: FixedSequenceMix =>  SequenceMixYamlFormat.write(fixed)
      }
    }
  }

}
