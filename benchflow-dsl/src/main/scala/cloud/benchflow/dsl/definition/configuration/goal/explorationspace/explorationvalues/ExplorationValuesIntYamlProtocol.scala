package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.explorationvalues

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.ExplorationSpaceYamlProtocol
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation }
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
object ExplorationValuesIntYamlProtocol extends DefaultYamlProtocol {

  val ValuesKey = YamlString("values")
  val RangeKey = YamlString("range")
  val StepKey = YamlString("step")

  val Level = s"${ExplorationSpaceYamlProtocol.Level}.(some int dimension)"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object ExplorationValuesIntReadFormat extends YamlFormat[Try[IntValues]] {

    override def read(yaml: YamlValue): Try[IntValues] = {

      val yamlObject = yaml.asYamlObject

      // all options stored as a list of values for easier use later on
      // TODO - handle case with range and step

      for {
        values <- deserializationHandler(
          yamlObject match {

            case _ if yamlObject.getFields(ValuesKey).nonEmpty =>
              yamlObject.getFields(ValuesKey).headOption.map(_.convertTo[List[Int]]).get

            case _ if yamlObject.getFields(RangeKey).nonEmpty => getList(
              yamlObject.getFields(RangeKey).headOption.map(_.convertTo[List[Int]]).get)

          },
          keyString(ValuesKey))

      } yield IntValues(values = values)

    }

    override def write(obj: Try[IntValues]): YamlValue = unsupportedWriteOperation
  }

  implicit object ExplorationSpaceUsersWriteFormat extends YamlFormat[IntValues] {

    override def write(obj: IntValues): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        ValuesKey -> obj.values.toYaml)

    }

    override def read(yaml: YamlValue): IntValues = unsupportedReadOperation

  }

  def getList(range: List[Int]): List[Int] = {

    (range.head to range(1)).toList

  }

}
