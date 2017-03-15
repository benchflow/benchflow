package cloud.benchflow.dsl.definition.configuration.terminationcriteria.test

import cloud.benchflow.dsl.definition.time.Time
import cloud.benchflow.dsl.definition.time.TimeYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
object TestTerminationCriteriaYamlProtocol extends DefaultYamlProtocol {

  val MaxTimeKey = "max_time"

  implicit object TestTerminationCriteriaYamlFormat extends YamlFormat[Try[TestTerminationCriteria]] {
    override def read(yaml: YamlValue): Try[TestTerminationCriteria] = {

      val yamlObject = yaml.asYamlObject

      for {

        maxTime <- yamlObject.fields(YamlString(MaxTimeKey)).convertTo[Try[Time]]

      } yield TestTerminationCriteria(maxTime = maxTime)

    }

    override def write(obj: Try[TestTerminationCriteria]): YamlValue = ???
  }

}
