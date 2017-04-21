package cloud.benchflow.experiment.config.benchflowservices.collectors

import net.jcazevedo.moultingyaml._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 24/02/16.
  */
case class CollectorAPI(start: Option[String], stop: String)
case class CollectorDependencies(monitors: Seq[String])

object CollectorYamlProtocol extends DefaultYamlProtocol {

  implicit object CollectorDependenciesYamlFormat extends YamlFormat[CollectorDependencies] {
    override def write(obj: CollectorDependencies): YamlValue = ???

    override def read(yaml: YamlValue): CollectorDependencies = {
      yaml.asYamlObject.fields.get(YamlString("dependencies")).flatMap {
        _.asYamlObject.fields.get(YamlString("monitors")).map {
          _ match {
            case YamlArray(monitors) => CollectorDependencies(monitors.map(_.convertTo[String]))
            case _ => CollectorDependencies(Seq.empty)
          }
        }
      }
    }.getOrElse(CollectorDependencies(Seq.empty))

  }

  implicit object CollectorAPIYamlFormat extends YamlFormat[CollectorAPI] {
    override def write(obj: CollectorAPI): YamlValue = ???

    override def read(yaml: YamlValue): CollectorAPI = {
      val endpoints = yaml.asYamlObject.fields.get(YamlString("endpoints")).get.asYamlObject.fields
      val start = endpoints.get(YamlString("start")).map(_.convertTo[String])
      val stop = endpoints.get(YamlString("stop")).map(_.convertTo[String]).get
      CollectorAPI(start, stop)
    }
  }

}

object CollectorAPI {
  def fromYaml(yaml: String) = {
    import CollectorYamlProtocol._
    yaml.stripMargin.parseYaml.convertTo[CollectorAPI]
  }
}
object CollectorDependencies {
  def fromYaml(yaml: String) = {
    import CollectorYamlProtocol._
    yaml.stripMargin.parseYaml.convertTo[CollectorDependencies]
  }
}
