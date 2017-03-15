package cloud.benchflow.dsl.definition

import cloud.benchflow.dsl.definition.configuration.Configuration
import cloud.benchflow.dsl.definition.configuration.ConfigurationYamlProtocol._
import cloud.benchflow.dsl.definition.datacollection.DataCollection
import cloud.benchflow.dsl.definition.datacollection.DataCollectionYamlProtocol._
import cloud.benchflow.dsl.definition.sut.Sut
import cloud.benchflow.dsl.definition.sut.SutYamlProtocol._
import cloud.benchflow.dsl.definition.workload.Workload
import cloud.benchflow.dsl.definition.workload.WorkloadYamlProtocol._
import net.jcazevedo.moultingyaml.{YamlString, _}

import scala.util.{Failure, Try}


/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  *         Created on 19/07/16.
  */
//trait BenchFlowTestYamlProtocol extends ConfigurationYamlProtocol with GoalYamlProtocol {
object BenchFlowTestYamlProtocol extends DefaultYamlProtocol {

  val VersionKey = YamlString("version")
  val NameKey = YamlString("name")
  val DescriptionKey = YamlString("description")
  val ConfigurationKey = YamlString("configuration")
  val SutKey = YamlString("sut")
  val WorkloadKey = YamlString("workload")
  val DataCollectionKey = YamlString("data_collection")

  def benchFlowTestFailure(key:String, e: Exception) = Failure(new Exception(BenchFlowTest.getClass.getSimpleName + key + ": " + e))

  implicit object BenchFlowTestYamlFormat extends YamlFormat[Try[BenchFlowTest]] {

    // TODO - we need this?
//    def getObject(key: String)(implicit obj: Map[YamlValue, YamlValue]) =
//      YamlObject(YamlString(key) -> obj(YamlString(key)))

    override def read(yaml: YamlValue): Try[BenchFlowTest] = {

      val testObject = yaml.asYamlObject

      for {

        version <- Try(testObject.fields(VersionKey).convertTo[String]) recoverWith {
          case e: Exception => benchFlowTestFailure(VersionKey.value, e)
        }
        name <- Try(testObject.fields(NameKey).convertTo[String]) recoverWith {
          case e: Exception => benchFlowTestFailure(NameKey.value, e)
        }
        description <- Try(testObject.fields(DescriptionKey).convertTo[String]) recoverWith {
          case e: Exception => benchFlowTestFailure(DescriptionKey.value, e)
        }

        configuration <- testObject.fields(ConfigurationKey).convertTo[Try[Configuration]]
        sut <- testObject.fields(SutKey).convertTo[Try[Sut]]
      // TODO - continue here
        workload <- Try(testObject.fields(WorkloadKey).convertTo[Map[String, Workload]])
        dataCollection <- Try(testObject.fields(DataCollectionKey).convertTo[DataCollection])

      } yield BenchFlowTest(version = version,
        name = name,
        description = description,
        configuration = configuration,
        sut = sut,
        workload = workload,
        dataCollection = dataCollection
      )

      //      val drivers = sut.sutsType match {
      //        case WfMS => testObject.fields.get(YamlString("drivers")).get.asInstanceOf[YamlArray].elements.map(d => d.convertTo[WfMSDriver])
      //        case Http => testObject.fields.get(YamlString("drivers")).get.asInstanceOf[YamlArray].elements.map(d => d.convertTo[HttpDriver])
      //        case _ => throw new DeserializationException("Illegal value for type field.")
      //      }
      //
      //      val properties = testObject.fields.get(YamlString("properties")).map { yamlProps =>
      //        YamlObject(YamlString("properties") -> yamlProps).convertTo[Properties]
      //      }
      //
      //      val sutConfiguration = testObject.fields.get(YamlString("sutConfiguration")).map { yamlConfig =>
      //        YamlObject(YamlString("sutConfiguration") -> yamlConfig).convertTo[SutConfiguration]
      //      }.get
      //
      //      val trials = testObject.fields.get(YamlString("trials")).map { yamlTrials =>
      //        YamlObject(YamlString("trials") -> yamlTrials).convertTo[TotalTrials]
      //      }.get
      //
      //      val loadFunction = testObject.fields.get(YamlString("execution")).get.convertTo[LoadFunction]
      //
      //      val goal = testObject.fields.get(YamlString("goal")).get.convertTo[Goal]


      //      BenchFlowTest(version = version,
      //        name = testName,
      //        description = description,
      //        sut = sut,
      //        trials = trials,
      //        drivers = drivers,
      //        properties = properties,
      //        loadFunction = loadFunction,
      //        sutConfiguration = sutConfiguration,
      //        goal = goal
      //      )
    }

    override def write(tryBenchFlowTest: Try[BenchFlowTest]): YamlValue = ???
//    {
//
//      val benchFlowTest = tryBenchFlowTest.get
//
//      YamlObject(
//        VersionKey -> YamlString(benchFlowTest.version),
//        NameKey -> YamlString(benchFlowTest.name),
//        DescriptionKey -> YamlString(benchFlowTest.description),
//        ConfigurationKey -> benchFlowTest.configuration.toYaml,
//        SutKey -> benchFlowTest.sut.toYaml,
//        WorkloadKey -> benchFlowTest.workload.toYaml,
//        DataCollectionKey -> benchFlowTest.dataCollection.toYaml
//      )
//
//    }

  }

}
