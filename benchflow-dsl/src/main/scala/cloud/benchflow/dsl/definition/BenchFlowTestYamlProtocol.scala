package cloud.benchflow.dsl.definition

import cloud.benchflow.dsl.definition.configuration.Configuration
import cloud.benchflow.dsl.definition.configuration.ConfigurationYamlProtocol._
import cloud.benchflow.dsl.definition.datacollection.DataCollection
import cloud.benchflow.dsl.definition.datacollection.DataCollectionYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler._
import cloud.benchflow.dsl.definition.sut.Sut
import cloud.benchflow.dsl.definition.sut.SutYamlProtocol._
import cloud.benchflow.dsl.definition.workload.Workload
import cloud.benchflow.dsl.definition.workload.WorkloadYamlProtocol._
import net.jcazevedo.moultingyaml.{YamlString, _}

import scala.util.Try


/**
  * @author Jesper Findahl (jesper.findahl@usi.ch)
  *         created on 16.03.17.
  */
object BenchFlowTestYamlProtocol extends DefaultYamlProtocol {

  val VersionKey = YamlString("version")
  val NameKey = YamlString("name")
  val DescriptionKey = YamlString("description")
  val ConfigurationKey = YamlString("configuration")
  val SutKey = YamlString("sut")
  val WorkloadKey = YamlString("workload")
  val DataCollectionKey = YamlString("data_collection")

  private def keyString(key: YamlString) = "" + key.value

  implicit object BenchFlowTestFormat extends YamlFormat[Try[BenchFlowTest]] {

    override def read(yaml: YamlValue): Try[BenchFlowTest] = {

      val testObject = yaml.asYamlObject

      for {

        version <- deserializationHandler(
          testObject.fields(VersionKey).convertTo[String],
          keyString(VersionKey)
        )

        name <- deserializationHandler(
          testObject.fields(NameKey).convertTo[String],
          keyString(NameKey)
        )

        description <- deserializationHandler(
          testObject.fields(DescriptionKey).convertTo[String],
          keyString(DescriptionKey)
        )

        configuration <- deserializationHandler(
          testObject.fields(ConfigurationKey).convertTo[Try[Configuration]].get,
          keyString(ConfigurationKey)
        )

        sut <- deserializationHandler(
          testObject.fields(SutKey).convertTo[Try[Sut]].get,
          keyString(SutKey)
        )

        workload <- deserializationHandler(
          testObject.fields(WorkloadKey).convertTo[Map[String, Try[Workload]]].mapValues(_.get),
          keyString(WorkloadKey)
        )

        dataCollection <- deserializationHandler(
          testObject.getFields(DataCollectionKey).headOption.map(_.convertTo[Try[DataCollection]].get),
          keyString(DataCollectionKey)
        )

      } yield BenchFlowTest(version = version,
        name = name,
        description = description,
        configuration = configuration,
        sut = sut,
        workload = workload,
        dataCollection = dataCollection
      )
    }

    override def write(tryBenchFlowTest: Try[BenchFlowTest]): YamlValue = {

      // TODO - add documentation reference with reasons why the code is like it is (Try, and .get, and not match)
      // TODO - document design decisions (which options(exceptions, try (both), try read & normal write and why)
      // TODO - change to separate Format for write (see BenchFlowTestWriteFormat)
      val benchFlowTest = tryBenchFlowTest.get

      var map = Map[YamlValue, YamlValue](
        VersionKey -> benchFlowTest.version.toYaml,
        NameKey -> benchFlowTest.name.toYaml,
        DescriptionKey -> benchFlowTest.description.toYaml,
        ConfigurationKey -> Try(benchFlowTest.configuration).toYaml,
        SutKey -> Try(benchFlowTest.sut).toYaml,
        WorkloadKey -> benchFlowTest.workload.mapValues(v => Try(v)).toYaml
      )

      // optional keys
      if (benchFlowTest.dataCollection.isDefined) {
        map += DataCollectionKey -> Try(benchFlowTest.dataCollection.get).toYaml
      }

      YamlObject(map)

    }

  }

  implicit object BenchFlowTestWriteFormat extends YamlFormat[BenchFlowTest] {

    override def write(obj: BenchFlowTest): YamlValue = ???

    override def read(yaml: YamlValue): BenchFlowTest = ???
  }

}
