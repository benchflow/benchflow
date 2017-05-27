package cloud.benchflow.dsl.explorationspace

import java.nio.file.Paths

import cloud.benchflow.dsl.definition.types.bytes.Bytes
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite
import cloud.benchflow.dsl.{ BenchFlowDSL, BenchFlowExplorationMultipleExample }

import scala.io.Source

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-26
 */
class ExplorationSpaceGeneratorTest extends JUnitSuite {

  @Test def generationTest(): Unit = {

    val testYamlString = Source.fromFile(Paths.get(BenchFlowExplorationMultipleExample).toFile).mkString

    val benchFlowTest = BenchFlowDSL.testFromYaml(testYamlString)

    val explorationSpace = ExplorationSpaceGenerator.generateExplorationSpace(benchFlowTest)

    val expectedUsersList = List(5, 10, 15, 20)

    val expectedMemoryMap = Map(("camunda", List(
      Bytes.fromString("1g").get,
      Bytes.fromString("2g").get,
      Bytes.fromString("3g").get,
      Bytes.fromString("4g").get,
      Bytes.fromString("5g").get)))

    val tpExpectedValues = List("1", "2", "3", "5")
    val enumExpectedValues = List("A", "B", "C")
    val expectedEnvironmentMap = Map(("camunda", Map(
      ("SIZE_OF_THREADPOOL", tpExpectedValues),
      ("AN_ENUM", enumExpectedValues))))

    Assert.assertTrue(explorationSpace.users.isDefined)
    Assert.assertEquals(expectedUsersList, explorationSpace.users.get)

    Assert.assertTrue(explorationSpace.memory.isDefined)
    Assert.assertEquals(expectedMemoryMap, explorationSpace.memory.get)

    Assert.assertTrue(explorationSpace.environment.isDefined)
    Assert.assertEquals(expectedEnvironmentMap, explorationSpace.environment.get)
  }

  @Test def initialExplorationSpaceTest(): Unit = {

    val testYamlString = Source.fromFile(Paths.get(BenchFlowExplorationMultipleExample).toFile).mkString

    val benchFlowTest = BenchFlowDSL.testFromYaml(testYamlString)

    val explorationSpace = ExplorationSpaceGenerator.generateExplorationSpace(benchFlowTest)

    val initialExplorationSpaceState = ExplorationSpaceGenerator.generateInitialExplorationSpaceState(explorationSpace)

    val expectedExplorationSpaceSize = 5 * 4 * 3 * 4

    Assert.assertEquals(
      expectedExplorationSpaceSize,
      initialExplorationSpaceState.usersState.map {
        case (list, _) => list.length
      }.get)

  }

  @Test def oneAtATimeExplorationSpaceTest(): Unit = {

    val testYamlString = Source.fromFile(Paths.get(BenchFlowExplorationMultipleExample).toFile).mkString

    val benchFlowTest = BenchFlowDSL.testFromYaml(testYamlString)

    val explorationSpace = ExplorationSpaceGenerator.generateExplorationSpace(benchFlowTest)

    val expectedExplorationSpaceSize = 5 * 4 * 3 * 4

    val numUsersValues = 4
    val usersShift = expectedExplorationSpaceSize / numUsersValues
    val expectedUsersList = List.fill(usersShift)(0) ++
      List.fill(usersShift)(1) ++
      List.fill(usersShift)(2) ++
      List.fill(usersShift)(3)

    val numEnumValues = 4
    val expectedAnEnumList: List[Int] = (for {
      _ <- 0 until expectedExplorationSpaceSize / numEnumValues
      list <- 0 until numEnumValues
    } yield list)(collection.breakOut)

    val filledList = ExplorationSpaceGenerator.fillList(usersShift, numUsersValues, expectedExplorationSpaceSize)

    Assert.assertEquals(expectedUsersList, filledList)

    val oneAtATimeExplorationSpace = ExplorationSpaceGenerator.oneAtATimeExplorationSpace(explorationSpace)

    Assert.assertEquals(expectedUsersList, oneAtATimeExplorationSpace.usersState.map {
      case (list, _) => list
    }.get)

    Assert.assertEquals(
      expectedAnEnumList,
      oneAtATimeExplorationSpace.environmentState.get("camunda").get("SIZE_OF_THREADPOOL").map {
        case (list, _) => list
      }.get)

  }

}
