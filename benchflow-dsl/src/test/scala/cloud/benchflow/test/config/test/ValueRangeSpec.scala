package cloud.benchflow.test.config.test

import org.scalatest.{Matchers, FlatSpec}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 19/07/16.
  */
class ValueRangeSpec extends FlatSpec with Matchers with ValueRangeYamlProtocol {

  import net.jcazevedo.moultingyaml._


  "Int constant value assignment" should "parse correctly" in {

    val constant = "1000".parseYaml.convertTo[Constant[_]]
    val parsedConstant = Constant(1000)

    constant should be (parsedConstant)

  }


  "Double constant value assignment" should "parse correctly" in {

    val constant = "3.5".parseYaml.convertTo[Constant[_]]

    val parsedConstant = Constant(3.5)

    constant should be (parsedConstant)

  }


  "String constant value assignment" should "parse correctly" in {

    val constant = "constString".parseYaml.convertTo[Constant[_]]

    val parsedConstant = Constant("constString")

    constant should be (parsedConstant)

  }


  "String factors assignment" should "parse correctly" in {

    val factors = "values: [ a, b, c ]".parseYaml.convertTo[Factors[_]]

    val parsedFactors = Factors(Vector("a", "b", "c"))

    factors should be (parsedFactors)

  }


  "Mixed int/double factors assignment" should "parse correctly" in {

    val factors = "values: [ 1, 1.5, 2 ]".parseYaml.convertTo[Factors[_]]

    val parsedFactors = Factors(Vector(1, 1.5, 2))

    factors should be (parsedFactors)

  }


  "Boolean factors assignment" should "parse correctly" in {

    val factors = "values: [ true, false ]".parseYaml.convertTo[Factors[_]]

    val parsedFactors = Factors(Vector(true, false))

    factors should be (parsedFactors)

  }


  "Step function" should "parse correctly" in {

    val stepFunction =
      """
        |range: 1...100
        |step: '+1'
      """.stripMargin.parseYaml.convertTo[Step]

    val parsedStepFunction = Step(
      min = 1d,
      max = 100d,
      step = 1d,
      stepFunction = implicitly[Numeric[Double]].plus
    )

    stepFunction should have (
      'min (parsedStepFunction.min),
      'max (parsedStepFunction.max),
      'step (parsedStepFunction.step)
    )

  }


  "Step function" should "increment correctly" in {

    val stepFunction =
      """
        |range: 1...100
        |step: '+1'
      """.stripMargin.parseYaml.convertTo[Step]

    val parsedStepFunction = Step(
      min = 1d,
      max = 100d,
      step = 1d,
      stepFunction = implicitly[Numeric[Double]].plus
    )

    stepFunction.increment(1) should (be (parsedStepFunction.increment(1)) and be (2))

  }


  "Step function" should "compute dimension size correctly" in {

    val stepFunction =
      """
        |range: 1...100
        |step: '+1'
      """.stripMargin.parseYaml.convertTo[Step]


    stepFunction.size should be (100)

  }

  "ValueRangeYamlProtocol" should "infer constant value range" in {

    val constant = "1000".parseYaml.convertTo[ValueRange[_]]
    val parsedConstant = Constant(1000)

    constant should be (parsedConstant)

  }

  "ValueRangeYamlProtocol" should "infer factors value range" in {

    val factors = "values: [ a, b, c ]".parseYaml.convertTo[ValueRange[_]]

    val parsedFactors = Factors(Vector("a", "b", "c"))

    factors should be (parsedFactors)

  }

  "ValueRangeYamlProtocol" should "infer ranged value range" in {

    val stepFunction =
      """
        |range: 1...100
        |step: '+1'
      """.stripMargin.parseYaml.convertTo[ValueRange[_]].asInstanceOf[Step]

    val parsedStepFunction = Step(
      min = 1d,
      max = 100d,
      step = 1d,
      stepFunction = implicitly[Numeric[Double]].plus
    )

    stepFunction.increment(1) should (be (parsedStepFunction.increment(1)) and be (2))

  }


}