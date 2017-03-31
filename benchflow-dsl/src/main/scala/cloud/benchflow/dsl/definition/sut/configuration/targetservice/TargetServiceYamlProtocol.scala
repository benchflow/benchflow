package cloud.benchflow.dsl.definition.sut.configuration.targetservice

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation }
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 14.03.17.
 */
object TargetServiceYamlProtocol extends DefaultYamlProtocol {

  val NameKey = YamlString("name")
  val EndpointKey = YamlString("endpoint")
  val SutReadyLogCheckKey = YamlString("sut_ready_log_check")

  private def keyString(key: YamlString) = "sut.configuration.target_service" + key.value

  implicit object TargetServiceReadFormat extends YamlFormat[Try[TargetService]] {
    override def read(yaml: YamlValue): Try[TargetService] = {

      val yamlObject = yaml.asYamlObject

      for {

        name <- deserializationHandler(
          yamlObject.fields(NameKey).convertTo[String],
          keyString(NameKey))

        endpoint <- deserializationHandler(
          yamlObject.fields(EndpointKey).convertTo[String],
          keyString(EndpointKey))

        sutReadyLogCheck <- deserializationHandler(
          yamlObject.getFields(SutReadyLogCheckKey).headOption.map(_.convertTo[String]),
          keyString(SutReadyLogCheckKey))

      } yield TargetService(name = name, endpoint = endpoint, sutReadyLogCheck = sutReadyLogCheck)

    }

    override def write(obj: Try[TargetService]): YamlValue = unsupportedWriteOperation
  }

  implicit object TargetServiceWriteFormat extends YamlFormat[TargetService] {

    override def write(obj: TargetService): YamlValue = YamlObject {
      Map[YamlValue, YamlValue](
        NameKey -> obj.name.toYaml,
        EndpointKey -> obj.endpoint.toYaml) ++
        obj.sutReadyLogCheck.map(key => SutReadyLogCheckKey -> key.toYaml)

    }

    override def read(yaml: YamlValue): TargetService = unsupportedReadOperation
  }

}
