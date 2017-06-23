package cloud.benchflow.dsl.definition.configuration.terminationcriteria.exploration.explorationtype;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-23
 */
public enum ExplorationType {
  FIXED("fixed"),
  TIME("time"),
  PRECISION("precision"),
  MAX_FAILED("max_failed")
  ;

  private final String stringValue;

  ExplorationType(String stringValue) {
    this.stringValue = stringValue;
  }

  @Override
  public String toString() {
    return stringValue;
  }

}
