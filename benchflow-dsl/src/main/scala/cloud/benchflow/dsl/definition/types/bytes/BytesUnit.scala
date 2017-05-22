package cloud.benchflow.dsl.definition.types.bytes

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
object BytesUnit extends Enumeration {
  type BytesUnit = Value
  val Bytes = Value("b")
  val KiloBytes = Value("k")
  val MegaBytes = Value("m")
  val GigaBytes = Value("g")
}
