package cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.criteriatype

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{unsupportedReadOperation, unsupportedWriteOperation}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlValue, _}

import scala.util.{Failure, Success, Try}

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-22
 */
object CriteriaTypeYamlProtocol extends DefaultYamlProtocol {

  implicit object CriteriaTypeReadFormat extends YamlFormat[Try[CriteriaType]] {
    override def read(yaml: YamlValue): Try[CriteriaType] = {

      val stringValue = yaml.asInstanceOf[YamlString].value.toLowerCase

      val optionCriteriaType: Option[CriteriaType] = CriteriaType.values.find(_.toString == stringValue)

      optionCriteriaType match {
        case Some(criteriaType) => Success(criteriaType)
        case None => Failure(DeserializationException("Unexpected experiment criteria type"))
      }

    }

    override def write(obj: Try[CriteriaType]): YamlValue = unsupportedWriteOperation
  }

  implicit object CriteriaTypeWriterFormat extends YamlFormat[CriteriaType] {

    override def write(obj: CriteriaType): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): CriteriaType = unsupportedReadOperation
  }

}
