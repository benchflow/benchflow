package cloud.benchflow.dsl.definition.workload.interoperationtimingstype;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-23
 */
public enum InterOperationsTimingType {
  NEGATIVE_EXPONENTIAL("negative-exponential"),
  UNIFORM("uniform"),
  FIXED_TIME("fixed-time")
  ;


  private final String stringValue;

  InterOperationsTimingType(String stringValue) {
    this.stringValue = stringValue;
  }

  @Override
  public String toString() {
    return stringValue;
  }
}
