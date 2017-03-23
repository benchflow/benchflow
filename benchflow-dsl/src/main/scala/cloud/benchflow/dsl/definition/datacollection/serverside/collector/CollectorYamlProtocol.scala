package cloud.benchflow.dsl.definition.datacollection.serverside.collector


import cloud.benchflow.dsl.definition.datacollection.serverside.collector.CollectorMultipleEnvironmentYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.deserializationHandler
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlArray, YamlFormat, YamlString, YamlValue, _}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 16.03.17.
  */
object CollectorYamlProtocol extends DefaultYamlProtocol {

  private def keyString() = "data_collection.server_side.(some collector)"

  implicit object CollectorFormat extends YamlFormat[Try[Collector]] {

    override def read(yaml: YamlValue): Try[Collector] = {

      yaml match {

        case YamlString(collector) =>
          deserializationHandler(CollectorSingle(collector), keyString())

        case YamlArray(collectors) =>
          deserializationHandler(CollectorMultiple(collectors.map(_.convertTo[String])), keyString())

        case yamlObject =>
          deserializationHandler(yamlObject.convertTo[Try[CollectorMultipleEnvironment]].get, keyString())

      }

    }

    override def write(obj: Try[Collector]): YamlValue = {

      val collector = obj.get

      collector match {

        case CollectorSingle(c) => c.toYaml

        case CollectorMultiple(c) => c.toYaml

        case cm: CollectorMultipleEnvironment => Try(cm).toYaml

      }

    }
  }

}
