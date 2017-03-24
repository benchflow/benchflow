package cloud.benchflow.dsl.definition.configuration.goal

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
object GoalYamlProtocol extends DefaultYamlProtocol {

  val TypeKey = YamlString("type")
  val ObservationKey = YamlString("observation")
  val ExplorationSpaceKey = YamlString("exploration_space")

  private def keyString(key: YamlString) = "configuration.goal." + key.value

  implicit object GoalYamlReadFormat extends YamlFormat[Try[Goal]] {

    override def read(yaml: YamlValue): Try[Goal] = {

      val yamlObject = yaml.asYamlObject

      for {

        goalType <- deserializationHandler(
          yamlObject.fields(TypeKey).convertTo[String],
          keyString(TypeKey)
        )

        observation <- Try(Option(None)) // TODO - define
        explorationSpace <- Try(Option(None)) // TODO - define

      } yield Goal(
        goalType = goalType,
        observation = observation,
        explorationSpace = explorationSpace
      )

    }

    override def write(goalTry: Try[Goal]): YamlValue = unsupportedWriteOperation
  }

  implicit object GoalYamlWriteFormat extends YamlFormat[Goal] {

    override def write(obj: Goal): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        TypeKey -> obj.goalType.toYaml
      )

      // TODO - add observation, explorationSpace

    }

    override def read(yaml: YamlValue): Goal = unsupportedReadOperation

  }

}