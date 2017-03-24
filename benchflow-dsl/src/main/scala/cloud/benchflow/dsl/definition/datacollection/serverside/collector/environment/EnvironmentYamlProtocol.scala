package cloud.benchflow.dsl.definition.datacollection.serverside.collector.environment

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.deserializationHandler
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue, _}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 16.03.17.
  */
object EnvironmentYamlProtocol extends DefaultYamlProtocol {

  val EnvironmentKey = YamlString("environment")

  private def keyString(yamlString: YamlString) = "data_collection.server_side.(some collector multiple - environment)." + yamlString.value

  implicit object EnvironmentFormat extends YamlFormat[Try[Environment]] {

    override def read(yaml: YamlValue): Try[Environment] = {

      for {

        environment <- deserializationHandler(
          yaml.asYamlObject.fields(EnvironmentKey).convertTo[Map[String, String]],
          keyString(EnvironmentKey)
        )

      } yield Environment(environment = environment)

    }

    override def write(obj: Try[Environment]): YamlValue = {

      val environment = obj.get

      YamlObject(
        EnvironmentKey -> environment.environment.toYaml
      )

    }
  }

}
