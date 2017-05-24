package cloud.benchflow.dsl.definition.sut.suttype

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ unsupportedReadOperation, unsupportedWriteOperation }
import cloud.benchflow.dsl.definition.sut.suttype.SutType.SutType
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, DeserializationException, YamlFormat, YamlString, YamlValue, _ }

import scala.util.{ Failure, Success, Try }

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-24
 */
object SutTypeYamlProtocol extends DefaultYamlProtocol {

  implicit object GoalTypeReadFormat extends YamlFormat[Try[SutType]] {

    override def read(yaml: YamlValue): Try[SutType] = {

      val stringValue = yaml.asInstanceOf[YamlString].value.toLowerCase

      val optionGoal: Option[SutType] = SutType.values.find(_.toString == stringValue)

      optionGoal match {
        case Some(goalType) => Success(goalType)
        case None => Failure(DeserializationException("Unexpected sut type"))
      }

    }

    override def write(obj: Try[SutType]): YamlValue = unsupportedWriteOperation

  }

  implicit object GoalTypeWriteFormat extends YamlFormat[SutType] {
    override def write(obj: SutType): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): SutType = unsupportedReadOperation
  }

}
