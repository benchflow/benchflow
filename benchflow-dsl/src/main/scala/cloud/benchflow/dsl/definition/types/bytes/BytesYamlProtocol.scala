package cloud.benchflow.dsl.definition.types.bytes

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ unsupportedReadOperation, unsupportedWriteOperation }
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
object BytesYamlProtocol extends DefaultYamlProtocol {

  implicit object TimeReadFormat extends YamlFormat[Try[Bytes]] {

    override def read(yaml: YamlValue): Try[Bytes] = Bytes.fromString(yaml.convertTo[String])

    override def write(obj: Try[Bytes]): YamlValue = unsupportedWriteOperation

  }

  implicit object TimeWriteFormat extends YamlFormat[Bytes] {

    override def write(obj: Bytes): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): Bytes = unsupportedReadOperation
  }

}
