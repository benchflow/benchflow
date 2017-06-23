package cloud.benchflow.dsl.definition.configuration.strategy.regression;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-23
 */
public enum  RegressionStrategyType {
  MARS("mars"),
  AUTOMATIC("automatic"),
  CART("cart"),
  KRIGING("kriging"),
  GENETIC_PROGRAMMING("genetic-programming")
  ;

  private final String stringValue;

  RegressionStrategyType(String stringValue) {
    this.stringValue = stringValue;
  }

  @Override
  public String toString() {
    return stringValue;
  }

}
