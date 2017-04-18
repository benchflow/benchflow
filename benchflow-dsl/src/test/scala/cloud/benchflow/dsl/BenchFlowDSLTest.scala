package cloud.benchflow.dsl

import java.nio.file.Paths

import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

import scala.io.Source

/**
 * based on http://www.scalatest.org/getting_started_with_junit_4_in_scala
 *
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 16.03.17.
 */
class BenchFlowDSLTest extends JUnitSuite {

  @Test def loadTestDefinition(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowLoadTestExample).toFile).mkString

    val benchFlowTest = BenchFlowDSL.testFromYaml(testYaml)

    Assert.assertTrue(benchFlowTest.isSuccess)

    val benchFlowTestYamlString = BenchFlowDSL.testToYamlString(benchFlowTest.get)

    Assert.assertNotNull(benchFlowTestYamlString)

    Assert.assertEquals(BenchFlowDSL.testFromYaml(benchFlowTestYamlString), benchFlowTest)

  }

  @Test def loadExperimentDefinitionFromTestDefinition(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowLoadTestExample).toFile).mkString

    val benchFlowExperiment = BenchFlowDSL.experimentFromTestYaml(testYaml)

    Assert.assertTrue(benchFlowExperiment.isSuccess)

    val benchFlowExperimentYamlString = BenchFlowDSL.experimentToYamlString(benchFlowExperiment.get)

    Assert.assertNotNull(benchFlowExperimentYamlString)

    Assert.assertEquals(BenchFlowDSL.experimentFromExperimentYaml(benchFlowExperimentYamlString), benchFlowExperiment)

  }

  @Test def  experimentFromTestYamlNumUsers(): Unit = {



  }

}
