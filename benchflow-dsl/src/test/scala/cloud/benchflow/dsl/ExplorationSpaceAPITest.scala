package cloud.benchflow.dsl

import java.nio.file.Paths
import java.util.Optional

import cloud.benchflow.dsl.definition.types.bytes.Bytes
import cloud.benchflow.dsl.dockercompose.DockerComposeYamlString
import cloud.benchflow.dsl.explorationspace.javatypes.JavaCompatExplorationSpacePoint
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

    val testYaml = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationMultipleExample).toFile).mkString

    val explorationSpace = ExplorationSpaceAPI.explorationSpaceDimensionsFromTestYaml(testYaml)

    Assert.assertTrue(explorationSpace.getUsers.isPresent)

    Assert.assertTrue(explorationSpace.getMemory.isPresent)

    Assert.assertTrue(explorationSpace.getEnvironment.isPresent)

  }

  @Test def explorationSpaceFromTestYamlTest(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationMultipleExample).toFile).mkString

    val explorationSpace = ExplorationSpaceAPI.explorationSpaceFromTestYaml(testYaml)

    val expectedExplorationSpaceSize = 5 * 4 * 3 * 4

    Assert.assertEquals(expectedExplorationSpaceSize, explorationSpace.getUsersDimension.get().size())

  }

  @Test def generateExperimentBundleWithNumberTest(): Unit = {

    val experimentIndex = 10

    val testYaml = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationMultipleExample).toFile).mkString

    val dockerComposeYamlString = DockerComposeYamlString

    val explorationSpace = ExplorationSpaceAPI.explorationSpaceFromTestYaml(testYaml)

    val (experimentDefinition, newDockerCompose) = ExplorationSpaceAPI.generateExperimentBundle(
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

    val javaCompatExplorationSpacePoint = new JavaCompatExplorationSpacePoint(
      Optional.ofNullable(5),
      Optional.ofNullable(Map("camunda" -> Bytes.fromString("1g").get).asJava),
      Optional.ofNullable(Map("camunda" -> Map("AN_ENUM" -> "C", "SIZE_OF_THREADPOOL" -> "3").asJava).asJava))

    val testYaml = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationMultipleExample).toFile).mkString

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

    Assert.assertTrue(newDockerCompose.contains("SIZE_OF_THREADPOOL=3"))
    Assert.assertTrue(newDockerCompose.contains("AN_ENUM=C"))
    Assert.assertTrue(newDockerCompose.contains("mem_limit: 1g"))

  }

}
