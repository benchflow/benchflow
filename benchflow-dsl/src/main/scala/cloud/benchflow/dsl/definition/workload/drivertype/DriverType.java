package cloud.benchflow.dsl.definition.workload.drivertype;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-23
 */
public enum DriverType {
  START("start"),
  HTTP("http")
  ;

  private final String stringValue;

  DriverType(String stringValue) {
    this.stringValue = stringValue;
  }

  @Override
  public String toString() {
    return stringValue;
  }

}
