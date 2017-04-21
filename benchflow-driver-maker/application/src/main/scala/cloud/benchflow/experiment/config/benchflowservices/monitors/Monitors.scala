package cloud.benchflow.experiment.config.benchflowservices.monitors

import net.jcazevedo.moultingyaml._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 07/07/16.
  */
case class MonitorAPI(start: Option[String], monitor: String, stop: Option[String])

sealed trait MonitorRunPhase { def toString: String }
case object StartOfLoad extends MonitorRunPhase { override def toString = "start" }
case object EndOfLoad extends MonitorRunPhase { override def toString = "end" }
case object EntireLoad extends MonitorRunPhase { override def toString = "all" }


object MonitorYamlProtocol extends DefaultYamlProtocol {

  implicit object MonitorLifeCycleFormat extends YamlFormat[MonitorRunPhase] {
    override def write(obj: MonitorRunPhase): YamlValue = ???

    override def read(yaml: YamlValue): MonitorRunPhase = {
      yaml.asYamlObject.fields.get(YamlString("phase")).get.convertTo[String] match {
        case "start" => StartOfLoad
        case "end" => EndOfLoad
        case "all" => EntireLoad
        case _ => ???
      }
    }

  }

  implicit object MonitorAPIFormat extends YamlFormat[MonitorAPI] {

    override def write(obj: MonitorAPI): YamlValue = ???

    override def read(yaml: YamlValue): MonitorAPI = {
      val endpoints = yaml.asYamlObject.fields.get(YamlString("endpoints")).get.asYamlObject.fields
      val start = endpoints.get(YamlString("start")).map(_.convertTo[String])
      val monitor = endpoints.get(YamlString("monitor")).map(_.convertTo[String]).get
      val stop = endpoints.get(YamlString("stop")).map(_.convertTo[String])
      //      val privatePort = endpoints.get(YamlString("privatePort")).map(_.convertTo[Int]).get
      MonitorAPI(start, monitor, stop)
    }
  }
}

object MonitorRunPhase {
  def fromYaml(monitorDescriptor: String) = {
    import MonitorYamlProtocol._
    monitorDescriptor.parseYaml.convertTo[MonitorRunPhase]
  }
}

object MonitorAPI {

  def fromYaml(monitorDescriptor: String) = {
    import MonitorYamlProtocol._
    monitorDescriptor.parseYaml.convertTo[MonitorAPI]
  }

}


