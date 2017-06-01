package cloud.benchflow.dsl

import cloud.benchflow.dsl.BenchFlowDSL.testFromYaml
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator.{ ExplorationSpace, ExplorationSpaceState }

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-01
 */
object ExplorationSpace {

  /**
   *
   * @param testDefinitionYaml benchflow-test.yml
   * @throws cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
   * @return ExplorationSpace
   */
  @throws(classOf[BenchFlowDeserializationException])
  def explorationSpaceFromTestYaml(testDefinitionYaml: String): ExplorationSpace = {

    val test = testFromYaml(testDefinitionYaml)

    ExplorationSpaceGenerator.generateExplorationSpace(test)

  }

  /**
   *
   * @param explorationSpace
   * @return initial state of exploration space
   */
  def getInitialExplorationSpaceState(explorationSpace: ExplorationSpace): ExplorationSpaceState = {

    ExplorationSpaceGenerator.generateInitialExplorationSpaceState(explorationSpace)

  }

  /**
   *
   * @param explorationSpace
   * @return a complete exploration space that can be traversed in order
   */
  def getOneAtATimeExplorationSpaceState(explorationSpace: ExplorationSpace): ExplorationSpaceState = {

    ExplorationSpaceGenerator.oneAtATimeExplorationSpace(explorationSpace)

  }

  //  /**
  //   *
  //   * @param explorationSpace         the exploration space where the experiment is
  //   * @param experimentNumber         the number of the experiment (index)
  //   * @param testDefinitionYamlString the test definition to build the experiment definition from as a YAML string
  //   * @param dockerComposeYamlString  the docker compose to build the experiment docker-compose from as a YAML string
  //   * @return
  //   */
  //  def getExperimentBundle(explorationSpace: ExplorationSpace, experimentNumber: Int, testDefinitionYamlString: String, dockerComposeYamlString: String): (String, String) = {
  //
  //  }

}
