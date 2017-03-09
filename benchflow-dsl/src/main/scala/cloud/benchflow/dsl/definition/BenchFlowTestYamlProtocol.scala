package cloud.benchflow.dsl.definition

import cloud.benchflow.dsl.definition.sut.http.{Http, HttpDriver}
import cloud.benchflow.dsl.definition.sut.wfms.{WfMS, WfMSDriver}
import cloud.benchflow.dsl.definition.configuration.GoalYamlProtocol
import cloud.benchflow.dsl.definition.sut.ConfigurationYamlProtocol
import net.jcazevedo.moultingyaml._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 19/07/16.
  */
trait BenchFlowTestYamlProtocol extends ConfigurationYamlProtocol with GoalYamlProtocol {

  implicit object BenchFlowTestYamlFormat extends YamlFormat[BenchFlowTest] {

    def getObject(key: String)(implicit obj: Map[YamlValue, YamlValue]) =
      YamlObject(YamlString(key) -> obj.get(YamlString(key)).get)

    override def read(yaml: YamlValue): BenchFlowTest = {

      val testObject = yaml.asYamlObject
      val testName = testObject.fields.get(YamlString("testName")).get.convertTo[String]
      val description = testObject.fields.get(YamlString("description")).get.convertTo[String]

      val sut = testObject.fields.get(YamlString("sut")).get.convertTo[Sut]

      val drivers = sut.sutsType match {
        case WfMS => testObject.fields.get(YamlString("drivers")).get.asInstanceOf[YamlArray].elements.map(d => d.convertTo[WfMSDriver])
        case Http => testObject.fields.get(YamlString("drivers")).get.asInstanceOf[YamlArray].elements.map(d => d.convertTo[HttpDriver])
        case _ => throw new DeserializationException("Illegal value for type field.")
      }

      val properties = testObject.fields.get(YamlString("properties")).map { yamlProps =>
        YamlObject(YamlString("properties") -> yamlProps).convertTo[Properties]
      }

      val sutConfiguration = testObject.fields.get(YamlString("sutConfiguration")).map { yamlConfig =>
        YamlObject(YamlString("sutConfiguration") -> yamlConfig).convertTo[SutConfiguration]
      }.get

      val trials = testObject.fields.get(YamlString("trials")).map { yamlTrials =>
        YamlObject(YamlString("trials") -> yamlTrials).convertTo[TotalTrials]
      }.get

      val loadFunction = testObject.fields.get(YamlString("execution")).get.convertTo[LoadFunction]

      val goal = testObject.fields.get(YamlString("goal")).get.convertTo[Goal]

      BenchFlowTest(
        name = testName,
        description = description,
        sut = sut,
        trials = trials,
        drivers = drivers,
        properties = properties,
        loadFunction = loadFunction,
        sutConfiguration = sutConfiguration,
        goal = goal
      )
    }

    override def write(obj: BenchFlowTest): YamlValue = ???
  }

}
