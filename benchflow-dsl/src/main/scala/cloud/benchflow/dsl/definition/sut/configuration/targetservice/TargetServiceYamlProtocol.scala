package cloud.benchflow.dsl.definition.sut.configuration.targetservice

import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 14.03.17.
  */
object TargetServiceYamlProtocol extends DefaultYamlProtocol {

  val NameKey = YamlString("name")
  val EndpointKey = YamlString("endpoint")
  val SutReadyLogCheckKey = YamlString("sut_ready_log_check")

  implicit object TargetServiceFormat extends YamlFormat[Try[TargetService]] {
    override def read(yaml: YamlValue): Try[TargetService] = {

      var yamlObject = yaml.asYamlObject

      for {
        name <- Try(yamlObject.fields(NameKey).convertTo[String])
        endpoint <- Try(yamlObject.fields(EndpointKey).convertTo[String])
        sutReadyLogCheck <- Try(yamlObject.getFields(SutReadyLogCheckKey).headOption.map(_.convertTo[String]))

      } yield TargetService(name = name, endpoint = endpoint, sutReadyLogCheck = sutReadyLogCheck)

    }

    override def write(obj: Try[TargetService]): YamlValue = ???
  }

}
