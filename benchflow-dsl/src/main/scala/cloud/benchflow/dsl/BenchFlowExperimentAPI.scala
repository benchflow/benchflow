package cloud.benchflow.dsl

import cloud.benchflow.dsl.definition.BenchFlowExperimentYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException
import cloud.benchflow.dsl.definition.{BenchFlowExperiment, BenchFlowExperimentYamlBuilder}
import net.jcazevedo.moultingyaml.{YamlObject, _}

import scala.util.{Failure, Success, Try}

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-22
 */
object BenchFlowExperimentAPI {

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

}
