package cloud.benchflow.dsl

import cloud.benchflow.dsl.BenchFlowDSL.testFromYaml
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator.{ ExplorationSpace, ExplorationSpaceDimensions, ExplorationSpacePoint }

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-01
 */
object ExplorationSpace {

  type ExperimentDefinitionYamlString = String
  type DockerComposeYamlString = String
  type ExperimentIndex = Int

  /**
   *
   * @param testDefinitionYaml benchflow-test.yml
   * @throws cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
   * @return ExplorationSpace
   */
  @throws(classOf[BenchFlowDeserializationException])
  def explorationSpaceDimensionsFromTestYaml(testDefinitionYaml: String): ExplorationSpaceDimensions = {

    val test = testFromYaml(testDefinitionYaml)

    ExplorationSpaceGenerator.extractExplorationSpaceDimensions(test)

  }

  /**
   *
   *
   * @param testDefinitionYaml
   * @throws cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
   * @return
   */
  @throws(classOf[BenchFlowDeserializationException])
  def explorationSpaceFromTestYaml(testDefinitionYaml: String): ExplorationSpaceGenerator.ExplorationSpace = {

    val test = testFromYaml(testDefinitionYaml)

    val explorationSpaceDimensions = ExplorationSpaceGenerator.extractExplorationSpaceDimensions(test)

    ExplorationSpaceGenerator.generateExplorationSpace(explorationSpaceDimensions)

  }

  /**
   *
   * @param explorationSpace         the exploration space where the experiment is
   * @param experimentIndex          the index of the experiment
   * @param testDefinitionYamlString the test definition to build the experiment definition from as a YAML string
   * @param dockerComposeYamlString  the docker compose to build the experiment docker-compose from as a YAML string
   * @return a tuple of experiment definition YAML string and docker compose YAML string
   */
  def generateExperimentBundle(
    explorationSpace: ExplorationSpace,
    experimentIndex: ExperimentIndex,
    testDefinitionYamlString: String,
    dockerComposeYamlString: String): (ExperimentDefinitionYamlString, DockerComposeYamlString) = {

    // build the experiment definition
    var experimentBuilder = BenchFlowDSL.experimentYamlBuilderFromTestYaml(testDefinitionYamlString)

    experimentBuilder = explorationSpace.usersDimension match {
      case Some(list) => experimentBuilder.numUsers(list(experimentIndex))
      case None => experimentBuilder
    }

    val experimentDefinitionYamlString = experimentBuilder.build()

    // build the docker compose YAML
    var dockerComposeBuilder = BenchFlowDSL.dockerComposeYamlBuilderFromDockerComposeYaml(dockerComposeYamlString)

    explorationSpace.memoryDimension match {
      case Some(map) => map foreach {
        case (serviceName, list) => dockerComposeBuilder = dockerComposeBuilder.memLimit(serviceName, list(experimentIndex))
      }
      case None => // nothing to do
    }

    explorationSpace.environmentDimension match {
      case Some(serviceMap) => serviceMap foreach {
        case (serviceName, environmentMap) => environmentMap foreach {
          case (environmentVariable, list) => dockerComposeBuilder = dockerComposeBuilder.environmentVariable(
            serviceName,
            environmentVariable,
            list(experimentIndex))
        }
      }
      case None => // nothing to do
    }

    val newDockerComposeYamlString = dockerComposeBuilder.build()

    (experimentDefinitionYamlString, newDockerComposeYamlString)
  }

  /**
   * Generates and experiment bundle from the given exploration space point.
   *
   * @param explorationSpace         the exploration space where the experiment is
   * @param explorationSpacePoint    the point in the exploration space from which to generate the experiment
   * @param testDefinitionYamlString the test definition to build the experiment definition from as a YAML string
   * @param dockerComposeYamlString  the docker compose to build the experiment docker-compose from as a YAML string
   * @return a tuple of experiment definition YAML string, docker compose YAML string and experiment number
   *         (index in exploration space) as an Option. None if point could not be found, or if multiple were found.
   */
  def generateExperimentBundle(
    explorationSpace: ExplorationSpace,
    explorationSpacePoint: ExplorationSpacePoint,
    testDefinitionYamlString: String,
    dockerComposeYamlString: String): Option[(ExperimentDefinitionYamlString, DockerComposeYamlString, ExperimentIndex)] = {

    ExplorationSpaceGenerator.getExperimentIndex(explorationSpace, explorationSpacePoint) match {
      case None => None
      case Some(experimentIndex) =>

        val (experimentDefinitionYamlString, newDockerComposeYamlString) = generateExperimentBundle(
          explorationSpace,
          experimentIndex,
          testDefinitionYamlString,
          dockerComposeYamlString)

        Some(experimentDefinitionYamlString, newDockerComposeYamlString, experimentIndex)
    }

  }

}
