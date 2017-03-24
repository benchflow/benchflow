package cloud.benchflow.dsl.definition.datacollection.serverside.collector

import cloud.benchflow.dsl.definition.datacollection.serverside.collector.environment.Environment
import cloud.benchflow.dsl.definition.datacollection.serverside.collector.environment.EnvironmentYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.deserializationHandler
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlValue, _}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 16.03.17.
  */
object CollectorMultipleEnvironmentYamlProtocol extends DefaultYamlProtocol {

  private def keyString() = "data_collection.server_side.(some collector multiple - environment)"

  implicit object CollectorMultipleEnvironmentFormat extends YamlFormat[Try[CollectorMultipleEnvironment]] {

    override def read(yaml: YamlValue): Try[CollectorMultipleEnvironment] = {

      val yamlObject = yaml.asYamlObject

      for {

        collectors <- deserializationHandler(
          yamlObject.convertTo[Map[String, Try[Environment]]].mapValues(_.get),
          keyString()
        )

      } yield CollectorMultipleEnvironment(collectors = collectors)

    }

    override def write(obj: Try[CollectorMultipleEnvironment]): YamlValue = {

      val collectorMultipleEnvironment = obj.get

      YamlObject(
        collectorMultipleEnvironment.collectors.map {
          case (k, environment) => k.toYaml -> Try(environment).toYaml
        }
      )


    }
  }

}