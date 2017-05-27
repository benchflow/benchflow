package cloud.benchflow.dsl

import java.nio.file.Paths

import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
import cloud.benchflow.dsl.definition.types.bytes.{ Bytes, BytesUnit }
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite
import cloud.benchflow.dsl.dockercompose.DockerComposeYamlString

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

  @Test def generateExplorationSpaceTest(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowExplorationMultipleExample).toFile).mkString

    val explorationSpace = BenchFlowDSL.explorationSpaceFromTestYaml(testYaml)

    Assert.assertTrue(explorationSpace.users.isDefined)

    Assert.assertTrue(explorationSpace.memory.isDefined)

    Assert.assertTrue(explorationSpace.environment.isDefined)

  }

  @Test def initialExplorationSpaceStateTest(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowExplorationMultipleExample).toFile).mkString

    val explorationSpace = BenchFlowDSL.explorationSpaceFromTestYaml(testYaml)

    val initialExplorationSpaceState = BenchFlowDSL.getInitialExplorationSpaceState(explorationSpace)

    val expectedExplorationSpaceSize = 5 * 4 * 3 * 4

    val expectedList = List.fill(expectedExplorationSpaceSize)(-1)

    val expectedUsersState = (expectedList, explorationSpace.users.get.length)
    val expectedMemoryState = Map(("camunda", (expectedList, 5)))
    val expectedEnvironmentState = Map(
      ("camunda", Map(
        ("SIZE_OF_THREADPOOL", (expectedList, 4)),
        ("AN_ENUM", (expectedList, 3)))))

    Assert.assertEquals(expectedUsersState, initialExplorationSpaceState.usersState.get)
    Assert.assertEquals(expectedMemoryState, initialExplorationSpaceState.memoryState.get)
    Assert.assertEquals(expectedEnvironmentState, initialExplorationSpaceState.environmentState.get)

  }

}
