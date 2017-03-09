package cloud.benchflow.test.config.test

import net.jcazevedo.moultingyaml._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 20/07/16.
  */
trait GoalYamlProtocol extends DefaultYamlProtocol with ParameterDefinitionYamlProtocol {

  implicit object GoalYamlFormat extends YamlFormat[Goal] {

    private def readServiceParameterDefinitions(yaml: YamlValue): Seq[ServiceParameterDefinition[_]] = {

      val definition = yaml.asYamlObject.fields.head
      val serviceName = definition._1.convertTo[String] match {
        case onRegex(sName) => sName
      }
      val parameterDefinitions = definition._2

      parameterDefinitions match {
        case YamlArray(defs) => for {
          aDef <- defs
        }  yield ServiceParameterDefinitionYamlFormat.read(
          YamlObject(YamlString(serviceName) -> aDef)
        )
        case _ => throw new Exception("Unexpected format for service parameter definitions")
      }

    }

    val onRegex = "on\\s(.+)".r

    override def read(yaml: YamlValue): Goal = {

      val goalType = GoalType(yaml.asYamlObject.fields.get(YamlString("type")).get.convertTo[String])

      val parameterDefinitions = yaml.asYamlObject.fields.get(YamlString("parameters")).get

      val parsedDefinitions = parameterDefinitions match {
        case YamlArray(defs) => defs flatMap readServiceParameterDefinitions
        case _ => ???
      }

      val toExplore = yaml.asYamlObject.fields.get(YamlString("explore")).get.convertTo[Map[String, Seq[String]]]
      val toObserve = yaml.asYamlObject.fields.get(YamlString("observe")).map(_.convertTo[Map[String, Seq[String]]])


      Goal(
        goalType = goalType,
        params = parsedDefinitions,
        explored = toExplore,
        observed = toObserve
      )

    }

    override def write(obj: Goal): YamlValue = ???
  }

}
