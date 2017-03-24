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

    val benchFlowYamlString = BenchFlowDSL.testToYaml(benchFlowTest.get)

    Assert.assertNotNull(benchFlowYamlString)

    Assert.assertEquals(BenchFlowDSL.testFromYaml(benchFlowYamlString), benchFlowTest)

  }

  //  import net.jcazevedo.moultingyaml._
  //
  //  import scala.io.Source.fromFile
  //
  //  "BenchFlow test" should "parse correctly" in {
  //
  //    val benchFlowTest =
  //      fromFile(Paths.get("./src/test/resources/benchflow-test.yml").toFile).mkString
  //        .parseYaml.convertTo[BenchFlowTest]
  //
  //    benchFlowTest should have(
  //      'name ("WfMSTest")
  //    )
  //
  //  }

}
