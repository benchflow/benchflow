package cloud.benchflow.dsl.definition.configuration.workloadexecution

import cloud.benchflow.dsl.definition.time.Time

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
// TODO - define type
case class WorkloadExecution(rampUp: Time,
                             steadyState: Time,
                             rampDown: Time)
