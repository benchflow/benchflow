package cloud.benchflow.dsl.definition

import cloud.benchflow.dsl.definition.configuration.ExperimentConfiguration
import cloud.benchflow.dsl.definition.configuration.ExperimentConfigurationYamlProtocol._
import cloud.benchflow.dsl.definition.datacollection.DataCollection
import cloud.benchflow.dsl.definition.datacollection.DataCollectionYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation}
import cloud.benchflow.dsl.definition.sut.Sut
import cloud.benchflow.dsl.definition.sut.SutYamlProtocol._
import cloud.benchflow.dsl.definition.workload.Workload
import cloud.benchflow.dsl.definition.workload.WorkloadYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-03-31
 */
object BenchFlowExperimentYamlProtocol extends DefaultYamlProtocol {

  val VersionKey = YamlString("version")
  val NameKey = YamlString("name")
  val DescriptionKey = YamlString("description")
  val ConfigurationKey = YamlString("configuration")
  val SutKey = YamlString("sut")
  val WorkloadKey = YamlString("workload")
  val DataCollectionKey = YamlString("data_collection")

  private def keyString(key: YamlString) = "" + key.value

  implicit object BenchFlowExperimentReadFormat extends YamlFormat[Try[BenchFlowExperiment]] {

    override def read(yaml: YamlValue): Try[BenchFlowExperiment] = {

      val yamlObject = yaml.asYamlObject

      for {

        version <- deserializationHandler(
          yamlObject.fields(VersionKey).convertTo[String],
          keyString(VersionKey)
        )

        name <- deserializationHandler(
          yamlObject.fields(NameKey).convertTo[String],
          keyString(NameKey)
        )

        description <- deserializationHandler(
          yamlObject.fields(DescriptionKey).convertTo[String],
          keyString(DescriptionKey)
        )

        configuration <- deserializationHandler(
          yamlObject.fields(ConfigurationKey).convertTo[Try[ExperimentConfiguration]].get,
          keyString(ConfigurationKey)
        )

        sut <- deserializationHandler(
          yamlObject.fields(SutKey).convertTo[Try[Sut]].get,
          keyString(SutKey)
        )

        workload <- deserializationHandler(
          yamlObject.fields(WorkloadKey).convertTo[Map[String, Try[Workload]]].mapValues(_.get),
          keyString(WorkloadKey)
        )

        dataCollection <- deserializationHandler(
          yamlObject.getFields(DataCollectionKey).headOption.map(_.convertTo[Try[DataCollection]].get),
          keyString(DataCollectionKey)
        )

      } yield BenchFlowExperiment(
        version = version,
        name = name,
        description = description,
        configuration = configuration,
        sut = sut,
        workload = workload,
        dataCollection = dataCollection
      )
    }

    override def write(obj: Try[BenchFlowExperiment]): YamlValue = unsupportedWriteOperation
  }

  implicit object BenchFlowExperimentWriteFormat extends YamlFormat[BenchFlowExperiment] {

    override def write(obj: BenchFlowExperiment): YamlValue = YamlObject {
      Map[YamlValue, YamlValue](
        VersionKey -> obj.version.toYaml,
        NameKey -> obj.name.toYaml,
        DescriptionKey -> obj.description.toYaml,
        ConfigurationKey -> obj.configuration.toYaml,
        SutKey -> obj.sut.toYaml,
        WorkloadKey -> obj.workload.toYaml
      ) ++
        obj.dataCollection.map(key => DataCollectionKey -> key.toYaml)
    }

    override def read(yaml: YamlValue): BenchFlowExperiment = unsupportedReadOperation
  }

}
