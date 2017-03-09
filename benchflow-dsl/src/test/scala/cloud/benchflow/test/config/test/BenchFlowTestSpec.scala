package cloud.benchflow.test.config.test

import java.nio.file.Paths

import org.scalatest.{Matchers, FlatSpec}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 21/07/16.
  */
class BenchFlowTestSpec extends FlatSpec with Matchers with BenchFlowTestYamlProtocol {

  import net.jcazevedo.moultingyaml._
  import scala.io.Source.fromFile

  "BenchFlow test" should "parse correctly" in {

    val benchFlowTest =
      fromFile(Paths.get("./src/test/resources/benchflow-test.yml").toFile).mkString
        .parseYaml.convertTo[BenchFlowTest]

    benchFlowTest should have (
      'name ("WfMSTest")
    )

  }

}
