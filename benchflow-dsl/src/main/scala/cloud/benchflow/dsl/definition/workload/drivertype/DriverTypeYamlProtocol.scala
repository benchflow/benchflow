package cloud.benchflow.dsl.definition.workload.drivertype

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ unsupportedReadOperation, unsupportedWriteOperation }
import cloud.benchflow.dsl.definition.workload.drivertype.DriverType.DriverType
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, DeserializationException, YamlFormat, YamlString, YamlValue, _ }

import scala.util.{ Failure, Success, Try }

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
object DriverTypeYamlProtocol extends DefaultYamlProtocol {

  implicit object DriverTypeReadFormat extends YamlFormat[Try[DriverType]] {

    override def read(yaml: YamlValue): Try[DriverType] = {

      val stringValue = yaml.asInstanceOf[YamlString].value.toLowerCase

      val optionDriverType: Option[DriverType] = DriverType.values.find(_.toString == stringValue)

      optionDriverType match {
        case Some(driverType) => Success(driverType)
        case None => Failure(DeserializationException("Unexpected driver type"))
      }

    }

    override def write(obj: Try[DriverType]): YamlValue = unsupportedWriteOperation
  }

  implicit object DriverTypeWriteFormat extends YamlFormat[DriverType] {

    override def write(obj: DriverType): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): DriverType = unsupportedReadOperation

  }

}
