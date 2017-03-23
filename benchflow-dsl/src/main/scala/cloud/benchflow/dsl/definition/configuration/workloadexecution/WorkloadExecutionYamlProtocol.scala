package cloud.benchflow.dsl.definition.configuration.workloadexecution

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler._
import cloud.benchflow.dsl.definition.types.time.Time
import cloud.benchflow.dsl.definition.types.time.TimeYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue, _}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
object WorkloadExecutionYamlProtocol extends DefaultYamlProtocol {

  val RampUpKey = YamlString("ramp_up")
  val SteadyStateKey = YamlString("steady_state")
  val RampDownKey = YamlString("ramp_down")

  private def keyString(key: YamlString) = "configuration.workload_execution" + key.value

  implicit object WorkloadExecutionYamlFormat extends YamlFormat[Try[WorkloadExecution]] {
    override def read(yaml: YamlValue): Try[WorkloadExecution] = {

      val yamlObject = yaml.asYamlObject

      for {

        rampUp <- deserializationHandler(
          yamlObject.fields(RampUpKey).convertTo[Try[Time]].get,
          keyString(RampUpKey)
        )

        steadyState <- deserializationHandler(
          yamlObject.fields(SteadyStateKey).convertTo[Try[Time]].get,
          keyString(SteadyStateKey)
        )

        rampDown <- deserializationHandler(
          yamlObject.fields(RampDownKey).convertTo[Try[Time]].get,
          keyString(RampDownKey)
        )

      } yield WorkloadExecution(rampUp = rampUp, steadyState = steadyState, rampDown = rampDown)

    }

    override def write(workloadExecutionTry: Try[WorkloadExecution]): YamlValue = {

      val workloadExecution = workloadExecutionTry.get

      val map = Map[YamlValue, YamlValue](
        RampUpKey -> Try(workloadExecution.rampUp).toYaml,
        SteadyStateKey -> Try(workloadExecution.steadyState).toYaml,
        RampDownKey -> Try(workloadExecution.rampDown).toYaml
      )

      YamlObject(map)

    }
  }

}
