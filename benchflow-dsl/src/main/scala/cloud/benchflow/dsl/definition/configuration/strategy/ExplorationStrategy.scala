package cloud.benchflow.dsl.definition.configuration.strategy

import cloud.benchflow.dsl.definition.configuration.strategy.regression.RegressionStrategyType.RegressionStrategyType
import cloud.benchflow.dsl.definition.configuration.strategy.selection.SelectionStrategyType.SelectionStrategyType
import cloud.benchflow.dsl.definition.configuration.strategy.validation.ValidationStrategyType.ValidationStrategyType

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-21
 */
case class ExplorationStrategy(selection: SelectionStrategyType, validation: Option[ValidationStrategyType], regression: Option[RegressionStrategyType])
