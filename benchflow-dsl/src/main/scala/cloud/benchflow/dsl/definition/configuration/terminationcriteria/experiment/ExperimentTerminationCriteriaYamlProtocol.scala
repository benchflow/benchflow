package cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment

import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlNumber, YamlObject, YamlString, YamlValue}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
object ExperimentTerminationCriteriaYamlProtocol extends DefaultYamlProtocol {

  val typeKey = "type"
  val numberKey = "number"

  implicit object ExperimentTerminationCriteriaYamlFormat extends YamlFormat[Try[ExperimentTerminationCriteria]] {
    override def read(yaml: YamlValue): Try[ExperimentTerminationCriteria] = {

      val yamlObject = yaml.asYamlObject

      for {

        criteriaType <- Try(yamlObject.fields(YamlString(typeKey)).convertTo[String])
        number <- Try(yamlObject.fields(YamlString(numberKey)).convertTo[Int])

      } yield ExperimentTerminationCriteria(criteriaType = criteriaType, number =number)

    }

    override def write(experimentTerminationCriteria: Try[ExperimentTerminationCriteria]): YamlValue = YamlObject(

      // TODO

//      YamlString(typeKey) -> YamlString(experimentTerminationCriteria.criteriaType),
//      YamlString(numberKey) -> YamlNumber(experimentTerminationCriteria.number)

    )
  }

}
