package cloud.benchflow.dsl.definition.configuration.terminationcriteria

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.ExperimentTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.ExperimentTerminationCriteriaYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.test.TestTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.test.TestTerminationCriteriaYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
object TerminationCriteriaYamlProtocol extends DefaultYamlProtocol {

  // TODO - implement me
  val TestKey = "test"
  val ExperimentKey = "experiment"

  implicit object TerminationCriteriaYamlFormat extends YamlFormat[Try[TerminationCriteria]] {
    override def read(yaml: YamlValue): Try[TerminationCriteria] =  {

      val yamlObject = yaml.asYamlObject

      for {

        test <- yamlObject.fields(YamlString(TestKey)).convertTo[Try[TestTerminationCriteria]]
        experiment <- yamlObject.fields(YamlString(ExperimentKey)).convertTo[Try[ExperimentTerminationCriteria]]

      } yield TerminationCriteria(test = test, experiment = experiment)

    }

    override def write(terminationCriteria: Try[TerminationCriteria]): YamlValue = YamlObject(

      // TODO

//      YamlString(TestKey) -> terminationCriteria.test.toYaml,
//      YamlString(ExperimentKey) -> terminationCriteria.experiment.toYaml

    )
  }

}
