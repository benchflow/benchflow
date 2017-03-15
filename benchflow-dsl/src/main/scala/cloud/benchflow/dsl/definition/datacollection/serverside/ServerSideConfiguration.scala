package cloud.benchflow.dsl.definition.datacollection.serverside

import cloud.benchflow.dsl.definition.binding.Binding

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
case class ServerSideConfiguration(configurationMap: Map[String, Seq[Binding]]) {
  def bindings(serviceName: String) = configurationMap.getOrElse(serviceName, Seq())
}