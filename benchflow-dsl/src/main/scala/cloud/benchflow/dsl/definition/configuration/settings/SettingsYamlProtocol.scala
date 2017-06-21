package cloud.benchflow.dsl.definition.configuration.settings

import cloud.benchflow.dsl.definition.configuration.BenchFlowTestConfigurationYamlProtocol
import cloud.benchflow.dsl.definition.configuration.BenchFlowTestConfigurationYamlProtocol.SettingsKey
import cloud.benchflow.dsl.definition.configuration.settings.SettingsObject.{ Settings, StoredKnowledgeDefaultValue }
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation }
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-22
 */
object SettingsYamlProtocol extends DefaultYamlProtocol {

  val StoredKnowledgeKey = YamlString("stored_knowledge")

  val Level = s"${BenchFlowTestConfigurationYamlProtocol.Level}.${SettingsKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object SettingsYamlReadFormat extends YamlFormat[Try[Settings]] {

    override def read(yaml: YamlValue): Try[Settings] = {

      yaml match {

        // IF we have an empty string we provide the default value
        case YamlNull => Try(Settings(storedKnowledge = StoredKnowledgeDefaultValue))

        // ELSE we do the normal deserialization
        case _ =>

          val yamlObject = yaml.asYamlObject

          for {

            storedKnowledge <- deserializationHandler(
              yamlObject.getFields(StoredKnowledgeKey).headOption match {
                case Some(storedKnowledgeValue) => storedKnowledgeValue.convertTo[Boolean]
                case None => StoredKnowledgeDefaultValue
              },
              keyString(StoredKnowledgeKey))

          } yield Settings(
            storedKnowledge = storedKnowledge)

      }

    }

    override def write(settingsTry: Try[Settings]): YamlValue = unsupportedWriteOperation
  }

  implicit object SettingsYamlWriteFormat extends YamlFormat[Settings] {

    override def write(obj: Settings): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        StoredKnowledgeKey -> obj.storedKnowledge.toYaml)

    }

    override def read(yaml: YamlValue): Settings = unsupportedReadOperation

  }

}
