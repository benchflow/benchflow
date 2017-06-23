package cloud.benchflow.dsl.definition.configuration.strategy.selection;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-23
 */
public enum SelectionStrategyType {
  ONE_AT_A_TIME("one-at-a-time"),
  RANDOM_BREAK_DOWN("random_breakdown"),
  BOUNDARY_FIRST("boundary_first"),
//  ADAPTIVE_EQUIDISTANT_BREAKDOWN("adaptive_equidistant_breakdown"),
//  ADAPTIVE_RANDOM_BREAK_DOWN("adaptive_random_breakdown")
  ;

  private final String stringValue;

  SelectionStrategyType(String stringValue) {
    this.stringValue = stringValue;
  }

  @Override
  public String toString() {
    return stringValue;
  }

}
