package cloud.benchflow.dsl.definition.configuration.goal.goaltype

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ unsupportedReadOperation, unsupportedWriteOperation }
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, DeserializationException, YamlFormat, YamlString, YamlValue, _ }

import scala.util.{ Failure, Success, Try }

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-22
 */
object GoalTypeYamlProtocol extends DefaultYamlProtocol {

  implicit object GoalTypeReadFormat extends YamlFormat[Try[GoalType]] {

    override def read(yaml: YamlValue): Try[GoalType] = {

      val stringValue = yaml.asInstanceOf[YamlString].value.toLowerCase

      val optionGoal: Option[GoalType] = GoalType.values.find(_.toString == stringValue)

      optionGoal match {
        case Some(goalType) => Success(goalType)
        case None => Failure(DeserializationException("Unexpected goal type"))
      }

    }

    override def write(obj: Try[GoalType]): YamlValue = unsupportedWriteOperation

  }

  implicit object GoalTypeWriteFormat extends YamlFormat[GoalType] {
    override def write(obj: GoalType): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): GoalType = unsupportedReadOperation
  }

}
