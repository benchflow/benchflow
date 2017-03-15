package cloud.benchflow.dsl.definition.time

import java.time.Duration
import java.time.temporal.{ChronoUnit, TemporalUnit}

import scala.util.{Failure, Success, Try}

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 14.03.17.
  */
class Time(underlying: Duration, unit: TemporalUnit) {

  override def toString: String = {

    unit match {
//      case ChronoUnit.MICROS => Time.MicrosUnit
      case ChronoUnit.NANOS => underlying.getNano + Time.NanosUnit
      case ChronoUnit.MILLIS => underlying.getNano / 1000000 +  Time.MillisUnit
      case ChronoUnit.SECONDS => underlying.getSeconds + Time.SecondsUnit
      case ChronoUnit.MINUTES => underlying.getSeconds / 60 +  Time.MinutesUnit
      case ChronoUnit.HOURS => underlying.getSeconds / (60*60) + Time.HoursUnit

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

//  val MicrosUnit = "micros"
  val NanosUnit = "ns"
  val MillisUnit = "ms"
  val SecondsUnit = "s"
  val MinutesUnit = "m"
  val HoursUnit = "h"

  def fromString(string: String): Try[Time] = {

    // split between
    val array = string.replace(" ", "").split("(?<=\\d)(?=\\D)|(?=\\d)(?<=\\D)")

    if (array.length != 2) {
      Failure(new Exception("invalid BenchFlow time definition"))
    } else {

      val timeUnit: Try[ChronoUnit] = Try(array{1} match {

//        case MicrosUnit => ChronoUnit.MICROS
        case NanosUnit => ChronoUnit.NANOS
        case MillisUnit => ChronoUnit.MILLIS
        case SecondsUnit => ChronoUnit.SECONDS
        case MinutesUnit => ChronoUnit.MINUTES
        case HoursUnit => ChronoUnit.HOURS

      })

      timeUnit match {

        case Success(matchedUnit) => Try(new Time(Duration.of(array{0}.toLong, matchedUnit), matchedUnit))
        case Failure(ex) => Failure(ex)

      }
    }
  }
}

