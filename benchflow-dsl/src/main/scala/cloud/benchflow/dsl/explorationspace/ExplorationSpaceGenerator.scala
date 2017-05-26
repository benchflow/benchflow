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

  case class ExplorationSpaceState(
    usersState: Option[(Int, DimensionLength)],
    memoryState: Option[Map[ServiceName, (Int, DimensionLength)]],
    environmentState: Option[Map[ServiceName, Map[VariableName, (Int, DimensionLength)]]])

  case class ExplorationSpace(
    users: Option[List[NumUsers]],
    memory: Option[Map[ServiceName, List[Bytes]]],
    environment: Option[Map[ServiceName, Map[VariableName, List[VariableValue]]]])

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

  def generateExplorationSpaceState(explorationSpace: ExplorationSpace): ExplorationSpaceState = {

    val usersState = explorationSpace.users.map(list => (0, list.length))

    val memoryState = explorationSpace.memory.map(memory => memory.mapValues(list => (0, list.length)))

    val environmentState = explorationSpace.environment.map(
      environmentMap => environmentMap.mapValues(
        environment => environment.mapValues(
          list => (0, list.length))))

    ExplorationSpaceState(usersState, memoryState, environmentState)

  }

}
