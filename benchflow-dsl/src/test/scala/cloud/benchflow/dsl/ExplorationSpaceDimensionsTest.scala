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

  @Test def explorationSpaceDimensionsFromTestYamlTest(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowExplorationMultipleExample).toFile).mkString

    val explorationSpace = ExplorationSpace.explorationSpaceDimensionsFromTestYaml(testYaml)

    Assert.assertTrue(explorationSpace.users.isDefined)

    Assert.assertTrue(explorationSpace.memory.isDefined)

    Assert.assertTrue(explorationSpace.environment.isDefined)

  }

  @Test def explorationSpaceFromTestYamlTest(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowExplorationMultipleExample).toFile).mkString

    val explorationSpace = ExplorationSpace.explorationSpaceFromTestYaml(testYaml)

    val expectedExplorationSpaceSize = 5 * 4 * 3 * 4

    Assert.assertEquals(expectedExplorationSpaceSize, explorationSpace.usersDimension.get.length)

  }

}
