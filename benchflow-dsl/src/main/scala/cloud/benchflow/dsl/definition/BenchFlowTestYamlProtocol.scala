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
import net.jcazevedo.moultingyaml.{ YamlString, _ }

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

  implicit object BenchFlowTestReadFormat extends YamlFormat[Try[BenchFlowTest]] {

    override def read(yaml: YamlValue): Try[BenchFlowTest] = {

      val testObject = yaml.asYamlObject

      for {

        version <- deserializationHandler(
          testObject.fields(VersionKey).convertTo[String],
          keyString(VersionKey))

        name <- deserializationHandler(
          testObject.fields(NameKey).convertTo[String],
          keyString(NameKey))

        description <- deserializationHandler(
          testObject.fields(DescriptionKey).convertTo[String],
          keyString(DescriptionKey))

        configuration <- deserializationHandler(
          testObject.fields(ConfigurationKey).convertTo[Try[Configuration]].get,
          keyString(ConfigurationKey))

        sut <- deserializationHandler(
          testObject.fields(SutKey).convertTo[Try[Sut]].get,
          keyString(SutKey))

        workload <- deserializationHandler(
          testObject.fields(WorkloadKey).convertTo[Map[String, Try[Workload]]].mapValues(_.get),
          keyString(WorkloadKey))

        dataCollection <- deserializationHandler(
          testObject.getFields(DataCollectionKey).headOption.map(_.convertTo[Try[DataCollection]].get),
          keyString(DataCollectionKey))

      } yield BenchFlowTest(
        version = version,
        name = name,
        description = description,
        configuration = configuration,
        sut = sut,
        workload = workload,
        dataCollection = dataCollection)
    }

    override def write(tryBenchFlowTest: Try[BenchFlowTest]): YamlValue = unsupportedWriteOperation

  }

  // TODO - add documentation reference with reasons why we separate read and write into 2 objects
  // The reason is that we want to wrap the result of read into a Try to simplify error handling.
  // Whereas for write we already know that the types are OK from the type system and therefore we don't
  // need the overhead of wrapping and unwrapping Trys.
  // The library does not allow to specify different return types for read and write, therefore
  // we split into two objects. The library takes automatic care of calling the right method.
  implicit object BenchFlowTestWriteFormat extends YamlFormat[BenchFlowTest] {

    override def write(obj: BenchFlowTest): YamlValue = YamlObject {
      Map[YamlValue, YamlValue](
        VersionKey -> obj.version.toYaml,
        NameKey -> obj.name.toYaml,
        DescriptionKey -> obj.description.toYaml,
        ConfigurationKey -> obj.configuration.toYaml,
        SutKey -> obj.sut.toYaml,
        WorkloadKey -> obj.workload.toYaml) ++
        // we map here because value is optional (Option)
        obj.dataCollection.map(key => DataCollectionKey -> key.toYaml) // +
      // this line is an example of how to mix optional and non-optional key,value pairs (incl. '+' on previous line)
      //          (VersionKey -> benchFlowTest.version.toYaml)
    }

    override def read(yaml: YamlValue): BenchFlowTest = unsupportedReadOperation

  }

}
