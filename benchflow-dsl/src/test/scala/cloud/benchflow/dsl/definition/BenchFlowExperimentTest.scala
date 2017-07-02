package cloud.benchflow.dsl.definition

import java.nio.file.Paths

import cloud.benchflow.dsl.BenchFlowLoadTestExample
import cloud.benchflow.dsl.definition.BenchFlowExperimentYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{Assert, Test}

import scala.io.Source
import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-22
 */
class BenchFlowExperimentTest {

  @Test def testNoDescription(): Unit = {

    val experimentYaml = Source.fromFile(Paths.get(BenchFlowLoadTestExample).toFile).mkString

    val triedExperiment = experimentYaml.parseYaml.convertTo[Try[BenchFlowExperiment]]

    Assert.assertTrue(triedExperiment.isSuccess)

    val experimentWithoutDescription = triedExperiment.get.copy(description = None)

    val experimentWithoutDescriptionYaml = experimentWithoutDescription.toYaml

    Assert.assertFalse(experimentWithoutDescriptionYaml.prettyPrint.contains("description: "))

    val triedExperientWithoutDescription = experimentWithoutDescriptionYaml.prettyPrint.parseYaml.convertTo[Try[BenchFlowExperiment]]

    Assert.assertTrue(triedExperientWithoutDescription.isSuccess)

    Assert.assertTrue(triedExperientWithoutDescription.get.description.isEmpty)

  }

}
