package cloud.benchflow.dsl.definition.datacollection.clientside.faban

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 12.03.17.
  */
// TODO - adjust this
case class FabanConfiguration(maxRunTime: String, interval: String, intervalWorkload: Map[String, String])
