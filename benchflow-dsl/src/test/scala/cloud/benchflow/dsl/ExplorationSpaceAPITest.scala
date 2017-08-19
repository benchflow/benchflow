package cloud.benchflow.dsl

import java.nio.file.Paths
import java.util.Optional

import cloud.benchflow.dsl.definition.types.bytes.Bytes
import cloud.benchflow.dsl.dockercompose.DockerComposeYamlString
import cloud.benchflow.dsl.explorationspace.JavaCompatExplorationSpaceConverter.JavaCompatExplorationSpacePoint
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

import scala.collection.JavaConverters._
import scala.io.Source

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-01
 */
class ExplorationSpaceAPITest extends JUnitSuite {

  @Test def explorationSpaceDimensionsFromTestYamlTest(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationUsersMemoryEnvironmentExample).toFile).mkString

    val explorationSpace = ExplorationSpaceAPI.explorationSpaceDimensionsFromTestYaml(testYaml)

    Assert.assertTrue(explorationSpace.users.isPresent)

    Assert.assertTrue(explorationSpace.memory.isPresent)

    Assert.assertTrue(explorationSpace.environment.isPresent)

  }

  @Test def explorationSpaceFromTestYamlTest(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationUsersMemoryEnvironmentExample).toFile).mkString

    val explorationSpace = ExplorationSpaceAPI.explorationSpaceFromTestYaml(testYaml)

    val expectedExplorationSpaceSize = 2 * 2 * 2 * 2

    Assert.assertEquals(expectedExplorationSpaceSize, explorationSpace.usersDimension.get().size())

  }

  @Test def generateExperimentBundleWithNumberTest(): Unit = {

    val experimentIndex = 3

    val testYaml = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationUsersMemoryEnvironmentExample).toFile).mkString

    val dockerComposeYamlString = DockerComposeYamlString

    val explorationSpace = ExplorationSpaceAPI.explorationSpaceFromTestYaml(testYaml)

    val (experimentDefinition, newDockerCompose) = ExplorationSpaceAPI.generateExperimentBundle(
      explorationSpace,
      experimentIndex,
      testYaml,
      dockerComposeYamlString)

    Assert.assertTrue(experimentDefinition.contains("users: " + 5))

    Assert.assertTrue(newDockerCompose.contains("SIZE_OF_THREADPOOL=2"))
    Assert.assertTrue(newDockerCompose.contains("AN_ENUM=B"))
    Assert.assertTrue(newDockerCompose.contains("mem_limit: 500m"))

  }

  @Test def generateExperimentBundleWithConfigurationTest(): Unit = {

    val expectedExperimentIndex = 3

    val javaCompatExplorationSpacePoint = JavaCompatExplorationSpacePoint(
      Optional.ofNullable(5),
      Optional.ofNullable(Map("camunda" -> Bytes.fromString("500m").get).asJava),
      Optional.ofNullable(Map("camunda" -> Map("AN_ENUM" -> "B", "SIZE_OF_THREADPOOL" -> "2").asJava).asJava))

    val testYaml = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationUsersMemoryEnvironmentExample).toFile).mkString

    val dockerComposeYamlString = DockerComposeYamlString

    val javaCompatExplorationSpace = ExplorationSpaceAPI.explorationSpaceFromTestYaml(testYaml)

    val optionResult = ExplorationSpaceAPI.generateExperimentBundle(
      javaCompatExplorationSpace,
      javaCompatExplorationSpacePoint,
      testYaml,
      dockerComposeYamlString)

    Assert.assertTrue(optionResult.isDefined)

    val (experimentDefinition, newDockerCompose, experimentIndex) = optionResult.get

    Assert.assertEquals(expectedExperimentIndex, experimentIndex)

    Assert.assertTrue(experimentDefinition.contains("users: " + 5))

    Assert.assertTrue(newDockerCompose.contains("SIZE_OF_THREADPOOL=2"))
    Assert.assertTrue(newDockerCompose.contains("AN_ENUM=B"))
    Assert.assertTrue(newDockerCompose.contains("mem_limit: 500m"))

  }

}
