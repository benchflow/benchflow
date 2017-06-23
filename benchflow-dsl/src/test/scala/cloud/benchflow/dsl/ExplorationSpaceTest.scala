package cloud.benchflow.dsl

import java.nio.file.Paths

import cloud.benchflow.dsl.definition.types.bytes.Bytes
import cloud.benchflow.dsl.dockercompose.DockerComposeYamlString
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator.ExplorationSpacePoint
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

import scala.io.Source

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-01
 */
class ExplorationSpaceTest extends JUnitSuite {

  @Test def explorationSpaceDimensionsFromTestYamlTest(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationMultipleExample).toFile).mkString

    val explorationSpace = ExplorationSpace.explorationSpaceDimensionsFromTestYaml(testYaml)

    Assert.assertTrue(explorationSpace.users.isDefined)

    Assert.assertTrue(explorationSpace.memory.isDefined)

    Assert.assertTrue(explorationSpace.environment.isDefined)

  }

  @Test def explorationSpaceFromTestYamlTest(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationMultipleExample).toFile).mkString

    val explorationSpace = ExplorationSpace.explorationSpaceFromTestYaml(testYaml)

    val expectedExplorationSpaceSize = 5 * 4 * 3 * 4

    Assert.assertEquals(expectedExplorationSpaceSize, explorationSpace.usersDimension.get.length)

  }

  @Test def generateExperimentBundleWithNumberTest(): Unit = {

    val experimentIndex = 10

    val testYaml = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationMultipleExample).toFile).mkString

    val dockerComposeYamlString = DockerComposeYamlString

    val explorationSpace = ExplorationSpace.explorationSpaceFromTestYaml(testYaml)

    val (experimentDefinition, newDockerCompose) = ExplorationSpace.generateExperimentBundle(
      explorationSpace,
      experimentIndex,
      testYaml,
      dockerComposeYamlString)

    Assert.assertTrue(experimentDefinition.contains("users: " + 5))

    Assert.assertTrue(newDockerCompose.contains("SIZE_OF_THREADPOOL=3"))
    Assert.assertTrue(newDockerCompose.contains("AN_ENUM=C"))
    Assert.assertTrue(newDockerCompose.contains("mem_limit: 1g"))

  }

  @Test def generateExperimentBundleWithConfigurationTest(): Unit = {

    val expectedExperimentIndex = 10

    val explorationSpacePoint = ExplorationSpacePoint(
      users = Some(5),
      memory = Some(Map("camunda" -> Bytes.fromString("1g").get)),
      environment = Some(Map("camunda" -> Map("AN_ENUM" -> "C", "SIZE_OF_THREADPOOL" -> "3"))))

    val testYaml = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationMultipleExample).toFile).mkString

    val dockerComposeYamlString = DockerComposeYamlString

    val explorationSpace = ExplorationSpace.explorationSpaceFromTestYaml(testYaml)

    val optionResult = ExplorationSpace.generateExperimentBundle(
      explorationSpace,
      explorationSpacePoint,
      testYaml,
      dockerComposeYamlString)

    Assert.assertTrue(optionResult.isDefined)

    val (experimentDefinition, newDockerCompose, experimentIndex) = optionResult.get

    Assert.assertEquals(expectedExperimentIndex, experimentIndex)

    Assert.assertTrue(experimentDefinition.contains("users: " + 5))

    Assert.assertTrue(newDockerCompose.contains("SIZE_OF_THREADPOOL=3"))
    Assert.assertTrue(newDockerCompose.contains("AN_ENUM=C"))
    Assert.assertTrue(newDockerCompose.contains("mem_limit: 1g"))

  }

}
