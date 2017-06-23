package cloud.benchflow.dsl.definition.configuration.strategy.selection

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ unsupportedReadOperation, unsupportedWriteOperation }
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, DeserializationException, YamlFormat, YamlString, YamlValue, _ }

import scala.util.{ Failure, Success, Try }

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-21
 */
object SelectionStrategyTypeYamlProtocol extends DefaultYamlProtocol {

  implicit object SelectionStrategyTypeReadyFormat extends YamlFormat[Try[SelectionStrategyType]] {

    override def read(yaml: YamlValue): Try[SelectionStrategyType] = {

      val stringValue = yaml.asInstanceOf[YamlString].value.toLowerCase

      val optionStrategy: Option[SelectionStrategyType] = SelectionStrategyType.values.find(_.toString == stringValue)

      optionStrategy match {
        case Some(strategyType) => Success(strategyType)
        case None => Failure(DeserializationException("Unexpected selection strategy type"))
      }

    }

    override def write(obj: Try[SelectionStrategyType]): YamlValue = unsupportedWriteOperation

  }

  implicit object SelectionStrategyTypeWriteFormat extends YamlFormat[SelectionStrategyType] {

    override def write(obj: SelectionStrategyType): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): SelectionStrategyType = unsupportedReadOperation
  }

}
