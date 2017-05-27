package cloud.benchflow.dsl.explorationspace

import cloud.benchflow.dsl.definition.BenchFlowTest
import cloud.benchflow.dsl.definition.types.bytes.Bytes

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

  case class ExplorationSpace(
    users: Option[List[NumUsers]],
    memory: Option[Map[ServiceName, List[Bytes]]],
    environment: Option[Map[ServiceName, Map[VariableName, List[VariableValue]]]])

  case class ExplorationSpaceState(
    usersState: Option[(List[Index], DimensionLength)],
    memoryState: Option[Map[ServiceName, (List[Index], DimensionLength)]],
    environmentState: Option[Map[ServiceName, Map[VariableName, (List[Index], DimensionLength)]]])

  def generateExplorationSpace(test: BenchFlowTest): ExplorationSpace = {

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

    ExplorationSpace(users, memory, environment)

  }

  def generateInitialExplorationSpaceState(explorationSpace: ExplorationSpace): ExplorationSpaceState = {

    val explorationSpaceSizeOption: Option[Int] = calculateExplorationSpaceSize(explorationSpace)

    explorationSpaceSizeOption match {

      case None => ExplorationSpaceState(None, None, None)

      case Some(explorationSpaceSize) => {

        val usersState = explorationSpace.users.map(list => (
          List.fill(explorationSpaceSize)(-1),
          list.length))

        val memoryState = explorationSpace.memory.map(memory => memory
          .mapValues(list => (
            List.fill(explorationSpaceSize)(-1),
            list.length)))

        val environmentState = explorationSpace.environment.map(
          environmentMap => environmentMap.mapValues(
            environment => environment.mapValues(
              list => (
                List.fill(explorationSpaceSize)(-1),
                list.length))))

        ExplorationSpaceState(usersState, memoryState, environmentState)

      }
    }

  }

  private def calculateExplorationSpaceSize(explorationSpace: ExplorationSpace) = {

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