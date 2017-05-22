package cloud.benchflow.dsl.definition.sut

import scala.util.{ Failure, Success, Try }

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 *
 * Has to be extended for each supported version format
 */
trait Version { def isCompatible(other: Version): Boolean }
object Version {

  import com.github.zafarkhaja.semver.{ Version => LibVersion }

  case class SemanticVersionRange(low: SemanticVersion, high: SemanticVersion) extends Version { range =>

    override def toString: String = s"$low-$high"

    override def isCompatible(other: Version): Boolean = other match {
      case semVer: SemanticVersion => semVer.isCompatible(range)
      case _ => false //not interested in comparing the other possibilities
    }
  }

  case class StringVersion(v: String) extends Version {

    override def toString: String = v

    override def isCompatible(other: Version): Boolean = other match {
      case StringVersion(otherV) => otherV == v
      case _ => false
    }
  }

  case class SemanticVersion(version: LibVersion) extends Version with Ordered[SemanticVersion] {

    override def toString: String = version.toString

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

  def apply(v: String): Version = v match {

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
