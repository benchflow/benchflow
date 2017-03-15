package cloud.benchflow.dsl.definition.datacollection.clientside

import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlValue}

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 12.03.17.
  */
object ClientSideConfigurationYamlProtocol extends DefaultYamlProtocol {

  implicit object ClientSideConfigurationFormat extends YamlFormat[ClientSideConfiguration] {
    override def read(yaml: YamlValue): ClientSideConfiguration = ???

    override def write(obj: ClientSideConfiguration): YamlValue = ???
  }

}
