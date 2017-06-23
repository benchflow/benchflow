package cloud.benchflow.dsl.definition.types.bytes;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-23
 */
public enum BytesUnit {
  BYTES("b"),
  KILO_BYTES("k"),
  MEGA_BYTES("m"),
  GIGA_BYTES("g")
  ;

  private final String stringValue;

  BytesUnit(String stringValue) {
    this.stringValue = stringValue;
  }

  @Override
  public String toString() {
    return stringValue;
  }

}
