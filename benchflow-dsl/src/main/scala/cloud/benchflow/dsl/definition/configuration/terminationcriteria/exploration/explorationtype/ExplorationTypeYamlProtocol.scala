package cloud.benchflow.dsl.definition.configuration.terminationcriteria.exploration.explorationtype

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{unsupportedReadOperation, unsupportedWriteOperation}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, DeserializationException, YamlFormat, YamlString, YamlValue, _}

import scala.util.{Failure, Success, Try}

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-22
 */
object ExplorationTypeYamlProtocol extends DefaultYamlProtocol {

  implicit object ExplorationTypeReadFormat extends YamlFormat[Try[ExplorationType]] {

    override def read(yaml: YamlValue): Try[ExplorationType] = {

      val stringValue = yaml.asInstanceOf[YamlString].value.toLowerCase

      val optionStrategy: Option[ExplorationType] = ExplorationType.values.find(_.toString == stringValue)

      optionStrategy match {
        case Some(explorationType) => Success(explorationType)
        case None => Failure(DeserializationException("Unexpected exploration type"))
      }

    }

    override def write(obj: Try[ExplorationType]): YamlValue = unsupportedWriteOperation
  }

  implicit object ExplorationTypeWriteFormat extends YamlFormat[ExplorationType] {

    override def write(obj: ExplorationType): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): ExplorationType = unsupportedReadOperation
  }

}
