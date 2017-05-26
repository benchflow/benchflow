package cloud.benchflow.dsl

import cloud.benchflow.dsl.definition.BenchFlowExperimentYamlProtocol._
import cloud.benchflow.dsl.definition.BenchFlowTestYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
import cloud.benchflow.dsl.definition.{ BenchFlowExperiment, BenchFlowExperimentYamlBuilder, BenchFlowTest }
import cloud.benchflow.dsl.dockercompose.DockerComposeYamlBuilder
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator.{ ExplorationSpace, ExplorationSpaceState }
import net.jcazevedo.moultingyaml._

import scala.util.{ Failure, Success, Try }

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 10.03.17.
 */
object BenchFlowDSL {

  /**
   * To serialize/deserialize YAML this library uses https://github.com/jcazevedo/moultingyaml.
   */

  /**
   *
   * @param testDefinitionYaml
   * @throws cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
   * @return
   */
  @throws(classOf[BenchFlowDeserializationException])
  def testFromYaml(testDefinitionYaml: String): BenchFlowTest = {

    // validates syntax
    // TODO - document why we wrap in a Try (e.g. because of library and deserialization)
    val triedTest: Try[BenchFlowTest] = testDefinitionYaml.parseYaml.convertTo[Try[BenchFlowTest]]

    // TODO - validate semantic in separate function on the object

    triedTest match {
      case Success(test) => test
      case Failure(ex) => throw ex
    }

  }

  def testToYamlString(benchFlowTest: BenchFlowTest): String = {

    // TODO - validate semantic in separate function on the object

    // write to YAML
    val testYaml: YamlObject = benchFlowTest.toYaml.asYamlObject

    testYaml.prettyPrint

  }

  def validateTest(benchFlowTest: BenchFlowTest): Boolean = {

    // TODO - validate semantic in separate function on the object

    false
  }

  /**
   * Generates a BenchFlowExperiment from a BenchFlow Test Definition YAML string with
   * values equal to those in the YAML string.
   *
   * Will return a failure if the Test Definition is invalid.
   *
   * @param testDefinitionYaml
   * @throws cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
   * @return
   */
  @throws(classOf[BenchFlowDeserializationException])
  def experimentFromTestYaml(testDefinitionYaml: String): BenchFlowExperiment = {

    // convert to experiment and validate syntax
    // NOTE: Since the key-value pairs in an experiment definition is a subset of those
    // found in a test, we can use the same YAML protocol code to parse from a test definition
    // to an experiment, as from an experiment definition to an experiment. Key-value pairs that
    // should not be in an experiment will simply be ignored.

    val triedExperiment: Try[BenchFlowExperiment] = testDefinitionYaml.parseYaml.convertTo[Try[BenchFlowExperiment]]

    // TODO - validate semantic in separate function on the object

    triedExperiment match {
      case Success(experiment) => experiment
      case Failure(ex) => throw ex
    }

  }

  /**
   * Generates a BenchFlowExperiment from a BenchFlow Experiment Definition YAML string.
   *
   * Will return a failure if the Experiment Definition is invalid.
   *
   * @param experimentDefinitionYaml
   * @throws cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
   * @return
   */
  @throws(classOf[BenchFlowDeserializationException])
  def experimentFromExperimentYaml(experimentDefinitionYaml: String): BenchFlowExperiment = {

    val triedExperiment: Try[BenchFlowExperiment] = experimentDefinitionYaml.parseYaml.convertTo[Try[BenchFlowExperiment]]

    // TODO - validate semantic in separate function on the object

    triedExperiment match {
      case Success(experiment) => experiment
      case Failure(ex) => throw ex
    }

  }

  /**
   * Generates a YAML string from a BenchFlowExperiment.
   *
   * @param benchFlowExperiment
   * @return
   */
  def experimentToYamlString(benchFlowExperiment: BenchFlowExperiment): String = {

    // TODO - validate semantic in separate function on the object

    // write to YAML
    val experimentYaml: YamlObject = benchFlowExperiment.toYaml.asYamlObject

    experimentYaml.prettyPrint

  }

  /**
   * Returns a BenchFLowExperimentYamlBuilder for creating custom experiment yaml
   *
   * @param testDefinitionYaml
   * @throws cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
   * @return
   */
  @throws(classOf[BenchFlowDeserializationException])
  def experimentYamlBuilderFromTestYaml(testDefinitionYaml: String): BenchFlowExperimentYamlBuilder = {

    val experiment = experimentFromTestYaml(testDefinitionYaml)

    new BenchFlowExperimentYamlBuilder(experiment)

  }

  /**
   * Returns a DockerComposeYamlBuilder for creating a custom docker-compose yaml
   *
   * @param dockerComposeYamlString docker-compose.yml
   * @return
   */
  def dockerComposeYamlBuilderFromDockerComposeYaml(dockerComposeYamlString: String): DockerComposeYamlBuilder = {

    val dockerComposeYaml = dockerComposeYamlString.parseYaml

    new DockerComposeYamlBuilder(dockerComposeYaml)

  }

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

    ExplorationSpaceGenerator.generateExplorationSpaceState(explorationSpace)

  }

}
