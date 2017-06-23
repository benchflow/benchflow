package cloud.benchflow.dsl.definition.configuration.goal.goaltype;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-23
 */
public enum GoalType {
  LOAD("load"),
//  OPTIMAL_CONFIGURATION("optimal_configuration"),
  EXHAUSTIVE_EXPLORATION("exhaustive_exploration"),
  PREDICTIVE_EXPLORATION("predictive_exploration");

  private final String stringValue;

  GoalType(String stringValue) {
    this.stringValue = stringValue;
  }

  @Override
  public String toString() {
    return stringValue;
  }

}
