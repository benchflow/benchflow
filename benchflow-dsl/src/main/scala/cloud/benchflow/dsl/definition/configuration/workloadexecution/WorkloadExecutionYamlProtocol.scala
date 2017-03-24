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

  implicit object WorkloadExecutionReadFormat extends YamlFormat[Try[WorkloadExecution]] {
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

    override def write(workloadExecutionTry: Try[WorkloadExecution]): YamlValue = ???
  }

  implicit object WorkloadExecutionWriteFormat extends YamlFormat[WorkloadExecution] {

    override def write(obj: WorkloadExecution): YamlValue = YamlObject {

      Map[YamlValue, YamlValue](
        RampUpKey -> obj.rampUp.toYaml,
        SteadyStateKey -> obj.steadyState.toYaml,
        RampDownKey -> obj.rampDown.toYaml
      )

    }

    override def read(yaml: YamlValue): WorkloadExecution = ???
  }

}
