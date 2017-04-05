package cloud.benchflow.dsl

import cloud.benchflow.dsl.definition.{ BenchFlowExperiment, BenchFlowTest }
import cloud.benchflow.dsl.definition.BenchFlowTestYamlProtocol._
import cloud.benchflow.dsl.definition.BenchFlowExperimentYamlProtocol._
import net.jcazevedo.moultingyaml._

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 10.03.17.
 */
object BenchFlowDSL {

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

  // TODO - add methods for common operations/changes to tests/experiments

  def experimentFromTestYaml(testDefinitionYaml: String): Try[BenchFlowExperiment] = {

    // convert to experiment and validate syntax
    val experiment: Try[BenchFlowExperiment] = testDefinitionYaml.parseYaml.convertTo[Try[BenchFlowExperiment]]

    // TODO - validate semantic in separate function on the object

    experiment

  }

  def experimentFromExperimentYaml(experimentDefinitionYaml: String): Try[BenchFlowExperiment] = {

    val experiment: Try[BenchFlowExperiment] = experimentDefinitionYaml.parseYaml.convertTo[Try[BenchFlowExperiment]]

    // TODO - validate semantic in separate function on the object

    experiment

  }

  def experimentToYamlString(benchFlowExperiment: BenchFlowExperiment): String = {

    // TODO - validate semantic in separate function on the object

    // write to YAML
    val experimentYaml: YamlObject = benchFlowExperiment.toYaml.asYamlObject

    experimentYaml.prettyPrint

  }

}
