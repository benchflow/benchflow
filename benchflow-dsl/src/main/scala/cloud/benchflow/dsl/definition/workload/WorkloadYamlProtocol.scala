package cloud.benchflow.dsl.definition.workload

import cloud.benchflow.dsl.definition._
import cloud.benchflow.dsl.definition.percent.Percent
import cloud.benchflow.dsl.definition.percent.PercentYamlProtocol._
import cloud.benchflow.dsl.definition.workload.WorkloadMixYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 10.03.17.
  */
object WorkloadYamlProtocol extends DefaultYamlProtocol {

  // TODO - implement me

  val TypeKey = YamlString("type")
  val PopularityKey = YamlString("popularity")
  val InterOperationTimingsKey = YamlString("inter_operation_timings")
  val OperationsKey = YamlString("operations")
  val MixKey = YamlString("mix")
  val FailureCriteriaKey = YamlString("failure_criteria")

  implicit object WorkloadYamlFormat extends YamlFormat[Workload] {
    override def read(yaml: YamlValue): Workload = ???

//    {
//
//      val yamlObject = yaml.asYamlObject
//
//      val workloadType = yamlObject.fields(TypeKey).convertTo[String]
//      val popularity = Option(yamlObject.fields(TypeKey).convertTo[Try[Percent]])
//      val interOperationTimings = yamlObject.fields(TypeKey).convertTo[Option[String]]
//      val operations = yamlObject.fields(OperationsKey).convertTo[List[String]]
//      val mix = yamlObject.fields.get(MixKey).map(generateMix)
//
//      Workload(workloadType = workloadType, popularity = popularity, interOperationTimings = interOperationTimings, operations = operations, mix = mix)
//
//    }

    override def write(obj: Workload): YamlValue = ???

  }

  def generateMix(yamlMix: YamlValue): Mix = {

    // TODO - validate correctness

    val mixMap = yamlMix.asYamlObject.fields
    Seq("matrix", "flat", "fixed_sequence", "flat_sequence")
      .map(mixType => mixMap.get(YamlString(mixType))) match {
      case Seq(None, None, Some(seq), None) => yamlMix.convertTo[FixedSequenceMix]
      case Seq(None, Some(flat), None, None) => yamlMix.convertTo[FlatMix]
      case Seq(Some(matrix), None, None, None) => yamlMix.convertTo[MatrixMix]
      case Seq(None, None, None, Some(flatSequence)) => yamlMix.convertTo[FlatSequenceMix]
    }

  }

}
