package cloud.benchflow.dsl.definition.configuration.settings

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-22
 */
object SettingsObject {

  val StoredKnowledgeDefaultValue = true

  case class Settings(
    storedKnowledge: Boolean)

}
