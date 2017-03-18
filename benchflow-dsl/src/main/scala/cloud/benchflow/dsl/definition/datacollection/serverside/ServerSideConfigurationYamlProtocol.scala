package cloud.benchflow.dsl.definition.datacollection.serverside

import cloud.benchflow.dsl.definition.datacollection.serverside.collector.Collector
import cloud.benchflow.dsl.definition.datacollection.serverside.collector.CollectorYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlValue, _}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 12.03.17.
  */
object ServerSideConfigurationYamlProtocol extends DefaultYamlProtocol {

  private def keyString() = "data_collection.server_side"

  implicit object ServerSideConfigurationFormat extends YamlFormat[Try[ServerSideConfiguration]] {

    override def read(yaml: YamlValue): Try[ServerSideConfiguration] = {

      val yamlObject = yaml.asYamlObject

      for {

        configurationMap <- YamlErrorHandler.deserializationHandler(
          yamlObject.convertTo[Map[String, Try[Collector]]].mapValues(_.get),
          keyString()
        )

      } yield ServerSideConfiguration(configurationMap = configurationMap)

    }

    override def write(obj: Try[ServerSideConfiguration]): YamlValue = {

      val configuration = obj.get

      YamlObject(
        configuration.configurationMap.map{
          case (k, collector) => k.toYaml -> Try(collector).toYaml
        }
      )

    }

  }

}
