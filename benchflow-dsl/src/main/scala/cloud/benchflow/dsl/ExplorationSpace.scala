package cloud.benchflow.dsl

import cloud.benchflow.dsl.BenchFlowDSL.testFromYaml
import cloud.benchflow.dsl.definition.configuration.BenchFlowExperimentConfiguration
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator.ExplorationSpaceDimensions

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-01
 */
object ExplorationSpace {

  type ExperimentDefinitionYamlString = String
  type DockerComposeYamlString = String
  type ExperimentNumber = Int

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
   * @param experimentNumber         the number of the experiment (index)
   * @param testDefinitionYamlString the test definition to build the experiment definition from as a YAML string
   * @param dockerComposeYamlString  the docker compose to build the experiment docker-compose from as a YAML string
   * @return a tuple of experiment definition YAML string and docker compose YAML string
   */
  def generateExperimentBundle(
    explorationSpace: ExplorationSpaceDimensions,
    experimentNumber: ExperimentNumber,
    testDefinitionYamlString: String,
    dockerComposeYamlString: String): (ExperimentDefinitionYamlString, DockerComposeYamlString) = {

    // get the experiment configuration from the exploration space

    // generate the experiment definition YAML

    // generate the docker compose YAML
    ("to do", "to do")
  }

  /**
   *
   * @param explorationSpace         the exploration space where the experiment is
   * @param experimentConfiguration  the configuration for the experiment to be generated
   * @param testDefinitionYamlString the test definition to build the experiment definition from as a YAML string
   * @param dockerComposeYamlString  the docker compose to build the experiment docker-compose from as a YAML string
   * @return a tuple of experiment definition YAML string, docker compose YAML string and experiment number (index in exploration space)
   */
  def generateExperimentBundle(
    explorationSpace: ExplorationSpaceDimensions,
    experimentConfiguration: BenchFlowExperimentConfiguration,
    testDefinitionYamlString: String,
    dockerComposeYamlString: String): (ExperimentDefinitionYamlString, DockerComposeYamlString, ExperimentNumber) = {

    // find the index of the given experiment configuration in the exploration space

    // generate the experiment definition YAML

    // generate the docker compose YAML

    ("to do", "to do", -1)

  }

}
