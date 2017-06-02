package cloud.benchflow.dsl.explorationspace

import cloud.benchflow.dsl.ExplorationSpace.ExperimentIndex
import cloud.benchflow.dsl.definition.BenchFlowTest
import cloud.benchflow.dsl.definition.types.bytes.Bytes

import scala.collection.mutable

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-25
 */
object ExplorationSpaceGenerator {

  // types for easier readability
  type ServiceName = String
  type VariableName = String
  type VariableValue = String
  type NumUsers = Int
  type DimensionLength = Int
  type Index = Int

  case class ExplorationSpaceDimensions(
    users: Option[List[NumUsers]],
    memory: Option[Map[ServiceName, List[Bytes]]],
    environment: Option[Map[ServiceName, Map[VariableName, List[VariableValue]]]])

  case class ExplorationSpace(
    size: Int,
    usersDimension: Option[List[NumUsers]],
    memoryDimension: Option[Map[ServiceName, List[Bytes]]],
    environmentDimension: Option[Map[ServiceName, Map[VariableName, List[VariableValue]]]])

  case class ExplorationSpacePoint(
    users: Option[NumUsers],
    memory: Option[Map[ServiceName, Bytes]],
    environment: Option[Map[ServiceName, Map[VariableName, VariableValue]]])

  /**
   * Extracts the exploration space dimensions of the provided BenchFlow test.
   *
   * @param test the test to extract from
   * @return the extracted exploration space dimensions
   */
  def extractExplorationSpaceDimensions(test: BenchFlowTest): ExplorationSpaceDimensions = {

    // traverses the exploration space definition of a BenchFlowTest object

    // get the specified user values
    val users: Option[List[NumUsers]] = test.configuration.goal.explorationSpace.flatMap(
      _.workload.flatMap(
        _.users.map(
          _.values)))

    // get the specified memory values per service
    val memory: Option[Map[ServiceName, List[Bytes]]] = for {

      explorationSpace <- test.configuration.goal.explorationSpace

      services <- explorationSpace.services

      serviceMemorySpace = services.mapValues(
        _.resources.flatMap(
          _.memory.map(
            _.values))).collect {
          case (service, Some(memLimit)) => (service, memLimit)
        }

    } yield serviceMemorySpace

    // get the specified environment variable values per variable and service
    val environment: Option[Map[ServiceName, Map[VariableName, List[VariableValue]]]] = for {

      explorationSpace <- test.configuration.goal.explorationSpace

      services <- explorationSpace.services

      environmentVariables = services.mapValues(
        _.environment).collect {
          case (service, Some(variableMap)) => (service, variableMap)
        }

    } yield environmentVariables

    ExplorationSpaceDimensions(users, memory, environment)

  }

  /**
   * Generates the complete exploration space from the provided exploration space dimensions by building the
   * cartesian product of the dimensions.
   *
   * @param explorationSpaceDimensions the possible dimensions in the exploration space and their possible values
   * @return the complete exploration space
   */
  def generateExplorationSpace(explorationSpaceDimensions: ExplorationSpaceDimensions): ExplorationSpace = {

    val explorationSpaceSizeOption = calculateExplorationSpaceSize(explorationSpaceDimensions)

    explorationSpaceSizeOption match {

      // if the exploration space dimension is not defined return an empty exploration space
      case None => ExplorationSpace(0, None, None, None)

      case Some(explorationSpaceSize) => {

        // for each dimension generate the possible values so that all possible values in the
        // exploration space is generated. It is done according to the cartesian product.

        // the initial block size
        var blockSize = explorationSpaceSize

        // generate all the possible user values
        val usersDimensionOption = explorationSpaceDimensions.users.map {
          case (list) =>
            blockSize = blockSize / list.length // update blockSize value
            fillListWithIndices(blockSize, list.length, explorationSpaceSize)
              .map(index => list(index))
        }

        // generate all the possible memory values per service
        val memoryDimensionOption = explorationSpaceDimensions.memory.map(memory => memory.map {
          case (serviceName, list) =>
            blockSize = blockSize / list.length // update blockSize value
            serviceName -> fillListWithIndices(blockSize, list.length, explorationSpaceSize).map(index => list(index))
        })

        // generate all the possible environment values per environment names and service
        val environmentDimensionOption = explorationSpaceDimensions.environment.map(
          serviceMap => serviceMap.map {
            case (serviceName, environmentMap) => serviceName -> environmentMap.map {
              case (variableName, list) =>
                blockSize = blockSize / list.length // update blockSize value
                variableName -> fillListWithIndices(blockSize, list.length, explorationSpaceSize).map(index => list(index))
            }

          })

        ExplorationSpace(explorationSpaceSize, usersDimension = usersDimensionOption, memoryDimensionOption, environmentDimensionOption)

      }

    }

  }

  /**
   *
   * Fills a list with indices for calculating the cartesian product.
   * If we want to create the list [0,0,1,1,2,2] we would pass: blockSize = 2, numValues = 3, listLength = 6
   * If we want to create the list [0,1,2,0,1,2] we would pass: blockSize = 1, numValues = 3, listLength = 6
   *
   * @param blockSize  decides the size of the blocks of a given value
   * @param numValues  how many possible indices should be generated
   * @param listLength the total length of the list
   * @return a list filled with indices according to the specified parameters
   */
  def fillListWithIndices(blockSize: Int, numValues: Int, listLength: Int): List[Index] = {

    // create the list where to store the values
    val list = mutable.MutableList[Index]()

    for {
      _ <- 0 until listLength / (blockSize * numValues) // how many times to produce a given sequence
      value: Index <- 0 until numValues // the sequence of indices
    } yield list ++= List.fill(blockSize)(value) // builds the list with the given value a blockSize number of times

    list.toList

  }

  /**
   * Calculates the cartesian product of all possible values in the exploration space
   *
   * @param explorationSpaceDimensions the dimensions of the exploration space
   * @return an option with the size of the exploration space. None if empty.
   */
  private def calculateExplorationSpaceSize(explorationSpaceDimensions: ExplorationSpaceDimensions): Option[Int] = {

    // calculate the overall size of the exploration space, e.g. how many possible experiments
    // it is the cartesian product of the possible values

    val explorationSpaceSizeOption: Option[Int] = for {

      usersDimensionLength <- explorationSpaceDimensions.users.map(_.length)

      memoryDimensionLength <- explorationSpaceDimensions.memory
        .map(memory => memory.map {
          case (_, values) => values.length
        }.product)

      environmentDimensionLength <- explorationSpaceDimensions.environment
        .map(environmentMap => environmentMap.map {
          case (_, variablesMap) => variablesMap.map {
            case (_, values) => values.length
          }.product
        }).map(_.product)

    } yield usersDimensionLength * memoryDimensionLength * environmentDimensionLength

    explorationSpaceSizeOption

  }

  /**
   * Find the index of the given experiment configuration in the exploration space
   * by traversing the space and performing the intersection of the found possible indices
   *
   * @param explorationSpace
   * @param explorationSpacePoint
   * @return
   */
  def getExperimentIndex(
    explorationSpace: ExplorationSpace,
    explorationSpacePoint: ExplorationSpacePoint): Option[ExperimentIndex] = {

    // create a set with all indices
    val allIndicesSet = (0 until explorationSpace.size).toSet

    val userIndicesOption = for {

      pointNumUsers <- explorationSpacePoint.users
      list <- explorationSpace.usersDimension

    } yield getIndicesSet(list)({ case (num, _) => num == pointNumUsers })

    var memoryIndices = allIndicesSet

    explorationSpacePoint.memory match {
      case Some(pointMemoryMap) => explorationSpace.memoryDimension match {
        case Some(memoryMap) => for {
          (serviceName, list) <- memoryMap
          if pointMemoryMap.keySet.contains(serviceName)
        } yield memoryIndices = memoryIndices intersect
          getIndicesSet[Bytes](list)({ case (value, _) => pointMemoryMap(serviceName) == value })
        case None => memoryIndices
      }
      case None => memoryIndices
    }

    var environmentIndices = allIndicesSet

    explorationSpacePoint.environment match {
      case Some(pointServiceMap) => explorationSpace.environmentDimension match {
        case Some(serviceMap) => for {
          (serviceName, environmentMap) <- serviceMap
          if pointServiceMap.keySet.contains(serviceName)
          (variableName, list) <- environmentMap
          if pointServiceMap(serviceName).contains(variableName)
        } yield environmentIndices = environmentIndices intersect getIndicesSet(list)({ case (value, _) => pointServiceMap(serviceName)(variableName) == value })
        case None => environmentIndices
      }
      case None => environmentIndices
    }

    val candidateSet = userIndicesOption match {
      case Some(userIndicesSet) => userIndicesSet intersect memoryIndices intersect environmentIndices
      case None => allIndicesSet intersect memoryIndices intersect environmentIndices
    }

    // there cannot be more than one candidate 
    if (candidateSet.size > 1) {
      None
    } else {
      Some(candidateSet.head)
    }

  }

  def getIndicesSet[T](list: List[T])(filter: ((T, Int)) => Boolean): Set[ExperimentIndex] =
    list.zipWithIndex.filter(filter).map {
      case (_, i) => i
    }.toSet
}
