package cloud.benchflow.dsl.definition.configuration.strategy.validation

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{unsupportedReadOperation, unsupportedWriteOperation}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, DeserializationException, YamlFormat, YamlString, YamlValue, _}

import scala.util.{Failure, Success, Try}

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-21
 */
object ValidationStrategyTypeYamlProtocol extends DefaultYamlProtocol {

  implicit object ValidationStrategyTypeReadyFormat extends YamlFormat[Try[ValidationStrategyType]] {

    override def read(yaml: YamlValue): Try[ValidationStrategyType] = {

      val stringValue = yaml.asInstanceOf[YamlString].value.toLowerCase

      val optionStrategy: Option[ValidationStrategyType] = ValidationStrategyType.values.find(_.toString == stringValue)

      optionStrategy match {
        case Some(strategyType) => Success(strategyType)
        case None => Failure(DeserializationException("Unexpected validation strategy type"))
      }

    }

    override def write(obj: Try[ValidationStrategyType]): YamlValue = unsupportedWriteOperation

  }

  implicit object ValidationStrategyTypeWriteFormat extends YamlFormat[ValidationStrategyType] {

    override def write(obj: ValidationStrategyType): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): ValidationStrategyType = unsupportedReadOperation
  }

}
