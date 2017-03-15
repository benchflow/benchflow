package cloud.benchflow.dsl.definition.datacollection

import cloud.benchflow.dsl.definition.datacollection.clientside.ClientSideConfiguration
import cloud.benchflow.dsl.definition.datacollection.clientside.ClientSideConfigurationYamlProtocol._
import cloud.benchflow.dsl.definition.datacollection.serverside.ServerSideConfiguration
import cloud.benchflow.dsl.definition.datacollection.serverside.ServerSideConfigurationYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue}

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 10.03.17.
  */
object DataCollectionYamlProtocol extends DefaultYamlProtocol {

  // TODO - implement me
  val ClientSideKey = YamlString("client_side")
  val ServerSideKey = YamlString("server_side")

  implicit object DataCollectionYamlFormat extends YamlFormat[DataCollection] {
    override def read(yaml: YamlValue): DataCollection = {

      val yamlObject = yaml.asYamlObject

      val clientSide = yamlObject.fields(ClientSideKey).convertTo[ClientSideConfiguration]
      val serverSideConfiguration = yamlObject.fields(ServerSideKey).convertTo[ServerSideConfiguration]

      DataCollection(clientSide = clientSide, serverSideConfiguration = serverSideConfiguration)

    }

    override def write(obj: DataCollection): YamlValue = ???
  }

}
