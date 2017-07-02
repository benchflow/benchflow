package cloud.benchflow.dsl.definition.datacollection.serverside

import cloud.benchflow.dsl.definition.datacollection.DataCollectionYamlProtocol
import cloud.benchflow.dsl.definition.datacollection.DataCollectionYamlProtocol.ServerSideKey
import cloud.benchflow.dsl.definition.datacollection.serverside.collector.Collector
import cloud.benchflow.dsl.definition.datacollection.serverside.collector.CollectorYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlValue, _}

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 12.03.17.
 */
object ServerSideConfigurationYamlProtocol extends DefaultYamlProtocol {

  val Level = s"${DataCollectionYamlProtocol.Level}.${ServerSideKey.value}"

  private def keyString() = s"$Level"

  implicit object ServerSideConfigurationReadFormat extends YamlFormat[Try[ServerSideConfiguration]] {

    override def read(yaml: YamlValue): Try[ServerSideConfiguration] = {

      val yamlObject = yaml.asYamlObject

      for {

        configurationMap <- deserializationHandler(
          yamlObject.convertTo[Map[String, Try[Collector]]].mapValues(_.get),
          keyString())

      } yield ServerSideConfiguration(configurationMap = configurationMap)

    }

    override def write(obj: Try[ServerSideConfiguration]): YamlValue = unsupportedWriteOperation

  }

  implicit object ServerSideConfigurationWriteFormat extends YamlFormat[ServerSideConfiguration] {

    override def write(obj: ServerSideConfiguration): YamlValue = YamlObject {
      obj.configurationMap.map {
        case (k, collector) => k.toYaml -> collector.toYaml
      }
    }

    override def read(yaml: YamlValue): ServerSideConfiguration = unsupportedReadOperation

  }

}
