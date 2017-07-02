package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.explorationvalues

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.ExplorationSpaceYamlProtocol
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation}
import cloud.benchflow.dsl.definition.types.bytes.Bytes
import cloud.benchflow.dsl.definition.types.bytes.BytesYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
object ExplorationValuesBytesYamlProtocol extends DefaultYamlProtocol {

  val ValuesKey = YamlString("values")
  val RangeKey = YamlString("range")
  val StepKey = YamlString("step")

  val Level = s"${ExplorationSpaceYamlProtocol.Level}.(some byte dimension)"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object ExplorationValuesIntReadFormat extends YamlFormat[Try[ByteValues]] {

    override def read(yaml: YamlValue): Try[ByteValues] = {

      val yamlObject = yaml.asYamlObject

      // all options stored as a list of values for easier use later on
      // TODO - handle case with range and step

      for {
        values <- deserializationHandler(
          yamlObject match {

            case _ if yamlObject.getFields(ValuesKey).nonEmpty =>
              yamlObject.getFields(ValuesKey).headOption.map(_.convertTo[List[Try[Bytes]]].map(_.get)).get

            case _ if yamlObject.getFields(RangeKey).nonEmpty => getList(
              yamlObject.getFields(RangeKey).headOption.map(_.convertTo[List[Try[Bytes]]].map(_.get)).get)

          },
          keyString(ValuesKey))

      } yield ByteValues(values = values)

    }

    override def write(obj: Try[ByteValues]): YamlValue = unsupportedWriteOperation
  }

  implicit object ExplorationSpaceUsersWriteFormat extends YamlFormat[ByteValues] {

    override def write(obj: ByteValues): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        ValuesKey -> obj.values.toYaml)

    }

    override def read(yaml: YamlValue): ByteValues = unsupportedReadOperation

  }

  def getList(range: List[Bytes]): List[Bytes] = {

    val values = range.map(_.underlying)

    (values.head to values(1)).toList.map(v => new Bytes(v, range.head.unit))

  }

}
