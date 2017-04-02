package cloud.benchflow.dsl.definition.types.time

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ unsupportedReadOperation, unsupportedWriteOperation }
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 14.03.17.
 */
object TimeYamlProtocol extends DefaultYamlProtocol {

  implicit object TimeReadFormat extends YamlFormat[Try[Time]] {

    override def read(yaml: YamlValue): Try[Time] = Time.fromString(yaml.convertTo[String])

    override def write(obj: Try[Time]): YamlValue = unsupportedWriteOperation

  }

  implicit object TimeWriteFormat extends YamlFormat[Time] {

    override def write(obj: Time): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): Time = unsupportedReadOperation
  }

}
