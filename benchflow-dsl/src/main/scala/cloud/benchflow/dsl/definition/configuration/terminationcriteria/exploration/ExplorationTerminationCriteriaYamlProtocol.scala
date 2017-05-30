package cloud.benchflow.dsl.definition.configuration.terminationcriteria.exploration

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowTestTerminationCriteriaYamlProtocol
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowTestTerminationCriteriaYamlProtocol.ExplorationKey
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.exploration.explorationtype.ExplorationType.ExplorationType
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.exploration.explorationtype.ExplorationTypeYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.{ deserializationHandler, unsupportedReadOperation, unsupportedWriteOperation }
import cloud.benchflow.dsl.definition.types.percent.Percent
import cloud.benchflow.dsl.definition.types.percent.PercentYamlProtocol._
import cloud.benchflow.dsl.definition.types.time.Time
import cloud.benchflow.dsl.definition.types.time.TimeYamlProtocol._
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlArray, YamlFormat, YamlObject, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-22
 */
object ExplorationTerminationCriteriaYamlProtocol extends DefaultYamlProtocol {

  val TypeKey = YamlString("type")
  val NumberKey = YamlString("number")
  val MaxTimeKey = YamlString("max_time")
  val MeanRelativeErrorKey = YamlString("mean_relative_error")
  val MaxFailedKey = YamlString("max_failed")

  val Level = s"${BenchFlowTestTerminationCriteriaYamlProtocol.Level}.${ExplorationKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object ExplorationTerminationCriteriaReadFormat extends YamlFormat[Try[ExplorationTerminationCriteria]] {

    override def read(yaml: YamlValue): Try[ExplorationTerminationCriteria] = {

      val yamlObject = yaml.asYamlObject

      for {

        explorationType <- deserializationHandler(
          yamlObject.fields(TypeKey) match {
            case list: YamlArray => list.convertTo[List[Try[ExplorationType]]].map(_.get)
            case string: YamlString => List(string.convertTo[Try[ExplorationType]].get)
          },
          keyString(TypeKey))

        number <- deserializationHandler(
          yamlObject.getFields(NumberKey).headOption.map(_.convertTo[Int]),
          keyString(NumberKey))

        maxTime <- deserializationHandler(
          yamlObject.getFields(MaxTimeKey).headOption.map(_.convertTo[Try[Time]].get),
          keyString(MaxTimeKey))

        meanRelativeError <- deserializationHandler(
          yamlObject.fields(MeanRelativeErrorKey).convertTo[Try[Percent]].get,
          keyString(MeanRelativeErrorKey))

        maxFailed <- deserializationHandler(
          yamlObject.getFields(MaxFailedKey).headOption.map(_.convertTo[Try[Percent]].get),
          keyString(MaxFailedKey))

      } yield ExplorationTerminationCriteria(
        explorationType = explorationType,
        number = number,
        maxTime = maxTime,
        meanRelativeError = meanRelativeError,
        maxFailed = maxFailed)

    }

    override def write(obj: Try[ExplorationTerminationCriteria]): YamlValue = unsupportedWriteOperation
  }

  implicit object ExplorationTerminationCriteriaWriteFormat extends YamlFormat[ExplorationTerminationCriteria] {

    override def write(obj: ExplorationTerminationCriteria): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        TypeKey -> obj.explorationType.toYaml) ++
        obj.number.map(key => NumberKey -> key.toYaml) ++
        obj.maxTime.map(key => MaxTimeKey -> key.toYaml) +
        (MeanRelativeErrorKey -> obj.meanRelativeError.toYaml) ++
        obj.maxFailed.map(key => MaxFailedKey -> key.toYaml)

    }

    override def read(yaml: YamlValue): ExplorationTerminationCriteria = unsupportedReadOperation
  }

}
