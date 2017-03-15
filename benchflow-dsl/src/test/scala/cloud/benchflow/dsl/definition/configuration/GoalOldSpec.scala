package cloud.benchflow.dsl.definition.configuration

import cloud.benchflow.dsl.definition.Custom
import cloud.benchflow.dsl.definition.configuration.simone.{ApplicationParameterDefinition, GoalOld, GoalOldYamlProtocol, Step}
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 20/07/16.
  */
class GoalOldSpec extends FlatSpec with Matchers with GoalOldYamlProtocol {

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
      """.stripMargin.parseYaml.convertTo[GoalOld]

    val parsedGoal = GoalOld(
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
