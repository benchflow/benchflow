package cloud.benchflow.dsl.definition.sut

import cloud.benchflow.dsl.definition._
import cloud.benchflow.dsl.definition.sut.http.{Http, HttpSutYamlProtocol}
import cloud.benchflow.dsl.definition.sut.wfms.{WfMS, WfMSSutYamlProtocol}
import net.jcazevedo.moultingyaml._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 18/07/16.
  */
trait ConfigurationYamlProtocol extends DefaultYamlProtocol
                                with CommonsYamlProtocol
                                with WfMSSutYamlProtocol
                                with HttpSutYamlProtocol {

  implicit val deployFormat = yamlFormat1(Deploy)
  implicit val targetServiceFormat = yamlFormat3(TargetService)
  implicit val totalTrialsFormat = yamlFormat1(TotalTrials)
  implicit val executionFormat = yamlFormat3(LoadFunction)

  implicit object SutsTypeYamlFormat extends YamlFormat[SutsType] {
    override def write(obj: SutsType): YamlValue = ???

    override def read(yaml: YamlValue): SutsType = {
      SutsType(yaml.asYamlObject.getFields(
        YamlString("suts_type")
      ).head.convertTo[String])
    }

  }

  implicit object SutYamlFormat extends YamlFormat[Sut] {
    override def write(sut: Sut): YamlValue = {
      YamlObject(
        YamlString("name") -> YamlString(sut.name),
        YamlString("version") -> YamlString(sut.version.toString),
        YamlString("type") -> YamlString(sut.sutsType match {
          case WfMS => "WfMS"
          case Http => "http"
        })
      )
    }

    override def read(yaml: YamlValue): Sut = {
      val sutName = yaml.asYamlObject.fields.get(YamlString("name")) match {
        case Some(YamlString(name)) => name
        case _ => throw new DeserializationException("No name specified in sut definition")
      }

      val version = yaml.asYamlObject.fields.get(YamlString("version")) match {
        case Some(YamlString(v)) => Version(v)
        case _ => throw new DeserializationException("No version specified in sut definition")
      }

      val sutsType = yaml.asYamlObject.fields.get(YamlString("type")) match {
        case Some(YamlString(t)) => SutsType(t)
        case _ => throw new DeserializationException("No type specified in sut definition")
      }

      Sut(sutName, version, sutsType)
    }
  }




  implicit object BindingYamlFormat extends YamlFormat[Binding] {
    override def write(binding: Binding): YamlValue = {

      binding.config match {
        case Some(config) => YamlObject(
          YamlString(binding.boundService) -> config.toYaml
        )
        case None => YamlString(binding.boundService)
      }

    }

    override def read(yaml: YamlValue): Binding = {

      def readYamlWithConfig(yaml: YamlValue): Binding = {
        val binding = yaml.asYamlObject.fields.head
        val bfService = binding._1.convertTo[String]
        val props = binding._2.asYamlObject.getFields(YamlString("config")) match {
          case Seq(YamlObject(obj)) =>
            Some(YamlObject(YamlString("properties") -> YamlObject(obj)).convertTo[Properties])
          case _ => None
        }
        Binding(bfService, props)
      }

      yaml match {
        case YamlString(boundName) => Binding(boundName, None)
        case other => readYamlWithConfig(other)
      }

    }

  }

  implicit object BenchFlowConfigFormat extends YamlFormat[BenchFlowConfig] {

    override def write(bFlowConfig: BenchFlowConfig): YamlValue = {
      bFlowConfig.benchflow_config.toYaml
    }

    override def read(yaml: YamlValue): BenchFlowConfig = {
      val bfConfig = yaml.asYamlObject.fields.head
      val bindings = bfConfig._2.asYamlObject.fields

      BenchFlowConfig(
        bindings.map(binding => binding match {
          case (YamlString(sName), YamlArray(bound)) =>
            (sName, bound.map(binding => binding.convertTo[Binding]))
          case _ => throw DeserializationException("Unexpected format for field benchflow-config")
        })
      )

    }
  }

  implicit object SutConfigurationFormat extends YamlFormat[SutConfiguration] {

    override def write(sutConfig: SutConfiguration): YamlValue = {
      YamlObject(
        YamlString("targetService") -> sutConfig.targetService.toYaml,
        YamlString("deploy") -> sutConfig.deploy.toYaml,
        YamlString("benchflowConfig") -> sutConfig.bfConfig.toYaml
      )
    }

    override def read(yaml: YamlValue): SutConfiguration = {
      val sutConfig = yaml.asYamlObject.fields.head
      val sections = sutConfig._2.asYamlObject.fields.toMap
      val deployKey = YamlString("deploy")
      val bfConfigKey = YamlString("benchflowConfig")
      val deploy = YamlObject(deployKey -> sections.get(deployKey).get).convertTo[Deploy]
      val bfConfig = YamlObject(bfConfigKey -> sections.get(bfConfigKey).get).convertTo[BenchFlowConfig]
      val targetService = sections.get(YamlString("targetService")).get.convertTo[TargetService]
      SutConfiguration(deploy = deploy, bfConfig = bfConfig, targetService = targetService)
    }
  }

}
