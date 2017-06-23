package cloud.benchflow.dsl.definition.sut.suttype;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-23
 */
public enum SutType {
  WFMS("wfms"),
  HTTP("http")
  ;

  private final String stringValue;

  SutType(String stringValue) {
    this.stringValue = stringValue;
  }

  @Override
  public String toString() {
    return stringValue;
  }
}
