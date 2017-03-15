package cloud.benchflow.dsl.definition.sut.configuration

import cloud.benchflow.dsl.definition.sut.configuration.targetservice.TargetService
import cloud.benchflow.dsl.definition.sut.configuration.targetservice.TargetServiceYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
object ConfigurationYamlProtocol extends DefaultYamlProtocol {

  val TargetServiceKey = YamlString("target_service")
  val DeploymentKey = YamlString("deployment")

  // TODO - rename to Configuration
  implicit object SutConfigurationFormat extends YamlFormat[Try[SutConfiguration]] {

    override def read(yaml: YamlValue): Try[SutConfiguration] = {

      val yamlObject = yaml.asYamlObject

      for {

        targetService <- yamlObject.fields(TargetServiceKey).convertTo[Try[TargetService]]
        deployment <- Try(yamlObject.fields(DeploymentKey).convertTo[Map[String, String]])

      } yield SutConfiguration(targetService = targetService, deployment = deployment)


      //      val sutConfig = yaml.asYamlObject.fields.head
      //      val sections = sutConfig._2.asYamlObject.fields.toMap
      //      val deployKey = YamlString("deploy")
      //      val bfConfigKey = YamlString("benchflowConfig")
      //      val deploy = YamlObject(deployKey -> sections(deployKey)).convertTo[Deploy]
      //      val bfConfig = YamlObject(bfConfigKey -> sections(bfConfigKey)).convertTo[BenchFlowConfig]
      //      val targetService = sections(YamlString("targetService")).convertTo[TargetService]

      //      SutConfiguration(deploy = deploy, bfConfig = bfConfig, targetService = targetService)

    }

    override def write(sutConfig: Try[SutConfiguration]): YamlValue = ???

//    {
//      //      YamlObject(
//      //        TargetServiceKey -> sutConfig.targetService.toYaml,
//      //        DeploymentKey -> sutConfig.deployment.toYaml
//      //      )
//    }
  }

}
