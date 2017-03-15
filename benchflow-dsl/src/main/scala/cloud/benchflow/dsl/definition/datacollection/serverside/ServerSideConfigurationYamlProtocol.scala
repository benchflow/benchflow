package cloud.benchflow.dsl.definition.datacollection.serverside

import cloud.benchflow.dsl.definition.binding.Binding
import cloud.benchflow.dsl.definition.binding.BindingYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, DeserializationException, YamlArray, YamlFormat, YamlString, YamlValue, _}

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 12.03.17.
  */
object ServerSideConfigurationYamlProtocol extends DefaultYamlProtocol {

  implicit object ServerSideConfigurationFormat extends YamlFormat[ServerSideConfiguration] {

    override def read(yaml: YamlValue): ServerSideConfiguration = {
      val bfConfig = yaml.asYamlObject.fields.head
      val bindings = bfConfig._2.asYamlObject.fields

      ServerSideConfiguration(
        bindings.map {
          case (YamlString(sName), YamlArray(bound)) => (sName, bound.map(binding => binding.convertTo[Binding]))
          case _ => throw DeserializationException("Unexpected format for field benchflow-config")
        }
      )

    }

    override def write(bFlowConfig: ServerSideConfiguration): YamlValue = {
      bFlowConfig.configurationMap.toYaml
    }

  }

}
