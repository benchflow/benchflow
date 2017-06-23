package cloud.benchflow.dsl.definition.workload

import cloud.benchflow.dsl.definition.BenchFlowTestYamlProtocol
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation }
import cloud.benchflow.dsl.definition.types.percent.Percent
import cloud.benchflow.dsl.definition.types.percent.PercentYamlProtocol._
import cloud.benchflow.dsl.definition.workload.drivertype.DriverType
import cloud.benchflow.dsl.definition.workload.drivertype.DriverTypeYamlProtocol._
import cloud.benchflow.dsl.definition.workload.interoperationtimingstype.InterOperationsTimingType
import cloud.benchflow.dsl.definition.workload.interoperationtimingstype.InterOperationsTimingTypeYamlProtocol._
import cloud.benchflow.dsl.definition.workload.mix.Mix
import cloud.benchflow.dsl.definition.workload.mix.MixYamlProtocol._
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 10.03.17.
 */
object WorkloadYamlProtocol extends DefaultYamlProtocol {

  val TypeKey = YamlString("driver_type")
  val PopularityKey = YamlString("popularity")
  val InterOperationTimingsKey = YamlString("inter_operation_timings")
  val OperationsKey = YamlString("operations")
  val MixKey = YamlString("mix")

  val Level = s"${BenchFlowTestYamlProtocol.WorkloadKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object WorkloadReadFormat extends YamlFormat[Try[Workload]] {
    override def read(yaml: YamlValue): Try[Workload] = {

      val yamlObject = yaml.asYamlObject

      for {

        workloadType <- deserializationHandler(
          yamlObject.fields(TypeKey).convertTo[Try[DriverType]].get,
          keyString(TypeKey))

        popularity <- deserializationHandler(
          yamlObject.getFields(PopularityKey).headOption.map(_.convertTo[Try[Percent]].get),
          keyString(PopularityKey))

        interOperationTimings <- deserializationHandler(
          yamlObject.getFields(InterOperationTimingsKey).headOption.map(_.convertTo[Try[InterOperationsTimingType]].get),
          keyString(InterOperationTimingsKey))

        operations <- deserializationHandler(
          yamlObject.fields(OperationsKey).convertTo[List[String]],
          keyString(OperationsKey))

        mix <- deserializationHandler(
          yamlObject.getFields(MixKey).headOption.map(_.convertTo[Try[Mix]].get),
          keyString(MixKey))

      } yield Workload(
        driverType = workloadType,
        popularity = popularity,
        interOperationTimings = interOperationTimings,
        operations = operations,
        mix = mix)

    }

    override def write(obj: Try[Workload]): YamlValue = unsupportedWriteOperation

  }

  implicit object WorkloadWriteFormat extends YamlFormat[Workload] {

    override def write(obj: Workload): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        TypeKey -> obj.driverType.toYaml) ++
        obj.popularity.map(key => PopularityKey -> key.toYaml) ++
        obj.interOperationTimings.map(key => InterOperationTimingsKey -> key.toYaml) +
        (OperationsKey -> obj.operations.toYaml) ++
        obj.mix.map(key => MixKey -> key.toYaml)

    }

    override def read(yaml: YamlValue): Workload = unsupportedReadOperation

  }

}
