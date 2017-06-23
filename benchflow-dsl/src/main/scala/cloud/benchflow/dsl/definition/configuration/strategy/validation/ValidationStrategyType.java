package cloud.benchflow.dsl.definition.configuration.strategy.validation;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-23
 */
public enum  ValidationStrategyType {
  RANDOM_VALIDATION_SET("random-validation-set"),
//  DYNAMIC_SECTOR("dynamic-sector"),
//  TEN_FOLD_VALIDATION("ten-fold-validation")
  ;

  private final String stringValue;

  ValidationStrategyType(String stringValue) {
    this.stringValue = stringValue;
  }

  @Override
  public String toString() {
    return stringValue;
  }

}
