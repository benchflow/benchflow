package cloud.benchflow.dsl.definition.time

import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue}

import scala.util.{Failure, Success, Try}

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 14.03.17.
  */
object TimeYamlProtocol extends DefaultYamlProtocol {

  implicit object TimeFormat extends YamlFormat[Try[Time]] {

    override def read(yaml: YamlValue): Try[Time] = {

      Time.fromString(yaml.convertTo[String])

    }

    override def write(obj: Try[Time]): YamlValue = obj match {

      case Success(time) => YamlString(time.toString)
      case Failure(ex) => YamlString(ex.toString)

    }

  }

}
