package cloud.benchflow.dsl.definition.datacollection.clientside

import cloud.benchflow.dsl.definition.datacollection.clientside.faban.FabanConfiguration
import cloud.benchflow.dsl.definition.datacollection.clientside.faban.FabanConfigurationYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue, _}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 12.03.17.
  */
object ClientSideConfigurationYamlProtocol extends DefaultYamlProtocol {

  val FabanKey = YamlString("faban")

  private def keyString(key: YamlString) = "data_collection.client_side." + key.value

  implicit object ClientSideConfigurationFormat extends YamlFormat[Try[ClientSideConfiguration]] {
    override def read(yaml: YamlValue): Try[ClientSideConfiguration] = {

      val yamlObject = yaml.asYamlObject

      for {

        faban <- YamlErrorHandler.deserializationHandler(
          yamlObject.fields(FabanKey).convertTo[Try[FabanConfiguration]].get,
          keyString(FabanKey)
        )

      } yield ClientSideConfiguration(faban)


    }

    override def write(obj: Try[ClientSideConfiguration]): YamlValue = {

      val clientSideConfiguration = obj.get

      val map = Map[YamlValue, YamlValue](
        FabanKey -> Try(clientSideConfiguration.faban).toYaml
      )

      YamlObject(map)

    }
  }

}
