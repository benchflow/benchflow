package cloud.benchflow.dsl.definition.configuration.workloadexecution

import cloud.benchflow.dsl.definition.time.Time
import cloud.benchflow.dsl.definition.time.TimeYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue, _}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
object WorkloadExecutionYamlProtocol extends DefaultYamlProtocol {

  val RampUpKey = "ramp_up"
  val SteadyStateKey = "steady_state"
  val RampDownKey = "ramp_down"

  implicit object WorkloadExecutionYamlFormat extends YamlFormat[Try[WorkloadExecution]] {
    override def read(yaml: YamlValue): Try[WorkloadExecution] = {

      val yamlObject = yaml.asYamlObject

      for {

        rampUp <- yamlObject.fields(YamlString(RampUpKey)).convertTo[Try[Time]]
        steadyState <- yamlObject.fields(YamlString(SteadyStateKey)).convertTo[Try[Time]]
        rampDown <- yamlObject.fields(YamlString(RampDownKey)).convertTo[Try[Time]]

      } yield WorkloadExecution(rampUp = rampUp, steadyState = steadyState, rampDown = rampDown)

    }

    override def write(workloadExecution: Try[WorkloadExecution]): YamlValue = YamlObject(


      // TODO
//      YamlString(RampUpKey) -> YamlString(workloadExecution.rampUp),
//      YamlString(SteadyStateKey) -> YamlString(workloadExecution.steadyState),
//      YamlString(RampDownKey) -> YamlString(workloadExecution.rampDown)

    )
  }

}
