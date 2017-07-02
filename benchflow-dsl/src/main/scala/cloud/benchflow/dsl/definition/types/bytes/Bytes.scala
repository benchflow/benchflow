package cloud.benchflow.dsl.definition.types.bytes

import scala.util.{Failure, Success, Try}

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
class Bytes(val underlying: Int, val unit: BytesUnit) {
  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case bytes: Bytes => this.toString.equals(bytes.toString)
      case _ => false
    }
  }

  override def toString: String = s"$underlying$unit"
}

object Bytes {

  def fromString(string: String): Try[Bytes] = {

    // split between number and unit
    val array = string.replace(" ", "").split("(?<=\\d)(?=\\D)|(?=\\d)(?<=\\D)")

    val exception = new Exception("Invalid BenchFlow Bytes definition (" + string + ")")

    if (array.length != 2) {
      Failure(exception)
    } else {

      val optionUnit: Option[BytesUnit] = BytesUnit.values.find(_.toString == array(1))

      optionUnit match {
        case Some(unit) => Success(new Bytes(array(0).toInt, unit))
        case None => Failure(exception)
      }

    }

  }

}
