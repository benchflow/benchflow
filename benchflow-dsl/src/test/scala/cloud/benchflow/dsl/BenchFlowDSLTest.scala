package cloud.benchflow.dsl

import java.nio.file.Paths

import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
import cloud.benchflow.dsl.definition.types.bytes.{ Bytes, BytesUnit }
import cloud.benchflow.dsl.dockercompose.DockerComposeYamlString
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

  // TODO - add tests that handles exceptions thrown

  @Test def loadTestDefinition(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowLoadTestExample).toFile).mkString

    val benchFlowTest = BenchFlowDSL.testFromYaml(testYaml)

    val benchFlowTestYamlString = BenchFlowDSL.testToYamlString(benchFlowTest)

    Assert.assertNotNull(benchFlowTestYamlString)

    Assert.assertEquals(BenchFlowDSL.testFromYaml(benchFlowTestYamlString), benchFlowTest)

  }

  @Test(expected = classOf[BenchFlowDeserializationException])
  def loadInvalidTestDefinition(): Unit = {

    val testYaml =
      """
        |version: '1'
        |name: WfMSTest
        |description: A WfMS test
      """.stripMargin

    val benchFlowTest = BenchFlowDSL.testFromYaml(testYaml)

  }

  @Test def loadExperimentDefinitionFromTestDefinition(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowLoadTestExample).toFile).mkString

    val benchFlowExperiment = BenchFlowDSL.experimentFromTestYaml(testYaml)

    val benchFlowExperimentYamlString = BenchFlowDSL.experimentToYamlString(benchFlowExperiment)

    Assert.assertNotNull(benchFlowExperimentYamlString)

    Assert.assertEquals(BenchFlowDSL.experimentFromExperimentYaml(benchFlowExperimentYamlString), benchFlowExperiment)

  }

  @Test def experimentFromTestYamlNumUsers(): Unit = {

    val originalTestYaml = Source.fromFile(Paths.get(BenchFlowExplorationUsersExample).toFile).mkString

    val benchFlowTest = BenchFlowDSL.testFromYaml(originalTestYaml)

    val extractedTestYaml = BenchFlowDSL.testToYamlString(benchFlowTest)

    Assert.assertTrue(extractedTestYaml.contains("selection: one-at-a-time"))

    benchFlowTest.configuration.goal.explorationSpace.get.workload.get.users.get.values.foreach(numUsers => {

      val builder = BenchFlowDSL.experimentYamlBuilderFromTestYaml(originalTestYaml)
      val experimentYaml = builder.numUsers(numUsers).build()

      Assert.assertTrue(experimentYaml.contains("users: " + numUsers))

    })

  }

  @Test def dockerComposeBuilderTest(): Unit = {

    val dockerComposeString = DockerComposeYamlString

    val serviceName = "camunda"
    val underlying = 500
    val memLimit: Bytes = new Bytes(underlying = underlying, unit = BytesUnit.MegaBytes)
    val environmentKey = "DB_DRIVER"
    val environmentValue = "TEST_ENVIRONMENT_VALUE"

    val generatedComposeString = BenchFlowDSL.dockerComposeYamlBuilderFromDockerComposeYaml(dockerComposeString)
      .environmentVariable(serviceName, environmentKey, environmentValue)
      .memLimit(serviceName, memLimit).build()

    Assert.assertTrue(generatedComposeString.contains(s"mem_limit: ${memLimit.underlying}${memLimit.unit}"))
    Assert.assertTrue(generatedComposeString.contains(s"$environmentKey=$environmentValue"))
    Assert.assertFalse(generatedComposeString.contains(s"DB_DRIVER=com.mysql.jdbc.Driver"))

  }

}
