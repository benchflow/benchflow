package cloud.benchflow.dsl.definition.workload.interoperationtimingstype

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ unsupportedReadOperation, unsupportedWriteOperation }
import cloud.benchflow.dsl.definition.workload.interoperationtimingstype.InterOperationsTimingType.InterOperationsTimingType
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlValue, _ }

import scala.util.{ Failure, Success, Try }

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
object InterOperationsTimingTypeYamlProtocol extends DefaultYamlProtocol {

  implicit object InterOperationsTimingTypeReadFormat extends YamlFormat[Try[InterOperationsTimingType]] {

    override def read(yaml: YamlValue): Try[InterOperationsTimingType] = {

      val stringValue = yaml.asInstanceOf[YamlString].value.toLowerCase

      val optionTimingType: Option[InterOperationsTimingType] = InterOperationsTimingType.values.find(_.toString == stringValue)

      optionTimingType match {
        case Some(timingType) => Success(timingType)
        case None => Failure(DeserializationException("Unexpected inter operations timing type"))
      }

    }

    override def write(obj: Try[InterOperationsTimingType]): YamlValue = unsupportedWriteOperation

  }

  implicit object InterOperationsTimingTypeWriteFormat extends YamlFormat[InterOperationsTimingType] {

    override def write(obj: InterOperationsTimingType): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): InterOperationsTimingType = unsupportedReadOperation
  }

}
