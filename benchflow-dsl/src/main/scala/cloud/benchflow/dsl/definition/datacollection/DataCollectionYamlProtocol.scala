package cloud.benchflow.dsl.definition.datacollection

import cloud.benchflow.dsl.definition.datacollection.clientside.ClientSideConfiguration
import cloud.benchflow.dsl.definition.datacollection.clientside.ClientSideConfigurationYamlProtocol._
import cloud.benchflow.dsl.definition.datacollection.serverside.ServerSideConfiguration
import cloud.benchflow.dsl.definition.datacollection.serverside.ServerSideConfigurationYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.deserializationHandler
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 10.03.17.
  */
object DataCollectionYamlProtocol extends DefaultYamlProtocol {

  val ClientSideKey = YamlString("client_side")
  val ServerSideKey = YamlString("server_side")

  private def keyString(key: YamlString) = "data_collection." + key.value

  implicit object DataCollectionYamlFormat extends YamlFormat[Try[DataCollection]] {
    override def read(yaml: YamlValue): Try[DataCollection] = {

      val yamlObject = yaml.asYamlObject

      for {

        clientSide <- deserializationHandler(
          yamlObject.getFields(ClientSideKey).headOption.map(_.convertTo[Try[ClientSideConfiguration]].get),
          keyString(ClientSideKey)
        )

        serverSideConfiguration <- deserializationHandler(
          yamlObject.getFields(ServerSideKey).headOption.map(_.convertTo[Try[ServerSideConfiguration]].get),
          keyString(ServerSideKey)
        )

      } yield DataCollection(clientSide = clientSide, serverSideConfiguration = serverSideConfiguration)

    }

    override def write(obj: Try[DataCollection]): YamlValue = {

      val dataCollection = obj.get

      var map = Map[YamlValue, YamlValue]()

      if (dataCollection.clientSide.isDefined)
        map += ClientSideKey -> Try(dataCollection.clientSide.get).toYaml

      if (dataCollection.serverSideConfiguration.isDefined)
        map += ServerSideKey -> Try(dataCollection.serverSideConfiguration.get).toYaml

      YamlObject(map)

    }
  }

}
