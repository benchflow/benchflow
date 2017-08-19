package cloud.benchflow.dsl.explorationspace

import java.nio.file.Paths

import cloud.benchflow.dsl.definition.types.bytes.Bytes
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator.ExplorationSpacePoint
import cloud.benchflow.dsl._
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

import scala.io.Source

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-26
 */
class ExplorationSpaceAPIDimensionsGeneratorTest extends JUnitSuite {

  @Test def extractExplorationSpaceDimensionsUsersMemoryEnvironmentTest(): Unit = {

    val testYamlString = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationUsersMemoryEnvironmentExample).toFile).mkString

    val benchFlowTest = BenchFlowTestAPI.testFromYaml(testYamlString)

    val explorationSpace = ExplorationSpaceGenerator.extractExplorationSpaceDimensions(benchFlowTest)

    val expectedUsersList = List(5, 10)

    val expectedMemoryMap = Map(("camunda", List(
      Bytes.fromString("500m").get,
      Bytes.fromString("1000m").get)))

    val tpExpectedValues = List("1", "2")
    val enumExpectedValues = List("A", "B")
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

  @Test def generateExplorationSpaceUsersMemoryEnvironmentTest(): Unit = {

    val testYamlString = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationUsersMemoryEnvironmentExample).toFile).mkString

    val benchFlowTest = BenchFlowTestAPI.testFromYaml(testYamlString)

    val explorationSpaceDimensions = ExplorationSpaceGenerator.extractExplorationSpaceDimensions(benchFlowTest)

    val expectedExplorationSpaceSize = 2 * 2 * 2 * 2

    val numUsersValues = 2
    val userValues = List(5, 10)
    val usersBlockSize = expectedExplorationSpaceSize / numUsersValues
    val expectedUsersIndiciesList = List.fill(usersBlockSize)(0) ++
      List.fill(usersBlockSize)(1)

    val expectedUsersList = expectedUsersIndiciesList.map(index => userValues(index))

    // the last generated list
    val numSizeThreadPollValues = 2
    val sizeThreadPoolValues = List("1", "2")
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

    val testYamlString = Source.fromFile(Paths.get(BenchFlowExhaustiveExplorationUsersMemoryEnvironmentExample).toFile).mkString

    val benchFlowTest = BenchFlowTestAPI.testFromYaml(testYamlString)

    val explorationSpaceDimensions = ExplorationSpaceGenerator.extractExplorationSpaceDimensions(benchFlowTest)

    val explorationSpace = ExplorationSpaceGenerator.generateExplorationSpace(explorationSpaceDimensions)

    val expectedExperimentIndex = 2

    val explorationSpacePoint = ExplorationSpacePoint(
      users = Some(5),
      memory = Some(Map("camunda" -> Bytes.fromString("500m").get)),
      environment = Some(Map("camunda" -> Map("AN_ENUM" -> "B", "SIZE_OF_THREADPOOL" -> "1"))))

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

  @Test def generateExplorationSpaceSizeTest(): Unit = {

    // check users memory environment example

    val explorationSpaceUsersMemoryEnvironment = getExplorationSpaceFromTestDefinitionFileName(BenchFlowExhaustiveExplorationUsersMemoryEnvironmentExample)

    val userMemoryEnvironmentExpectedSize = 16

    Assert.assertEquals(userMemoryEnvironmentExpectedSize, explorationSpaceUsersMemoryEnvironment.size)
    Assert.assertTrue(explorationSpaceUsersMemoryEnvironment.usersDimension.isDefined)
    Assert.assertTrue(explorationSpaceUsersMemoryEnvironment.memoryDimension.isDefined)
    Assert.assertTrue(explorationSpaceUsersMemoryEnvironment.environmentDimension.isDefined)

    // check users example

    val explorationSpaceUsers = getExplorationSpaceFromTestDefinitionFileName(BenchFlowExhaustiveExplorationUsersExample)

    val usersExpectedSize = 4

    Assert.assertEquals(usersExpectedSize, explorationSpaceUsers.size)
    Assert.assertTrue(explorationSpaceUsers.usersDimension.isDefined)
    Assert.assertTrue(explorationSpaceUsers.memoryDimension.isEmpty)
    Assert.assertTrue(explorationSpaceUsers.environmentDimension.isEmpty)

    // check user environment example

    val explorationSpaceUsersEnvironment = getExplorationSpaceFromTestDefinitionFileName(BenchFlowExhaustiveExplorationUsersEnvironmentExample)

    val usersEnvironmentExpectedSize = 6

    Assert.assertEquals(usersEnvironmentExpectedSize, explorationSpaceUsersEnvironment.size)
    Assert.assertTrue(explorationSpaceUsersEnvironment.usersDimension.isDefined)
    Assert.assertTrue(explorationSpaceUsersEnvironment.memoryDimension.isEmpty)
    Assert.assertTrue(explorationSpaceUsersEnvironment.environmentDimension.isDefined)

    // check memory example

    val explorationSpaceMemory = getExplorationSpaceFromTestDefinitionFileName(BenchFlowExhaustiveExplorationMemoryExample)

    val memoryExpectedSize = 2

    Assert.assertEquals(memoryExpectedSize, explorationSpaceMemory.size)
    Assert.assertTrue(explorationSpaceMemory.usersDimension.isEmpty)
    Assert.assertTrue(explorationSpaceMemory.memoryDimension.isDefined)
    Assert.assertTrue(explorationSpaceMemory.environmentDimension.isEmpty)

  }

  @Test def generateExplorationSpaceEmptyTest(): Unit = {

    // check load example (no exploration space)
    val explorationSpaceLoad = getExplorationSpaceFromTestDefinitionFileName(BenchFlowLoadTestExample)

    val loadExpectedSize = 0

    Assert.assertEquals(loadExpectedSize, explorationSpaceLoad.size)
    Assert.assertTrue(explorationSpaceLoad.usersDimension.isEmpty)
    Assert.assertTrue(explorationSpaceLoad.memoryDimension.isEmpty)
    Assert.assertTrue(explorationSpaceLoad.environmentDimension.isEmpty)

  }

  def getExplorationSpaceFromTestDefinitionFileName(fileName: String): ExplorationSpaceGenerator.ExplorationSpace = {

    val testYamlString = Source.fromFile(Paths.get(fileName).toFile).mkString

    val benchFlowTest = BenchFlowTestAPI.testFromYaml(testYamlString)

    val explorationSpaceDimensions = ExplorationSpaceGenerator.extractExplorationSpaceDimensions(benchFlowTest)

    ExplorationSpaceGenerator.generateExplorationSpace(explorationSpaceDimensions)

  }

}
