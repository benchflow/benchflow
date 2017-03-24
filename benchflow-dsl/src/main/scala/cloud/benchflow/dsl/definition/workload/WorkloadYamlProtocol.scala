package cloud.benchflow.dsl.definition.workload

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.deserializationHandler
import cloud.benchflow.dsl.definition.types.percent.Percent
import cloud.benchflow.dsl.definition.types.percent.PercentYamlProtocol._
import cloud.benchflow.dsl.definition.workload.mix.Mix
import cloud.benchflow.dsl.definition.workload.mix.MixYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 10.03.17.
  */
object WorkloadYamlProtocol extends DefaultYamlProtocol {

  val TypeKey = YamlString("type")
  val PopularityKey = YamlString("popularity")
  val InterOperationTimingsKey = YamlString("inter_operation_timings")
  val OperationsKey = YamlString("operations")
  val MixKey = YamlString("mix")

  private def keyString(key: YamlString) = "workload." + key.value

  implicit object WorkloadYamlFormat extends YamlFormat[Try[Workload]] {
    override def read(yaml: YamlValue): Try[Workload] = {

      val yamlObject = yaml.asYamlObject

      for {

        workloadType <- deserializationHandler(
          yamlObject.fields(TypeKey).convertTo[String],
          keyString(TypeKey)
        )

        popularity <- deserializationHandler(
          yamlObject.getFields(PopularityKey).headOption.map(_.convertTo[Try[Percent]].get),
          keyString(PopularityKey)
        )

        interOperationTimings <- deserializationHandler(
          yamlObject.getFields(InterOperationTimingsKey).headOption.map(_.convertTo[String]),
          keyString(InterOperationTimingsKey)
        )

        operations <- deserializationHandler(
          yamlObject.fields(OperationsKey).convertTo[List[String]],
          keyString(OperationsKey)
        )

        mix <- deserializationHandler(
          yamlObject.getFields(MixKey).headOption.map(_.convertTo[Try[Mix]].get),
          keyString(MixKey)
        )

      } yield Workload(
        workloadType = workloadType,
        popularity = popularity,
        interOperationTimings = interOperationTimings,
        operations = operations,
        mix = mix
      )

    }

    override def write(obj: Try[Workload]): YamlValue = {

      val workload = obj.get

      var map = Map[YamlValue, YamlValue](
        TypeKey -> workload.workloadType.toYaml,
        OperationsKey -> workload.operations.toYaml
      )

      if (workload.popularity.isDefined)
        map += PopularityKey -> Try(workload.popularity.get).toYaml

      if (workload.interOperationTimings.isDefined)
        map += InterOperationTimingsKey -> workload.interOperationTimings.get.toYaml

      if (workload.mix.isDefined)
        map += MixKey -> Try(workload.mix.get).toYaml

      YamlObject(map)

    }

  }

}
