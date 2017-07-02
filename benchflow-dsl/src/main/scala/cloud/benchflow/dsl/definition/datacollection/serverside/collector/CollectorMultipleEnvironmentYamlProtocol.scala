package cloud.benchflow.dsl.definition.datacollection.serverside.collector

import cloud.benchflow.dsl.definition.datacollection.serverside.ServerSideConfigurationYamlProtocol
import cloud.benchflow.dsl.definition.datacollection.serverside.collector.environment.Environment
import cloud.benchflow.dsl.definition.datacollection.serverside.collector.environment.EnvironmentYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlValue, _}

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 16.03.17.
 */
object CollectorMultipleEnvironmentYamlProtocol extends DefaultYamlProtocol {

  val Level = s"${ServerSideConfigurationYamlProtocol.Level}"

  private def keyString() = s"$Level.(some collector multiple - environment)"

  implicit object CollectorMultipleEnvironmentReadFormat extends YamlFormat[Try[CollectorMultipleEnvironment]] {

    override def read(yaml: YamlValue): Try[CollectorMultipleEnvironment] = {

      val yamlObject = yaml.asYamlObject

      for {

        collectors <- deserializationHandler(
          yamlObject.convertTo[Map[String, Try[Environment]]].mapValues(_.get),
          keyString())

      } yield CollectorMultipleEnvironment(collectors = collectors)

    }

    override def write(obj: Try[CollectorMultipleEnvironment]): YamlValue = unsupportedWriteOperation

  }

  implicit object CollectorMultipleEnvironmentWriteFormat extends YamlFormat[CollectorMultipleEnvironment] {

    override def write(obj: CollectorMultipleEnvironment): YamlValue = YamlObject {
      obj.collectors.map {
        case (k, environment) => k.toYaml -> environment.toYaml
      }
    }

    override def read(yaml: YamlValue): CollectorMultipleEnvironment = unsupportedReadOperation
  }

}
