package cloud.benchflow.dsl.definition.datacollection.clientside.faban

import cloud.benchflow.dsl.definition.datacollection.clientside.ClientSideConfigurationYamlProtocol
import cloud.benchflow.dsl.definition.datacollection.clientside.ClientSideConfigurationYamlProtocol.FabanKey
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation }
import cloud.benchflow.dsl.definition.types.time.Time
import cloud.benchflow.dsl.definition.types.time.TimeYamlProtocol._
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 12.03.17.
 */
object FabanConfigurationYamlProtocol extends DefaultYamlProtocol {

  val MaxRunTimeKey = YamlString("max_run_time")
  val IntervalKey = YamlString("interval")
  val WorkloadKey = YamlString("workload")

  val Level = s"${ClientSideConfigurationYamlProtocol.Level}.${FabanKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object FabanConfigurationReadFormat extends YamlFormat[Try[FabanConfiguration]] {

    override def read(yaml: YamlValue): Try[FabanConfiguration] = {

      val yamlObject = yaml.asYamlObject

      for {

        maxRunTime <- deserializationHandler(
          yamlObject.getFields(MaxRunTimeKey).headOption.map(_.convertTo[Try[Time]].get),
          keyString(MaxRunTimeKey))

        interval <- deserializationHandler(
          yamlObject.getFields(IntervalKey).headOption.map(_.convertTo[Try[Time]].get),
          keyString(IntervalKey))
        workload <- deserializationHandler(
          yamlObject.getFields(WorkloadKey).headOption.map(_.convertTo[Map[String, Try[Time]]].mapValues(_.get)),
          keyString(WorkloadKey))

      } yield FabanConfiguration(maxRunTime = maxRunTime, interval = interval, workload = workload)

    }

    override def write(obj: Try[FabanConfiguration]): YamlValue = unsupportedWriteOperation
  }

  implicit object FabanConfigurationWriteFormat extends YamlFormat[FabanConfiguration] {

    override def write(obj: FabanConfiguration): YamlValue = YamlObject {

      Map[YamlValue, YamlValue]() ++
        obj.maxRunTime.map(key => MaxRunTimeKey -> key.toYaml) ++
        obj.interval.map(key => IntervalKey -> key.toYaml) ++
        obj.workload.map(key => WorkloadKey -> key.toYaml)

    }

    override def read(yaml: YamlValue): FabanConfiguration = unsupportedReadOperation
  }

}
