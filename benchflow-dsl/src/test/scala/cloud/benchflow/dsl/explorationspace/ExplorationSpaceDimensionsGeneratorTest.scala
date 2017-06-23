package cloud.benchflow.dsl.explorationspace

import java.nio.file.Paths

import cloud.benchflow.dsl.definition.types.bytes.Bytes
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator.ExplorationSpacePoint
import cloud.benchflow.dsl.{ BenchFlowTestAPI, BenchFlowExhaustiveExplorationMultipleExample }
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

import scala.io.Source

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-26
 */
class ExplorationSpaceAPIDimensionsGeneratorTest extends JUnitSuite {

  @Test def extractExplorationSpaceDimensionsTest(): Unit = {

    val testYamlString = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationMultipleExample).toFile).mkString

    val benchFlowTest = BenchFlowTestAPI.testFromYaml(testYamlString)

    val explorationSpace = ExplorationSpaceGenerator.extractExplorationSpaceDimensions(benchFlowTest)

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

  @Test def fillListWithIndicesTest(): Unit = {

    val expectedFirstList = List(0, 0, 1, 1, 2, 2)
    val firstBlockSize = 2
    val firstNumValues = 3
    val firstListLength = 6

    Assert.assertEquals(
      expectedFirstList,
      ExplorationSpaceGenerator.computeOrderOfValuesForDimension(firstBlockSize, firstNumValues, firstListLength))

    val expectedSecondList = List(0, 1, 2, 0, 1, 2)
    val secondBlockSize = 1
    val secondNumValues = 3
    val secondListLength = 6

    Assert.assertEquals(
      expectedSecondList,
      ExplorationSpaceGenerator.computeOrderOfValuesForDimension(secondBlockSize, secondNumValues, secondListLength))

  }

  @Test def generateExplorationSpaceTest(): Unit = {

    val testYamlString = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationMultipleExample).toFile).mkString

    val benchFlowTest = BenchFlowTestAPI.testFromYaml(testYamlString)

    val explorationSpaceDimensions = ExplorationSpaceGenerator.extractExplorationSpaceDimensions(benchFlowTest)

    val expectedExplorationSpaceSize = 5 * 4 * 3 * 4

    val numUsersValues = 4
    val userValues = List(5, 10, 15, 20)
    val usersBlockSize = expectedExplorationSpaceSize / numUsersValues
    val expectedUsersIndiciesList = List.fill(usersBlockSize)(0) ++
      List.fill(usersBlockSize)(1) ++
      List.fill(usersBlockSize)(2) ++
      List.fill(usersBlockSize)(3)

    val expectedUsersList = expectedUsersIndiciesList.map(index => userValues(index))

    // the last generated list
    val numSizeThreadPollValues = 4
    val sizeThreadPoolValues = List("1", "2", "3", "5")
    val expectedSizeThreadPoolIndicesList: List[Int] = (for {
      _ <- 0 until expectedExplorationSpaceSize / numSizeThreadPollValues
      list <- 0 until numSizeThreadPollValues
    } yield list)(collection.breakOut)
    val expectedSizeThreadPoolList = expectedSizeThreadPoolIndicesList.map(index => sizeThreadPoolValues(index))

    // test filled list
    val filledList = ExplorationSpaceGenerator.computeOrderOfValuesForDimension(usersBlockSize, numUsersValues, expectedExplorationSpaceSize)

    Assert.assertEquals(expectedUsersIndiciesList, filledList)

    // test exploration space generation
    val explorationSpace = ExplorationSpaceGenerator.generateExplorationSpace(explorationSpaceDimensions)

    Assert.assertEquals(expectedUsersList, explorationSpace.usersDimension.get)

    Assert.assertEquals(
      expectedSizeThreadPoolList,
      explorationSpace.environmentDimension.get("camunda")("SIZE_OF_THREADPOOL"))

  }

  @Test def experimentIndexTest(): Unit = {

    val testYamlString = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationMultipleExample).toFile).mkString

    val benchFlowTest = BenchFlowTestAPI.testFromYaml(testYamlString)

    val explorationSpaceDimensions = ExplorationSpaceGenerator.extractExplorationSpaceDimensions(benchFlowTest)

    val explorationSpace = ExplorationSpaceGenerator.generateExplorationSpace(explorationSpaceDimensions)

    val expectedExperimentIndex = 10

    val explorationSpacePoint = ExplorationSpacePoint(
      users = Some(5),
      memory = Some(Map("camunda" -> Bytes.fromString("1g").get)),
      environment = Some(Map("camunda" -> Map("AN_ENUM" -> "C", "SIZE_OF_THREADPOOL" -> "3"))))

    val experimentIndexOption = ExplorationSpaceGenerator.getExperimentIndex(explorationSpace, explorationSpacePoint)

    Assert.assertEquals(expectedExperimentIndex, experimentIndexOption.get)

  }

  @Test def getIndicesSetTest(): Unit = {

    val integerList = List(1, 1, 2, 2, 3, 4, 1)

    val integerSet = ExplorationSpaceGenerator.getIndicesSet(integerList)({ case (value, _) => value == 1 })

    val expectedIntegerSet = Set(0, 1, 6)

    Assert.assertEquals(expectedIntegerSet, integerSet)

    val stringList = List("A", "A", "B", "B", "A")

    val stringSet = ExplorationSpaceGenerator.getIndicesSet(stringList)({ case (value, _) => value == "A" })

    val expectedStringSet = Set(0, 1, 4)

    Assert.assertEquals(expectedStringSet, stringSet)

  }

}
