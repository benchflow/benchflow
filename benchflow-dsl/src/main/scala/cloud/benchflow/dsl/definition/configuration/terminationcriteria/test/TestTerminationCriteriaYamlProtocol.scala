package cloud.benchflow.dsl.definition.configuration.terminationcriteria.test

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowTestTerminationCriteriaYamlProtocol
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowTestTerminationCriteriaYamlProtocol.TestKey
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ unsupportedReadOperation, unsupportedWriteOperation }
import cloud.benchflow.dsl.definition.types.time.Time
import cloud.benchflow.dsl.definition.types.time.TimeYamlProtocol._
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
object TestTerminationCriteriaYamlProtocol extends DefaultYamlProtocol {

  val MaxTimeKey = YamlString("max_time")

  val Level = s"${BenchFlowTestTerminationCriteriaYamlProtocol.Level}.${TestKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object TestTerminationCriteriaReadFormat extends YamlFormat[Try[TestTerminationCriteria]] {
    override def read(yaml: YamlValue): Try[TestTerminationCriteria] = {

      val yamlObject = yaml.asYamlObject

      for {

        maxTime <- YamlErrorHandler.deserializationHandler(
          yamlObject.fields(MaxTimeKey).convertTo[Try[Time]].get,
          keyString(MaxTimeKey))

      } yield TestTerminationCriteria(maxTime = maxTime)

    }

    override def write(obj: Try[TestTerminationCriteria]): YamlValue = unsupportedWriteOperation
  }

  implicit object TestTerminationCriteriaWriteFormat extends YamlFormat[TestTerminationCriteria] {

    override def write(obj: TestTerminationCriteria): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        MaxTimeKey -> obj.maxTime.toYaml)

    }

    override def read(yaml: YamlValue): TestTerminationCriteria = unsupportedReadOperation
  }

}
