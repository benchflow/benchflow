package cloud.benchflow.dsl.definition

import cloud.benchflow.dsl.definition.sut.ConfigurationYamlProtocol
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 18/07/16.
  */
class PropertiesSpec extends FlatSpec with Matchers with ConfigurationYamlProtocol {

  import net.jcazevedo.moultingyaml._

  "Properties" should "parse correctly" in {

    val properties =
      """
        |properties:
        |  hello: hello
        |  foo:
        |     bar: bar
        |  foo:
        |  - bar
      """.stripMargin.parseYaml.convertTo[Properties]

    val parsedProperties = Properties(
      properties = Map(
        "hello" -> "hello",
        "foo" -> Map("bar" -> "bar"),
        "foo" -> List("bar")
      )
    )

    properties should be (parsedProperties)

  }

}
