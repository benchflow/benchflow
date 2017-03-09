package cloud.benchflow.test.config.test

import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 20/07/16.
  */
class GoalSpec extends FlatSpec with Matchers with GoalYamlProtocol {

  import net.jcazevedo.moultingyaml._

  "Goal" should "parse correctly" in {

    val goal =
      """
        |type: custom
        |
        |parameters:
        |- on camunda:
        |    - aVariable:
        |        range: 1...100
        |        step: '+1'
        |
        |explore:
        |  camunda:
        |  - aVariable
        |
        |observe:
        |  camunda:
        |  - aMetric
      """.stripMargin.parseYaml.convertTo[Goal]

    val parsedGoal = Goal(
      goalType = Custom,
      params = Vector(
        ApplicationParameterDefinition(
          name = "aVariable",
          serviceName = "camunda",
          dimensionDefinition = Step(
            min = 1d,
            max = 100d,
            step = 1d,
            stepFunction = implicitly[Numeric[Double]].plus
          )
        )
      ),
      explored = Map(
        "camunda" -> Vector(
          "aVariable"
        )
      ),
      observed = Some(Map(
        "camunda" -> List(
          "aMetric"
        )
      ))
    )

    //TODO: eventually improve this test
    goal should have (
      'goalType (Custom),
      'explored (parsedGoal.explored),
      'observed (parsedGoal.observed)
    )

  }

}
