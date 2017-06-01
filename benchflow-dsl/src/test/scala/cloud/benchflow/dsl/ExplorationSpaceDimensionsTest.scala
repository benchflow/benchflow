package cloud.benchflow.dsl

import java.nio.file.Paths

import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

import scala.io.Source

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-01
 */
class ExplorationSpaceDimensionsTest extends JUnitSuite {

  @Test def generateExplorationSpaceTest(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowExplorationMultipleExample).toFile).mkString

    val explorationSpace = ExplorationSpace.explorationSpaceFromTestYaml(testYaml)

    Assert.assertTrue(explorationSpace.users.isDefined)

    Assert.assertTrue(explorationSpace.memory.isDefined)

    Assert.assertTrue(explorationSpace.environment.isDefined)

  }

  @Test def initialExplorationSpaceStateTest(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowExplorationMultipleExample).toFile).mkString

    val explorationSpace = ExplorationSpace.explorationSpaceFromTestYaml(testYaml)

    val initialExplorationSpaceState = ExplorationSpace.getInitialExplorationSpaceState(explorationSpace)

    val expectedExplorationSpaceSize = 5 * 4 * 3 * 4

    val expectedList = List.fill(expectedExplorationSpaceSize)(-1)

    val expectedUsersState = (expectedList, 4)
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
