package cloud.benchflow.dsl.definition.configuration.strategy.regression

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{unsupportedReadOperation, unsupportedWriteOperation}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, DeserializationException, YamlFormat, YamlString, YamlValue, _}

import scala.util.{Failure, Success, Try}

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-21
 */
object RegressionStrategyTypeYamlProtocol extends DefaultYamlProtocol {

  implicit object RegressionStrategyTypeReadFormat extends YamlFormat[Try[RegressionStrategyType]] {

    override def read(yaml: YamlValue): Try[RegressionStrategyType] = {

      val stringValue = yaml.asInstanceOf[YamlString].value.toLowerCase

      val optionStrategy: Option[RegressionStrategyType] = RegressionStrategyType.values.find(_.toString == stringValue)

      optionStrategy match {
        case Some(strategyType) => Success(strategyType)
        case None => Failure(DeserializationException("Unexpected regression strategy type"))
      }

    }

    override def write(obj: Try[RegressionStrategyType]): YamlValue = unsupportedWriteOperation

  }

  implicit object RegressionStrategyTypeWriteFormat extends YamlFormat[RegressionStrategyType] {

    override def write(obj: RegressionStrategyType): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): RegressionStrategyType = unsupportedReadOperation
  }

}
