package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.users

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.WorkloadExplorationSpaceYamlProtocol
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation}
import cloud.benchflow.dsl.definition.yamlprotocol.valuesrange.MonotonicPositiveIntValuesRangeYamlProtocol
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.{Failure, Success, Try}

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
object UserValuesYamlProtocol extends DefaultYamlProtocol with MonotonicPositiveIntValuesRangeYamlProtocol {

  val Level = s"${WorkloadExplorationSpaceYamlProtocol.Level}.${WorkloadExplorationSpaceYamlProtocol.UsersKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  // TODO: Move to traits (In particular an Int trait)
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

  /*
   * We customize the default step for Users to be +1 or -1
   */
  protected override def getRangeList(range: List[Int]): Try[List[Int]] = {

    val rangeValid = super.assertRangeIsValid(range)

    if (rangeValid.isSuccess) {
      // IF only range then the step is 1
      if(range.head > range.last)
        Success((range.head to range.last by -1).toList)
      //TODO: check it works when applying https://github.com/benchflow/benchflow/issues/397#issuecomment-314408192
      else
        Success((range.head to range.last by 1).toList)
    } else {
      Failure(rangeValid.failed.get)
    }

  }

}
