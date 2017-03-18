package cloud.benchflow.dsl.definition.datacollection.clientside.faban

import cloud.benchflow.dsl.definition.types.time.Time

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 12.03.17.
  */
case class FabanConfiguration(maxRunTime: Option[Time],
                              interval: Option[Time],
                              workload: Option[Map[String, Time]])
