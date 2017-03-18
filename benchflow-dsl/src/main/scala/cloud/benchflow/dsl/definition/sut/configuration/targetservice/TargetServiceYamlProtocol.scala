package cloud.benchflow.dsl.definition.sut.configuration.targetservice

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.deserializationHandler
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue}

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

  implicit object TargetServiceFormat extends YamlFormat[Try[TargetService]] {
    override def read(yaml: YamlValue): Try[TargetService] = {

      val yamlObject = yaml.asYamlObject

      for {

        name <- deserializationHandler(
          yamlObject.fields(NameKey).convertTo[String],
          keyString(NameKey)
        )

        endpoint <- deserializationHandler(
          yamlObject.fields(EndpointKey).convertTo[String],
          keyString(EndpointKey)
        )

        sutReadyLogCheck <- deserializationHandler(
          yamlObject.getFields(SutReadyLogCheckKey).headOption.map(_.convertTo[String]),
          keyString(SutReadyLogCheckKey)
        )

      } yield TargetService(name = name, endpoint = endpoint, sutReadyLogCheck = sutReadyLogCheck)

    }

    override def write(obj: Try[TargetService]): YamlValue = {

      val targetService = obj.get

      var map = Map[YamlValue, YamlValue](
        NameKey -> YamlString(targetService.name),
        EndpointKey -> YamlString(targetService.endpoint)
      )

      if (targetService.sutReadyLogCheck.isDefined)
        map += SutReadyLogCheckKey -> YamlString(targetService.sutReadyLogCheck.get)

      YamlObject(map)

    }
  }

}
