package cloud.benchflow.dsl.definition.configuration.settings

import cloud.benchflow.dsl.definition.configuration.settings.SettingsObject.Settings
import cloud.benchflow.dsl.definition.configuration.settings.SettingsYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{ Assert, Test }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-22
 */
class SettingsTest {

  private val defaultSettings: String =
    """
      |
    """.stripMargin

  @Test def storedKnowledgeDefault(): Unit = {

    val triedSettings = defaultSettings.parseYaml.convertTo[Try[Settings]]

    Assert.assertTrue(triedSettings.isSuccess)

    Assert.assertTrue(triedSettings.get.storedKnowledge)

    val goalDefaultYaml = triedSettings.get.toYaml

    Assert.assertTrue(goalDefaultYaml.prettyPrint.contains("stored_knowledge: true"))

  }

  @Test def storedKnowledge(): Unit = {

    val storedKnowledgeYamlString: String =
      """stored_knowledge: false
        |
    """.stripMargin

    val triedSettings = storedKnowledgeYamlString.parseYaml.convertTo[Try[Settings]]

    Assert.assertTrue(triedSettings.isSuccess)

    Assert.assertFalse(triedSettings.get.storedKnowledge)

    val goalDefaultYaml = triedSettings.get.toYaml

    Assert.assertTrue(goalDefaultYaml.prettyPrint.contains("stored_knowledge: false"))

  }

}
