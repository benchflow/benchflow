package cloud.benchflow.dsl.definition.configuration.strategy.validation

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-21
 */
object ValidationStrategyType extends Enumeration {
  type ValidationStrategyType = Value
  val RandomValidationSet = Value("random-validation-set")
  //  val DynamicSector = Value("dynamic-sector")
  //  val TenFoldValidation = Value("ten-fold-validation")
}
