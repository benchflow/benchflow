package cloud.benchflow.dsl.definition.workload

import cloud.benchflow.dsl.definition._
import net.jcazevedo.moultingyaml._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 21/07/16.
  */
trait WorkloadModelYamlProtocol extends DefaultYamlProtocol with WorkloadMixYamlProtocol {

  implicit object DriverConfigurationYamlFormat extends YamlFormat[DriverConfiguration] {
    override def write(driverConfig: DriverConfiguration): YamlValue = {
      YamlObject(
        YamlString("mix") -> driverConfig.mix.toYaml,
        YamlString("max90th") -> driverConfig.max90th.toYaml,
        YamlString("popularity") -> driverConfig.popularity.toYaml
      )
    }

    override def read(yaml: YamlValue): DriverConfiguration = {

      def generateMix(yamlMix: YamlValue): Mix = {

        val mixMap = yamlMix.asYamlObject.fields
        Seq("matrix", "flat", "fixedSequence", "flatSequence")
          .map(mixType => mixMap.get(YamlString(mixType))) match {
          case Seq(None, None, Some(seq), None) => yamlMix.convertTo[FixedSequenceMix]
          case Seq(None, Some(flat), None, None) =>  yamlMix.convertTo[FlatMix]
          case Seq(Some(matrix), None, None, None) =>  yamlMix.convertTo[MatrixMix]
          case Seq(None, None, None, Some(flatSequence)) => yamlMix.convertTo[FlatSequenceMix]
        }

      }

      val max90th = yaml.asYamlObject.fields.get(YamlString("max90th")).map(_.convertTo[Double])
      val popularity = yaml.asYamlObject.fields.get(YamlString("popularity")).map(_.convertTo[String].init.toFloat/100)
      val mix = yaml.asYamlObject.fields.get(YamlString("mix")).map(generateMix)

      DriverConfiguration(
        mix = mix,
        max90th = max90th,
        popularity = popularity
      )
    }
  }

}
