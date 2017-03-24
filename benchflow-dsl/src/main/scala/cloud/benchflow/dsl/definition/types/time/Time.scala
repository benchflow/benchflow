package cloud.benchflow.dsl.definition.types.time

import java.time.{Duration, LocalTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit._
import java.time.temporal.{ChronoUnit, TemporalUnit}

import cloud.benchflow.dsl.definition.types.time.Time._

import scala.util.{Failure, Success, Try}

/**
 *
 *
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 14.03.17.
 */
class Time(underlying: Duration, unit: TemporalUnit) {

  // TODO - document class
  // Why it cannot be extended to support MICROS natively
  // http://stackoverflow.com/questions/24491243/why-cant-i-get-a-duration-in-minutes-or-hours-in-java-time?answertab=votes#tab-top

  def toHoursPart: Long = underlying.toHours
  def toMinutesPart: Long = underlying.toMinutes
  def toSecondsPart: Long = underlying.getSeconds
  def toMillisPart: Long = underlying.toMillis
  def toMicrosPart: Long = underlying.getNano / 1000L
  def toNanosPart: Long = underlying.getNano

  override def toString: String = {

    LocalTime.MIDNIGHT.plus(underlying).format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    unit match {

      case NANOS => toNanosPart + NanosUnit
      case MICROS => toMicrosPart + MicrosUnit
      case MILLIS => toMillisPart + MillisUnit
      case SECONDS => toSecondsPart + SecondsUnit
      case MINUTES => toMinutesPart + MinutesUnit
      case HOURS => toHoursPart + HoursUnit

    }
  }

  override def equals(obj: scala.Any): Boolean = {

    obj match {
      case time: Time => this.toString.equals(time.toString)
      case _ => false
    }

  }
}

object Time {

  val MicrosUnit = "micros"
  val NanosUnit = "ns"
  val MillisUnit = "ms"
  val SecondsUnit = "s"
  val MinutesUnit = "m"
  val HoursUnit = "h"

  def fromString(string: String): Try[Time] = {

    // split between number and unit
    val array = string.replace(" ", "").split("(?<=\\d)(?=\\D)|(?=\\d)(?<=\\D)")

    if (array.length != 2) {
      Failure(new Exception("Invalid BenchFlow time definition (" + string + ")"))
    } else {

      val timeUnit: Try[ChronoUnit] = Try(array {
        1
      } match {

        case MicrosUnit => MICROS
        case NanosUnit => NANOS
        case MillisUnit => MILLIS
        case SecondsUnit => SECONDS
        case MinutesUnit => MINUTES
        case HoursUnit => HOURS

      })

      timeUnit match {

        case Success(matchedUnit) => Try(new Time(Duration.of(array {
          0
        }.toLong, matchedUnit), matchedUnit))
        case Failure(ex) => Failure(ex)

      }
    }
  }
}

