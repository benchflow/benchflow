package cloud.benchflow.dsl.definition.configuration.terminationcriteria.test

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler
import cloud.benchflow.dsl.definition.types.time.Time
import cloud.benchflow.dsl.definition.types.time.TimeYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue, _}

import scala.util.{Failure, Try}

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
object TestTerminationCriteriaYamlProtocol extends DefaultYamlProtocol {

  val MaxTimeKey = YamlString("max_time")

  def testTerminationCriteriaFailure(key: String, e: Exception) = Failure(new Exception(TestTerminationCriteriaYamlProtocol.getClass.getSimpleName + key + ": " + e))

  private def keyString(key: YamlString) = "configuration.termination_criteria.test" + key.value

  implicit object TestTerminationCriteriaYamlFormat extends YamlFormat[Try[TestTerminationCriteria]] {
    override def read(yaml: YamlValue): Try[TestTerminationCriteria] = {

      val yamlObject = yaml.asYamlObject

      for {

        maxTime <- YamlErrorHandler.deserializationHandler(
          yamlObject.fields(MaxTimeKey).convertTo[Try[Time]].get,
          keyString(MaxTimeKey)
        )

      } yield TestTerminationCriteria(maxTime = maxTime)

    }

    override def write(obj: Try[TestTerminationCriteria]): YamlValue = {

      val testTerminationCriteria = obj.get

      val map = Map[YamlValue, YamlValue](
        MaxTimeKey -> Try(testTerminationCriteria.maxTime).toYaml
      )

      YamlObject(map)

    }
  }

}
