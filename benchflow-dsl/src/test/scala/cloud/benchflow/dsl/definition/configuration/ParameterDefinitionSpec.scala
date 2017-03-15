package cloud.benchflow.dsl.definition.configuration

import cloud.benchflow.dsl.definition.configuration.simone._
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 20/07/16.
  */
class ParameterDefinitionSpec extends FlatSpec with Matchers with ParameterDefinitionYamlProtocol {

  import net.jcazevedo.moultingyaml._


  "Service parameter definition" should "parse correctly" in {

    val serviceParameterDefiniton =
      """
        |camunda:
        |  $A_FACTORS_DIMENSION:
        |    values: [ first, second, third ]
      """.stripMargin.parseYaml.convertTo[ServiceParameterDefinition[_]]

    val parsedServiceParameterDefinition = ApplicationParameterDefinition[String](
      name = "$A_FACTORS_DIMENSION",
      serviceName = "camunda",
      dimensionDefinition = Factors(Vector("first", "second", "third"))
    )

    serviceParameterDefiniton should be (parsedServiceParameterDefinition)

  }


  "Memory parameter definition" should "parse correctly" in {

    val memoryParameterDefinition: SystemParameterDefinition[Double] =
      """
        |memory:
        |  range: 256...2048
        |  step: '+256'
      """.stripMargin.parseYaml.convertTo[MemoryDefinition]

    val parsedMemoryParameterDefinition = MemoryDefinition(
      Step(
        min = 256d,
        max = 2048d,
        step = 256d,
        stepFunction = implicitly[Numeric[Double]].plus
      )
    )

    memoryParameterDefinition.dimensionDefinition.asInstanceOf[Step] should have (
      'min (parsedMemoryParameterDefinition.dimensionDefinition.asInstanceOf[Step].min),
      'max (parsedMemoryParameterDefinition.dimensionDefinition.asInstanceOf[Step].max),
      'step (parsedMemoryParameterDefinition.dimensionDefinition.asInstanceOf[Step].step)
    )

  }


}
