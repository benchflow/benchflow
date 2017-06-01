package cloud.benchflow.dsl.explorationspace

import cloud.benchflow.dsl.definition.BenchFlowTest
import cloud.benchflow.dsl.definition.types.bytes.Bytes

import scala.collection.mutable

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-25
 */
object ExplorationSpaceGenerator {

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

  case class ExplorationSpaceState(
    usersState: Option[(List[Index], DimensionLength)],
    memoryState: Option[Map[ServiceName, (List[Index], DimensionLength)]],
    environmentState: Option[Map[ServiceName, Map[VariableName, (List[Index], DimensionLength)]]])

  def generateExplorationSpaceDimensions(test: BenchFlowTest): ExplorationSpaceDimensions = {

    val users: Option[List[NumUsers]] = test.configuration.goal.explorationSpace.flatMap(
      _.workload.flatMap(
        _.users.map(
          _.values)))

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

  def generateInitialExplorationSpaceState(explorationSpace: ExplorationSpaceDimensions): ExplorationSpaceState = {

    val explorationSpaceSizeOption: Option[Int] = calculateExplorationSpaceSize(explorationSpace)

    explorationSpaceSizeOption match {

      case None => ExplorationSpaceState(None, None, None)

      case Some(explorationSpaceSize) => {

        val usersState = explorationSpace.users.map(list => (
          List.fill(explorationSpaceSize)(-1),
          list.length))

        val memoryState = explorationSpace.memory.map(serviceMap => serviceMap
          .mapValues(list => (
            List.fill(explorationSpaceSize)(-1),
            list.length)))

        val environmentState = explorationSpace.environment.map(
          serviceMap => serviceMap.mapValues(
            environmentMap => environmentMap.mapValues(
              list => (
                List.fill(explorationSpaceSize)(-1),
                list.length))))

        ExplorationSpaceState(usersState, memoryState, environmentState)

      }
    }

  }

  def oneAtATimeExplorationSpace(explorationSpace: ExplorationSpaceDimensions): ExplorationSpaceState = {

    val explorationSpaceSizeOption = calculateExplorationSpaceSize(explorationSpace)

    explorationSpaceSizeOption match {

      case None => ExplorationSpaceState(None, None, None)

      case Some(explorationSpaceSize) => {

        var shiftValue = explorationSpaceSize

        val usersStateOption = explorationSpace.users.map {
          case (list) =>
            shiftValue = shiftValue / list.length // update shift value
            (fillList(shiftValue, list.length, explorationSpaceSize), list.length)
        }

        val memoryStateOption = explorationSpace.memory.map(memory => memory.map {
          case (serviceName, list) =>
            shiftValue = shiftValue / list.length // update shift value
            serviceName -> (fillList(shiftValue, list.length, explorationSpaceSize), list.length)
        })

        val environmentStateOption = explorationSpace.environment.map(
          serviceMap => serviceMap.map {
            case (serviceName, environmentMap) => serviceName -> environmentMap.map {
              case (variableName, list) =>
                shiftValue = shiftValue / list.length // update shift value
                variableName -> (fillList(shiftValue, list.length, explorationSpaceSize), list.length)
            }

          })

        ExplorationSpaceState(usersState = usersStateOption, memoryStateOption, environmentStateOption)

      }

    }

  }

  def fillList(shift: Int, numValues: Int, explorationSpaceSize: Int): List[Index] = {

    val list = mutable.MutableList[Index]()

    for {
      _ <- 0 until explorationSpaceSize / (shift * numValues) // how many times to run sequence
      value: Index <- 0 until numValues // sequence of indexes
    } yield list ++= List.fill(shift)(value)

    list.toList

  }

  private def calculateExplorationSpaceSize(explorationSpace: ExplorationSpaceDimensions) = {

    // calculate the overall size of the exploration space, e.g. how many possible experiments

    val explorationSpaceSizeOption: Option[Int] = for {

      usersDimensionLength <- explorationSpace.users.map(_.length)

      memoryDimensionLength <- explorationSpace.memory
        .map(memory => memory.map {
          case (_, values) => values.length
        }.product)

      environmentDimensionLength <- explorationSpace.environment
        .map(environmentMap => environmentMap.map {
          case (_, variablesMap) => variablesMap.map {
            case (_, values) => values.length
          }.product
        }).map(_.product)

    } yield usersDimensionLength * memoryDimensionLength * environmentDimensionLength

    explorationSpaceSizeOption

  }
}
