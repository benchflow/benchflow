package cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.criteriatype;

/**
  * @author Jesper Findahl (jesper.findahl@gmail.com) 
  *         created on 2017-06-22
  */
public enum CriteriaType {
  FIXED("fixed");
//  CONFIDENCE_INTERVAL("confidence-interval")

  private final String stringValue;

  CriteriaType(String stringValue) {
    this.stringValue = stringValue;
  }

  @Override
  public String toString() {
    return stringValue;
  }
}
