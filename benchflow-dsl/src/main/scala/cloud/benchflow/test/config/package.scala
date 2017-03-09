package cloud.benchflow.test

import cloud.benchflow.test.config.sut.http.Http
import cloud.benchflow.test.config.sut.wfms.WfMS

import scala.util.{Failure, Success, Try}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 18/07/16.
  */
package object config {

  /***
    * Has to be extended for each supported version format
    */
  trait Version { def isCompatible(other: Version): Boolean }
  object Version {

    import com.github.zafarkhaja.semver.{Version => LibVersion}

    case class SemanticVersionRange(low: SemanticVersion, high: SemanticVersion) extends Version { range =>

      override def toString = s"$low-$high"

      override def isCompatible(other: Version): Boolean = other match {
        case semVer: SemanticVersion => semVer.isCompatible(range)
        case _ => false //not interested in comparing the other possibilities
      }
    }

    case class StringVersion(v: String) extends Version {

      override def toString = v

      override def isCompatible(other: Version): Boolean = other match {
        case StringVersion(otherV) => otherV == v
        case _ => false
      }
    }

    case class SemanticVersion(version: LibVersion) extends Version with Ordered[SemanticVersion] {

      override def toString = version.toString

      override def isCompatible(other: Version): Boolean =
        other match {
          case SemanticVersion(v) => version == v
          case SemanticVersionRange(low, high) =>
            version.greaterThanOrEqualTo(low.version) &&
              version.lessThanOrEqualTo(high.version)
        }

      override def compare(that: SemanticVersion): Int = version.compareTo(that.version)
    }

    private val singleVersion = "([0-9]+\\.[0-9]+\\.[0-9]+.*)".r
    private val rangedVersionPattern = s"$singleVersion-$singleVersion".r

    def apply(v: String) = v match {

      case rangedVersionPattern(low, high) =>
        Try(LibVersion.valueOf(low)) match {
          case Success(l) =>
            val semLow = SemanticVersion(l)
            val semHigh = SemanticVersion(LibVersion.valueOf(high))
            SemanticVersionRange(semLow, semHigh)
          case Failure(ex) => //not a semantic version
            throw new Exception("BenchFlow doesn't support ranges for non SemVer versioning.")
        }

      case singleVersion(version) =>
        Try(LibVersion.valueOf(version)) match {
          case Success(semver) => SemanticVersion(semver)
          case Failure(_) => StringVersion(version)
        }

      case _ => StringVersion(v)
      //throw new Exception("Unrecognized version format.")
    }

  }

  /***
    * Has to be extended for each SUT type
    */
  trait SutsType
  object SutsType {

    def apply(sutsType: String): SutsType = sutsType.toLowerCase match {
      case "wfms" => WfMS
      case "http" => Http
      case _ => throw new Exception("Illegal value for field suts_type; possible values: wfms, http")
    }
  }

  case class Sut(name: String, version: Version, sutsType: SutsType)

  case class Properties(properties: Map[String, Any])

  /***
    * Abstract operation model
    */
  abstract class Operation(val name: String, val data: Option[String])

  /**
    * Possible mixes
    */
  sealed abstract class Mix(deviation: Option[Double])

  //This mix maintains the state of execution.
  //It chooses the next operation based on the current operation and a given probability ratio.
  case class MatrixMixRow(row: Seq[Double])
  case class MatrixMix(rows: Seq[MatrixMixRow], deviation: Option[Double]) extends Mix(deviation)

  //This mix randomly chooses the next operation to execute based on given probability for the mix.
  case class FlatMix(opsMix: Seq[Double], deviation: Option[Double]) extends Mix(deviation)

  //The fixed sequence does what it says. There is no randomness. The operations are called in sequence.
  case class FixedSequenceMix(sequence: Seq[String], deviation: Option[Double]) extends Mix(deviation)

  //This mix allows random selection of fixed sequences (as opposed to random selection of an operation in FlatMix).
  case class FlatSequenceMixRow(row: Seq[String])
  case class FlatSequenceMix(opsMix: Seq[Double],
                             rows: Seq[FlatSequenceMixRow],
                             deviation: Option[Double]) extends Mix(deviation)

  sealed trait DriverMetric //TODO: possible values will be: ops/sec, req/s(?)

  case class DriverConfiguration(max90th: Option[Double], mix: Option[Mix], popularity: Option[Float])


  /**
    * Abstract driver model
    */
  abstract class Driver[A <: Operation](val properties: Option[Properties],
                                               val operations: Seq[A],
                                               val configuration: Option[DriverConfiguration])


  case class TotalTrials(trials: Int)


  case class Deploy(deploy: Map[String, String]) {
    def get(serviceName: String) = deploy.get(serviceName)
  }

  case class Binding(boundService: String, config: Option[Properties])

  case class BenchFlowConfig(benchflow_config: Map[String, Seq[Binding]]) {
    def bindings(serviceName: String) = benchflow_config.getOrElse(serviceName, Seq())
  }

  case class TargetService(name: String, endpoint: String, sutReadyLogCheck: Option[String] = None)

  case class SutConfiguration(targetService: TargetService,
                              deploy: Deploy,
                              bfConfig: BenchFlowConfig)

  case class LoadFunction(rampUp: Int, steadyState: Int, rampDown: Int)

}
