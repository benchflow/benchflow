package cloud.benchflow.dsl

import cloud.benchflow.dsl.BenchFlowTestAPI.testFromYaml
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
import cloud.benchflow.dsl.explorationspace.javatypes.{JavaCompatExplorationSpace, JavaCompatExplorationSpaceDimensions, JavaCompatExplorationSpacePoint}
import cloud.benchflow.dsl.explorationspace.{ExplorationSpaceGenerator, JavaCompatExplorationSpaceConverter}

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-01
 */
object ExplorationSpaceAPI {

  type ExperimentDefinitionYamlString = String
  type DockerComposeYamlString = String
  type ExperimentIndex = Int

  /**
   *
   * @param testDefinitionYaml benchflow-test.yml
   * @throws cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
   * @return JavaCompatExplorationSpaceDimensions
   */
  @throws(classOf[BenchFlowDeserializationException])
  def explorationSpaceDimensionsFromTestYaml(testDefinitionYaml: String): JavaCompatExplorationSpaceDimensions = {

    val test = testFromYaml(testDefinitionYaml)

    val explorationSpaceDimensions = ExplorationSpaceGenerator.extractExplorationSpaceDimensions(test)

    JavaCompatExplorationSpaceConverter.convertToJavaCompatExplorationSpaceDimensions(explorationSpaceDimensions)

  }

  /**
   *
   *
   * @param testDefinitionYaml
   * @throws cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
   * @return JavaCompatExplorationSpace
   */
  @throws(classOf[BenchFlowDeserializationException])
  def explorationSpaceFromTestYaml(testDefinitionYaml: String): JavaCompatExplorationSpace = {

    val test = testFromYaml(testDefinitionYaml)

    val explorationSpaceDimensions = ExplorationSpaceGenerator.extractExplorationSpaceDimensions(test)

    val explorationSpace = ExplorationSpaceGenerator.generateExplorationSpace(explorationSpaceDimensions)

    JavaCompatExplorationSpaceConverter.convertToJavaCompatExplorationSpace(explorationSpace)

  }

  /**
   *
   * @param javaCompatExplorationSpace the exploration space where the experiment is
   * @param experimentIndex            the index of the experiment
   * @param testDefinitionYamlString   the test definition to build the experiment definition from as a YAML string
   * @param dockerComposeYamlString    the docker compose to build the experiment docker-compose from as a YAML string
   * @return a tuple of experiment definition YAML string and docker compose YAML string
   */
  def generateExperimentBundle(
    javaCompatExplorationSpace: JavaCompatExplorationSpace,
    experimentIndex: ExperimentIndex,
    testDefinitionYamlString: String,
    dockerComposeYamlString: String): (ExperimentDefinitionYamlString, DockerComposeYamlString) = {

    // convert from java compat type
    val explorationSpace = JavaCompatExplorationSpaceConverter.convertFromJavaCompatExplorationSpace(javaCompatExplorationSpace)

    // build the experiment definition
    var experimentBuilder = BenchFlowExperimentAPI.experimentYamlBuilderFromTestYaml(testDefinitionYamlString)

    experimentBuilder = explorationSpace.usersDimension match {
      case Some(list) => experimentBuilder.numUsers(list(experimentIndex))
      case None => experimentBuilder
    }

    val experimentDefinitionYamlString = experimentBuilder.build()

    // build the docker compose YAML
    var dockerComposeBuilder = DeploymentDescriptorAPI.dockerComposeYamlBuilderFromDockerComposeYaml(dockerComposeYamlString)

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
   * @param javaCompatExplorationSpace      the exploration space where the experiment is
   * @param javaCompatexplorationSpacePoint the point in the exploration space from which to generate the experiment
   * @param testDefinitionYamlString        the test definition to build the experiment definition from as a YAML string
   * @param dockerComposeYamlString         the docker compose to build the experiment docker-compose from as a YAML string
   * @return a tuple of experiment definition YAML string, docker compose YAML string and the experiment index as an Option.
   *         It returns Some if a point was found, or None if a point could not be found or if multiple points were found.
   */
  def generateExperimentBundle(
    javaCompatExplorationSpace: JavaCompatExplorationSpace,
    javaCompatexplorationSpacePoint: JavaCompatExplorationSpacePoint,
    testDefinitionYamlString: String,
    dockerComposeYamlString: String): Option[(ExperimentDefinitionYamlString, DockerComposeYamlString, ExperimentIndex)] = {

    // convert from java compat types
    val explorationSpace = JavaCompatExplorationSpaceConverter.convertFromJavaCompatExplorationSpace(javaCompatExplorationSpace)
    val explorationSpacePoint = JavaCompatExplorationSpaceConverter.convertFromJavaCompatExplorationSpacePoint(javaCompatexplorationSpacePoint)

    // generate experiment bundle
    ExplorationSpaceGenerator.getExperimentIndex(explorationSpace, explorationSpacePoint) match {
      case None => None
      case Some(experimentIndex) =>

        val (experimentDefinitionYamlString, newDockerComposeYamlString) = generateExperimentBundle(
          javaCompatExplorationSpace,
          experimentIndex,
          testDefinitionYamlString,
          dockerComposeYamlString)

        Some(experimentDefinitionYamlString, newDockerComposeYamlString, experimentIndex)
    }

  }

}
