package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.users

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.ExplorationSpaceYamlProtocol
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.step.RangeWithStep
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation }
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _ }

import scala.util.{ Failure, Success, Try }

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
object UserValuesYamlProtocol extends DefaultYamlProtocol {

  val ValuesKey = YamlString("values")
  val RangeKey = YamlString("range")
  val StepKey = YamlString("step")

  val Level = s"${ExplorationSpaceYamlProtocol.Level}.(some int dimension)"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object UserValuesReadFormat extends YamlFormat[Try[UserValues]] {

    override def read(yaml: YamlValue): Try[UserValues] = {

      val yamlObject = yaml.asYamlObject

      // all options stored as a list of values for easier use later on

      for {
        values <- deserializationHandler(
          yamlObject match {

            // handle values specification
            case _ if yamlObject.getFields(ValuesKey).nonEmpty =>
              assertValidValues(
                yamlObject.getFields(ValuesKey).headOption.map(_.convertTo[List[Int]]).get).get

            // handle step and range specification
            case _ if yamlObject.getFields(StepKey).nonEmpty && yamlObject.getFields(RangeKey).nonEmpty =>
              getStepList(
                yamlObject.getFields(RangeKey).headOption.map(_.convertTo[List[Int]]).get,
                yamlObject.getFields(StepKey).headOption.map(_.convertTo[String]).get).get

            // handle range only specification
            case _ if yamlObject.getFields(RangeKey).nonEmpty =>
              getRangeList(
                yamlObject.getFields(RangeKey).headOption.map(_.convertTo[List[Int]]).get).get

          },
          keyString(ValuesKey))

      } yield UserValues(values = values)

    }

    override def write(obj: Try[UserValues]): YamlValue = unsupportedWriteOperation
  }

  implicit object UserValuesWriteFormat extends YamlFormat[UserValues] {

    override def write(obj: UserValues): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        ValuesKey -> obj.values.toYaml)

    }

    override def read(yaml: YamlValue): UserValues = unsupportedReadOperation

  }

  private def assertValidValues(values: List[Int]): Try[List[Int]] = {

    // check that all values are in increasing order
    val increasingOrder = values.sliding(2).forall {
      case List(x, y) => x < y
    }

    if (!increasingOrder) {
      Failure(new BenchFlowDeserializationException("values must be increasing", new Throwable))
    } else if (values.head <= 0) {
      Failure(new BenchFlowDeserializationException("values must be > 0", new Throwable))
    } else {
      Success(values)
    }

  }

  private def getRangeList(range: List[Int]): Try[List[Int]] = {

    if (range.size != 2) {
      Failure(new BenchFlowDeserializationException("range must be specified as a List of 2 elements", new Throwable))
    } else if (range.head >= range.last) {
      Failure(new BenchFlowDeserializationException("range must be increasing", new Throwable))
    } else if (range.head <= 0) {
      Failure(new BenchFlowDeserializationException("range must be > 0", new Throwable))
    } else {
      // IF only range then the step is 1
      Success((range.head to range(1)).toList)
    }

  }

  private def getStepList(range: List[Int], step: String): Try[List[Int]] = {

    if (range.size != 2) {
      Failure(new BenchFlowDeserializationException("range must be specified as a List of 2 elements", new Throwable))
    } else if (range.head >= range.last) {
      Failure(new BenchFlowDeserializationException("range must be increasing", new Throwable))
    } else if (range.head <= 0) {
      Failure(new BenchFlowDeserializationException("range must be > 0", new Throwable))
    } else {

      val prefix = step.substring(0, 1)

      val triedValue = Try(step.substring(1).toInt)

      triedValue match {

        case Success(value) =>
          if (value <= 0) {
            Failure(new BenchFlowDeserializationException("step must be > 0", new Throwable))
          } else {
            generateStepList(range, value, prefix)
          }

        case Failure(ex) => Failure(new BenchFlowDeserializationException("step must contain an integer", ex))

      }

    }

  }

  private def generateStepList(range: List[Int], step: Int, prefix: String): Try[List[Int]] = prefix match {
    case "+" =>
      if (step == 0) {
        Failure(new BenchFlowDeserializationException("step function cannot be identity element: +0", new Throwable))
      } else {
        RangeWithStep.stepList(range, step, (a: Int, b: Int) => a + b)
      }

    case "*" =>
      if (step == 1) {
        Failure(new BenchFlowDeserializationException("step function cannot be identity element: *1", new Throwable))
      } else {
        RangeWithStep.stepList(range, step, (a: Int, b: Int) => a * b)
      }

    case "^" =>
      if (step == 1) {
        Failure(new BenchFlowDeserializationException("step function cannot be ^1", new Throwable))
      } else {
        RangeWithStep.stepList(range, step, (a: Int, b: Int) => Math.pow(a, b).intValue)
      }
    case _ => Failure(new BenchFlowDeserializationException("step operation not supported", new Throwable))
  }
}
