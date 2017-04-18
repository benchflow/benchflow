package cloud.benchflow.dsl

import cloud.benchflow.dsl.definition.BenchFlowExperimentYamlProtocol._
import cloud.benchflow.dsl.definition.BenchFlowTestYamlProtocol._
import cloud.benchflow.dsl.definition.{BenchFlowExperiment, BenchFlowTest}
import net.jcazevedo.moultingyaml._

import scala.util.{Failure, Success, Try}

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 10.03.17.
 */
object BenchFlowDSL {

  /**
   * To serialize/deserialize YAML this library uses https://github.com/jcazevedo/moultingyaml.
   */

  def testFromYaml(testDefinitionYaml: String): Try[BenchFlowTest] = {

    // validates syntax
    // TODO - document why we wrap in a Try (e.g. because of library and deserialization)
    val test: Try[BenchFlowTest] = testDefinitionYaml.parseYaml.convertTo[Try[BenchFlowTest]]

    // TODO - validate semantic in separate function on the object

    test
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

  def experimentFromTestYamlNumUsers(testDefinitionYaml: String, numUsers: Int): String = {

    // TODO - implement me and test

    // TODO - change to separate file and use builder pattern

    val triedExperiment: Try[BenchFlowExperiment] = testDefinitionYaml.parseYaml.convertTo[Try[BenchFlowExperiment]]

    triedExperiment match {
      case Success(experiment) => {
        experiment.configuration.users -> numUsers
        experiment.toYaml.asYamlObject.prettyPrint
      }
      case Failure(ex) => throw ex
    }

  }

  // TODO - add methods for common operations/changes to tests/experiments

  /**
   * Generates a BenchFlowExperiment from a BenchFlow Test Definition YAML string with
   * values equal to those in the YAML string.
   *
   * Will return a failure if the Test Definition is invalid.
   *
   * @param testDefinitionYaml
   * @return
   */
  def experimentFromTestYaml(testDefinitionYaml: String): Try[BenchFlowExperiment] = {

    // convert to experiment and validate syntax
    // NOTE: Since the key-value pairs in an experiment definition is a subset of those
    // found in a test, we can use the same YAML protocol code to parse from a test definition
    // to an experiment, as from an experiment definition to an experiment. Key-value pairs that
    // should not be in an experiment will simply be ignored.
    val experiment: Try[BenchFlowExperiment] = testDefinitionYaml.parseYaml.convertTo[Try[BenchFlowExperiment]]

    // TODO - validate semantic in separate function on the object

    experiment

  }

  /**
   * Generates a BenchFlowExperiment from a BenchFlow Experiment Definition YAML string.
   *
   * Will return a failure if the Experiment Definition is invalid.
   *
   * @param experimentDefinitionYaml
   * @return
   */
  def experimentFromExperimentYaml(experimentDefinitionYaml: String): Try[BenchFlowExperiment] = {

    val experiment: Try[BenchFlowExperiment] = experimentDefinitionYaml.parseYaml.convertTo[Try[BenchFlowExperiment]]

    // TODO - validate semantic in separate function on the object

    experiment

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

}
