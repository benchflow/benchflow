package cloud.benchflow.dsl.definition.simone

import cloud.benchflow.dsl.definition.simone.http.HttpSutYamlProtocol
import cloud.benchflow.dsl.definition.simone.wfms.WfMSSutYamlProtocol
import cloud.benchflow.dsl.definition.sut.SutType
import cloud.benchflow.dsl.definition.sut.configuration.targetservice.TargetService
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  *         Created on 18/07/16.
  */
trait ConfigurationYamlProtocol extends DefaultYamlProtocol
  with CommonsYamlProtocol
  with WfMSSutYamlProtocol
  with HttpSutYamlProtocol {

  implicit val deployFormat = yamlFormat1(Deploy)
  implicit val targetServiceFormat = yamlFormat3(TargetService)
  implicit val totalTrialsFormat = yamlFormat1(TotalTrials)
  implicit val executionFormat = yamlFormat3(LoadFunction)

  implicit object SutsTypeYamlFormat extends YamlFormat[SutType] {
    override def write(obj: SutType): YamlValue = ???

    override def read(yaml: YamlValue): SutType = {
      SutType(yaml.asYamlObject.getFields(
        YamlString("suts_type")
      ).head.convertTo[String])
    }

  }

  //  implicit object BenchFlowConfigFormat extends YamlFormat[ServerSideConfiguration] {
  //
  //    override def write(bFlowConfig: ServerSideConfiguration): YamlValue = {
  //      bFlowConfig.configurationMap.toYaml
  //    }
  //
  //    override def read(yaml: YamlValue): ServerSideConfiguration = {
  //      val bfConfig = yaml.asYamlObject.fields.head
  //      val bindings = bfConfig._2.asYamlObject.fields
  //
  //      ServerSideConfiguration(
  //        bindings.map(binding => binding match {
  //          case (YamlString(sName), YamlArray(bound)) =>
  //            (sName, bound.map(binding => binding.convertTo[Binding]))
  //          case _ => throw DeserializationException("Unexpected format for field benchflow-config")
  //        })
  //      )
  //
  //    }
  //  }

}
