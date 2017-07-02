package cloud.benchflow.dsl.definition.configuration.goal.explorationspace

import cloud.benchflow.dsl.definition.configuration.goal.GoalYamlProtocol
import cloud.benchflow.dsl.definition.configuration.goal.GoalYamlProtocol.ExplorationSpaceKey
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.service.ServiceExplorationSpace
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.service.ServiceExplorationSpaceYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.WorkloadExplorationSpace
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.WorkloadExplorationSpaceYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue, _}

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-18
 */
object ExplorationSpaceYamlProtocol extends DefaultYamlProtocol {

  val WorkloadKey = YamlString("workload")

  val Level = s"${GoalYamlProtocol.Level}.${ExplorationSpaceKey.value}"

  private def keyString(key: YamlString) = s"$Level.${key.value}"

  implicit object ExplorationSpaceReadFormat extends YamlFormat[Try[ExplorationSpace]] {

    override def read(yaml: YamlValue): Try[ExplorationSpace] = {

      val yamlObject = yaml.asYamlObject

      for {

        workload <- deserializationHandler(
          yamlObject.getFields(WorkloadKey).headOption.map(_.convertTo[Try[WorkloadExplorationSpace]].get),
          keyString(WorkloadKey))

        services <- deserializationHandler(
          Option(
            yamlObject.fields.filterNot(value => value._1.equals(WorkloadKey))
              .map(value => (value._1.convertTo[String], value._2.convertTo[Try[ServiceExplorationSpace]].get))),
          keyString(YamlString("(some service)")))

      } yield ExplorationSpace(
        workload = workload,
        services = services)

    }

    override def write(obj: Try[ExplorationSpace]): YamlValue = unsupportedWriteOperation
  }

  implicit object ExplorationSpaceWriteFormat extends YamlFormat[ExplorationSpace] {

    override def write(obj: ExplorationSpace): YamlValue = {

      val yamlObject = Map[YamlValue, YamlValue]() ++
        obj.workload.map(key => WorkloadKey -> key.toYaml)

      val serviceObject = obj.services.head.map(entry => entry._1.toYaml -> entry._2.toYaml)

      YamlObject(yamlObject ++ serviceObject)

    }

    override def read(yaml: YamlValue): ExplorationSpace = unsupportedReadOperation
  }

}
