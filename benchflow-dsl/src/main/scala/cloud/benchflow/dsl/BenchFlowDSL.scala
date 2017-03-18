package cloud.benchflow.dsl

import cloud.benchflow.dsl.definition.BenchFlowTest
import cloud.benchflow.dsl.definition.BenchFlowTestYamlProtocol._
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
    val test:Try[BenchFlowTest] = testDefinitionYaml.parseYaml.convertTo[Try[BenchFlowTest]]

    // TODO - validate semantic in separate function on the object

    test
  }

  def testToYaml(benchFlowTest: BenchFlowTest): String = {

    // TODO - validate semantic in separate function on the object

    // TODO - write to YAML
    // TODO - document why we wrap in a Try (e.g. because of library and deserialization)
    val testYaml: YamlObject = Try(benchFlowTest).toYaml.asYamlObject

    testYaml.prettyPrint

  }

  def validateTest(benchFlowTest: BenchFlowTest): Boolean = {

    // TODO - validate semantic in separate function on the object

    false
  }

  // TODO - add methods for common operations/changes to tests/experiments

}
