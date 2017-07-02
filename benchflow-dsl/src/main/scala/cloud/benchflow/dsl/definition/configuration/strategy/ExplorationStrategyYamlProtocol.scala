package cloud.benchflow.dsl.definition.configuration.strategy

import cloud.benchflow.dsl.definition.configuration.BenchFlowTestConfigurationYamlProtocol
import cloud.benchflow.dsl.definition.configuration.BenchFlowTestConfigurationYamlProtocol.StrategyKey
import cloud.benchflow.dsl.definition.configuration.strategy.regression.RegressionStrategyType
import cloud.benchflow.dsl.definition.configuration.strategy.regression.RegressionStrategyTypeYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.strategy.selection.SelectionStrategyType
import cloud.benchflow.dsl.definition.configuration.strategy.selection.SelectionStrategyTypeYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.strategy.validation.ValidationStrategyType
import cloud.benchflow.dsl.definition.configuration.strategy.validation.ValidationStrategyTypeYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler._
import net.jcazevedo.moultingyaml.{YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-21
 */
object ExplorationStrategyYamlProtocol extends DefaultYamlProtocol {

  val SelectionKey = YamlString("selection")
  val ValidationKey = YamlString("validation")
  val RegressionKey = YamlString("regression")

  val Level = s"${BenchFlowTestConfigurationYamlProtocol.Level}.${StrategyKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object ExplorationStrategyReadFormat extends YamlFormat[Try[ExplorationStrategy]] {

    override def read(yaml: YamlValue): Try[ExplorationStrategy] = {

      val yamlObject = yaml.asYamlObject

      for {

        selection <- deserializationHandler(
          yamlObject.fields(SelectionKey).convertTo[Try[SelectionStrategyType]].get,
          keyString(SelectionKey))

        validation <- deserializationHandler(
          yamlObject.getFields(ValidationKey).headOption.map(_.convertTo[Try[ValidationStrategyType]].get),
          keyString(ValidationKey))

        regression <- deserializationHandler(
          yamlObject.getFields(RegressionKey).headOption.map(_.convertTo[Try[RegressionStrategyType]].get),
          keyString(RegressionKey))

      } yield ExplorationStrategy(
        selection = selection,
        validation = validation,
        regression = regression)

    }

    override def write(obj: Try[ExplorationStrategy]): YamlValue = unsupportedWriteOperation
  }

  implicit object ExplorationStrategyWriteFormat extends YamlFormat[ExplorationStrategy] {

    override def write(obj: ExplorationStrategy): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        SelectionKey -> obj.selection.toYaml) ++
        obj.validation.map(key => ValidationKey -> key.toYaml) ++
        obj.regression.map(key => RegressionKey -> key.toYaml)

    }

    override def read(yaml: YamlValue): ExplorationStrategy = unsupportedReadOperation
  }

}
