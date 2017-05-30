package cloud.benchflow.dsl.definition.version

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ unsupportedReadOperation, unsupportedWriteOperation }
import cloud.benchflow.dsl.definition.version.Version.Version
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, DeserializationException, YamlFormat, YamlString, YamlValue, _ }

import scala.util.{ Failure, Success, Try }

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-24
 */
object VersionYamlProtocol extends DefaultYamlProtocol {

  implicit object GoalTypeReadFormat extends YamlFormat[Try[Version]] {

    override def read(yaml: YamlValue): Try[Version] = {

      val stringValue = yaml.asInstanceOf[YamlString].value.toLowerCase

      val optionGoal: Option[Version] = Version.values.find(_.toString == stringValue)

      optionGoal match {
        case Some(goalType) => Success(goalType)
        case None => Failure(DeserializationException("Unexpected version"))
      }

    }

    override def write(obj: Try[Version]): YamlValue = unsupportedWriteOperation

  }

  implicit object GoalTypeWriteFormat extends YamlFormat[Version] {
    override def write(obj: Version): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): Version = unsupportedReadOperation
  }

}
