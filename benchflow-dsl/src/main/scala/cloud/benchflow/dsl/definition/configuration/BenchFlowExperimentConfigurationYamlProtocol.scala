package cloud.benchflow.dsl.definition.configuration

import cloud.benchflow.dsl.definition.configuration.BenchFlowTestConfigurationYamlProtocol.{ TerminationCriteriaKey, UsersKey, WorkloadExecutionKey }
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowExperimentTerminationCriteria
import cloud.benchflow.dsl.definition.configuration.terminationcriteria.BenchFlowExperimentTerminationCriteriaYamlProtocol._
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecution
import cloud.benchflow.dsl.definition.configuration.workloadexecution.WorkloadExecutionYamlProtocol._
import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler._
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _ }

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 10.03.17.
 */
object BenchFlowExperimentConfigurationYamlProtocol extends DefaultYamlProtocol {

  // default value since deserializing from a test definition it might not be defined
  // before use it will be overridden
  val defaultUsers = 0

  private def keyString(key: YamlString) = s"${BenchFlowTestConfigurationYamlProtocol.Level}.${key.value}"

  implicit object ExperimentConfigurationReadFormat extends YamlFormat[Try[BenchFlowExperimentConfiguration]] {

    override def read(yaml: YamlValue): Try[BenchFlowExperimentConfiguration] = {

      val yamlObject = yaml.asYamlObject

      for {

        users <- deserializationHandler(
          yamlObject.getFields(UsersKey).headOption match {
            case Some(usersKey) => usersKey.convertTo[Int]
            case None => defaultUsers
          },
          keyString(UsersKey))

        workloadExecution <- deserializationHandler(
          yamlObject.fields(WorkloadExecutionKey).convertTo[Try[WorkloadExecution]].get,
          keyString(WorkloadExecutionKey))

        terminationCriteria <- deserializationHandler(
          yamlObject.fields(TerminationCriteriaKey).convertTo[Try[BenchFlowExperimentTerminationCriteria]].get,
          keyString(TerminationCriteriaKey))

      } yield BenchFlowExperimentConfiguration(
        users = users,
        workloadExecution = workloadExecution,
        terminationCriteria = terminationCriteria)

    }

    override def write(configuration: Try[BenchFlowExperimentConfiguration]): YamlValue = unsupportedWriteOperation

  }

  implicit object ExperimentConfigurationWriteFormat extends YamlFormat[BenchFlowExperimentConfiguration] {

    override def write(obj: BenchFlowExperimentConfiguration): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        UsersKey -> obj.users.toYaml,
        WorkloadExecutionKey -> obj.workloadExecution.toYaml,
        TerminationCriteriaKey -> obj.terminationCriteria.toYaml)

    }

    override def read(yaml: YamlValue): BenchFlowExperimentConfiguration = unsupportedReadOperation
  }

}
