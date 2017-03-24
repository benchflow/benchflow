package cloud.benchflow.dsl.definition.datacollection.clientside.faban

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.deserializationHandler
import cloud.benchflow.dsl.definition.types.time.Time
import cloud.benchflow.dsl.definition.types.time.TimeYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue, _}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 12.03.17.
  */
object FabanConfigurationYamlProtocol extends DefaultYamlProtocol {

  val MaxRunTimeKey = YamlString("max_run_time")
  val IntervalKey = YamlString("interval")
  val WorkloadKey = YamlString("workload")

  private def keyString(key: YamlString) = "data_collection.client_side.faban." + key.value

  implicit object FabanConfigurationFormat extends YamlFormat[Try[FabanConfiguration]] {

    override def read(yaml: YamlValue): Try[FabanConfiguration] = {

      val yamlObject = yaml.asYamlObject

      for {

        maxRunTime <- deserializationHandler(
          yamlObject.getFields(MaxRunTimeKey).headOption.map(_.convertTo[Try[Time]].get),
          keyString(MaxRunTimeKey)
        )

        interval <- deserializationHandler(
          yamlObject.getFields(IntervalKey).headOption.map(_.convertTo[Try[Time]].get),
          keyString(IntervalKey)
        )
        workload <- deserializationHandler(
          yamlObject.getFields(WorkloadKey).headOption.map(_.convertTo[Map[String, Try[Time]]].mapValues(_.get)),
          keyString(WorkloadKey)
        )

      } yield FabanConfiguration(maxRunTime = maxRunTime, interval = interval, workload = workload)

    }

    override def write(obj: Try[FabanConfiguration]): YamlValue = {

      val fabanConfiguration = obj.get

      var map = Map[YamlValue, YamlValue]()

      if (fabanConfiguration.maxRunTime.isDefined)
        map += MaxRunTimeKey -> Try(fabanConfiguration.maxRunTime.get).toYaml

      if (fabanConfiguration.interval.isDefined)
        map += IntervalKey -> Try(fabanConfiguration.interval.get).toYaml

      if (fabanConfiguration.workload.isDefined)
        map += WorkloadKey -> fabanConfiguration.workload.get.mapValues(time => Try(time)).toYaml

      YamlObject(map)

    }
  }

}