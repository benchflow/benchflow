package cloud.benchflow.dsl

import java.nio.file.Paths

import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue, _}
import org.junit.{Assert, Test}
import org.scalatest.junit.AssertionsForJUnit

import scala.io.Source
import scala.util.Try


/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  *         Created on 21/07/16.
  */
//class BenchFlowTestSpec extends FlatSpec with Matchers with BenchFlowTestYamlProtocol {
class BenchFlowDSLTest extends AssertionsForJUnit {

  case class HelloThere(hello: String, number: Option[Int])

  object TestYamlProtocol extends DefaultYamlProtocol {

    implicit object HelloThereFormat extends YamlFormat[Try[HelloThere]] {
      override def write(obj: Try[HelloThere]): YamlValue = ???

      override def read(yaml: YamlValue): Try[HelloThere] = {

        val yamlObject = yaml.asYamlObject

        for (

          hello <- Try(yamlObject.fields(YamlString("hello")).convertTo[String]);

          // IF key is optional
          number <- Try(yamlObject.getFields(YamlString("number")).headOption.map(_.convertTo[Int]))

        ) yield HelloThere(hello = hello, number = number)

      }
    }


  }

  @Test def loadTestDefinition(): Unit = {

    val testYaml = Source.fromFile(Paths.get(BenchFlowLoadTestExample).toFile).mkString

    val benchFlowTest = BenchFlowDSL.testFromYaml(testYaml)

    Assert.assertTrue(benchFlowTest.isSuccess)

  }

  @Test def helloTest(): Unit = {

    import TestYamlProtocol._

    val testYaml =
      """
      hello: 1%
      number: 12
      """

    val hello = testYaml.parseYaml.convertTo[Try[HelloThere]]

    Assert.assertTrue(hello.isSuccess)

  }

  //  import net.jcazevedo.moultingyaml._
  //
  //  import scala.io.Source.fromFile
  //
  //  "BenchFlow test" should "parse correctly" in {
  //
  //    val benchFlowTest =
  //      fromFile(Paths.get("./src/test/resources/benchflow-test.yml").toFile).mkString
  //        .parseYaml.convertTo[BenchFlowTest]
  //
  //    benchFlowTest should have(
  //      'name ("WfMSTest")
  //    )
  //
  //  }

}
