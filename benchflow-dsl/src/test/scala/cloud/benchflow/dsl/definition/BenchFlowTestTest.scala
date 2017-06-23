package cloud.benchflow.dsl.definition

import java.nio.file.Paths

import cloud.benchflow.dsl.BenchFlowLoadTestExample
import cloud.benchflow.dsl.definition.BenchFlowTestYamlProtocol._
import org.junit.{ Assert, Test }
import net.jcazevedo.moultingyaml._

import scala.io.Source
import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-22
 */
class BenchFlowTestTest {

  @Test def testNoDescription(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowLoadTestExample).toFile).mkString

    val triedTest = testYaml.parseYaml.convertTo[Try[BenchFlowTest]]

    Assert.assertTrue(triedTest.isSuccess)

    val testWithoutDescription = triedTest.get.copy(description = None)

    val testWithoutDescriptionYaml = testWithoutDescription.toYaml

    Assert.assertFalse(testWithoutDescriptionYaml.prettyPrint.contains("description: "))

    val triedTestWithoutDescription = testWithoutDescriptionYaml.prettyPrint.parseYaml.convertTo[Try[BenchFlowTest]]

    Assert.assertTrue(triedTestWithoutDescription.isSuccess)

    Assert.assertTrue(triedTestWithoutDescription.get.description.isEmpty)

  }

}
