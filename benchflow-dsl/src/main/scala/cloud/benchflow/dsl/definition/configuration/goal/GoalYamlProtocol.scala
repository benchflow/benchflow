package cloud.benchflow.dsl.definition.configuration.goal

import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
object GoalYamlProtocol extends DefaultYamlProtocol {

  val TypeKey = "type"
  val ObservationKey = "observation"
  val ExplorationSpaceKey = "exploration_space"

  implicit object GoalYamlFormat extends YamlFormat[Try[Goal]] {
    override def read(yaml: YamlValue): Try[Goal] = {

      val yamlObject = yaml.asYamlObject

      for {

        goalType          <- Try(yamlObject.fields(YamlString(TypeKey)).convertTo[String])
        observation       <- Try(Option(None)) // TODO - define
        explorationSpace  <- Try(Option(None)) // TODO - define

      } yield Goal(goalType = goalType, observation = observation, explorationSpace = explorationSpace)


    }

    override def write(goal: Try[Goal]): YamlValue = YamlObject (

      // TODO

//      YamlString(typeKey) -> YamlString(goal.goalType)

    )
  }

}
