package cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowTestTerminationCriteriaYamlProtocol
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowTestTerminationCriteriaYamlProtocol.ExperimentKey
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation }
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
object ExperimentTerminationCriteriaYamlProtocol extends DefaultYamlProtocol {

  val TypeKey = YamlString("type")
  val NumberKey = YamlString("number")

  val Level = s"${BenchFlowTestTerminationCriteriaYamlProtocol.Level}.${ExperimentKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object ExperimentTerminationCriteriaReadFormat extends YamlFormat[Try[ExperimentTerminationCriteria]] {
    override def read(yaml: YamlValue): Try[ExperimentTerminationCriteria] = {

      val yamlObject = yaml.asYamlObject

      for {

        criteriaType <- deserializationHandler(
          yamlObject.fields(TypeKey).convertTo[String],
          keyString(TypeKey))

        number <- deserializationHandler(
          yamlObject.fields(NumberKey).convertTo[Int],
          keyString(NumberKey))

      } yield ExperimentTerminationCriteria(criteriaType = criteriaType, number = number)

    }

    override def write(obj: Try[ExperimentTerminationCriteria]): YamlValue = unsupportedWriteOperation

  }

  implicit object ExperimentTerminationCriteriaWriteFormat extends YamlFormat[ExperimentTerminationCriteria] {

    override def write(obj: ExperimentTerminationCriteria): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        TypeKey -> obj.criteriaType.toYaml,
        NumberKey -> obj.number.toYaml)

    }

    override def read(yaml: YamlValue): ExperimentTerminationCriteria = unsupportedReadOperation
  }

}
