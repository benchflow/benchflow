package cloud.benchflow.dsl.definition.sut.simone

import cloud.benchflow.dsl.definition._
import cloud.benchflow.dsl.definition.binding.Binding
import cloud.benchflow.dsl.definition.binding.BindingYamlProtocol._
import cloud.benchflow.dsl.definition.datacollection.serverside.ServerSideConfiguration
import cloud.benchflow.dsl.definition.sut.`type`.SutType
import cloud.benchflow.dsl.definition.sut.configuration.targetservice.TargetService
import cloud.benchflow.dsl.definition.sut.simone.http.HttpSutYamlProtocol
import cloud.benchflow.dsl.definition.sut.simone.wfms.WfMSSutYamlProtocol
import net.jcazevedo.moultingyaml._

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

  implicit object BenchFlowConfigFormat extends YamlFormat[ServerSideConfiguration] {

    override def write(bFlowConfig: ServerSideConfiguration): YamlValue = {
      bFlowConfig.configurationMap.toYaml
    }

    override def read(yaml: YamlValue): ServerSideConfiguration = {
      val bfConfig = yaml.asYamlObject.fields.head
      val bindings = bfConfig._2.asYamlObject.fields

      ServerSideConfiguration(
        bindings.map(binding => binding match {
          case (YamlString(sName), YamlArray(bound)) =>
            (sName, bound.map(binding => binding.convertTo[Binding]))
          case _ => throw DeserializationException("Unexpected format for field benchflow-config")
        })
      )

    }
  }

}
