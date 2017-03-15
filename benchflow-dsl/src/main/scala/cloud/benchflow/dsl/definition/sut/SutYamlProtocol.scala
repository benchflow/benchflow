package cloud.benchflow.dsl.definition.sut

import cloud.benchflow.dsl.definition.sut.configuration.SutConfiguration
import cloud.benchflow.dsl.definition.sut.configuration.ConfigurationYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 10.03.17.
  */
object SutYamlProtocol extends DefaultYamlProtocol {

  // TODO - implement me
  val NameKey = YamlString("name")
  val VersionKey = YamlString("version")
  val TypeKey = YamlString("type")
  val ConfigurationKey = YamlString("configuration")

  implicit object SutYamlFormat extends YamlFormat[Try[Sut]] {
    override def read(yaml: YamlValue): Try[Sut] = {

      val yamlObject = yaml.asYamlObject

      for {

        name <- Try(yamlObject.fields(NameKey).convertTo[String])
        version <- Try(yamlObject.fields(VersionKey).convertTo[String]) // TODO - validate version? see class Version
        sutType <- Try(yaml.asYamlObject.fields(TypeKey).convertTo[String]) // TODO - validate SutType? see class SutType
        configuration <- yamlObject.fields(ConfigurationKey).convertTo[Try[SutConfiguration]]

        serviceConfiguration <- Try(Option(None)) // TODO

      } yield Sut(name = name, version = version, sutType = sutType, configuration = configuration, serviceConfiguration = serviceConfiguration)

    }

    // TODO - implement me
    override def write(obj: Try[Sut]): YamlValue = ???
  }

}

//implicit object SutYamlFormat extends YamlFormat[Sut] {
//  override def write(sut: Sut): YamlValue = {
//    YamlObject(
//      YamlString("name") -> YamlString(sut.name),
//      YamlString("version") -> YamlString(sut.version.toString),
//      YamlString("type") -> YamlString(sut.sutType match {
//        case WfMS => "WfMS"
//        case Http => "http"
//      })
//    )
//  }
//
//  override def read(yaml: YamlValue): Sut = {
//    val sutName = yaml.asYamlObject.fields.get(YamlString("name")) match {
//      case Some(YamlString(name)) => name
//      case _ => throw new DeserializationException("No name specified in sut definition")
//    }
//
//    val version = yaml.asYamlObject.fields.get(YamlString("version")) match {
//      case Some(YamlString(v)) => Version(v)
//      case _ => throw new DeserializationException("No version specified in sut definition")
//    }
//
//    val sutsType = yaml.asYamlObject.fields.get(YamlString("type")) match {
//      case Some(YamlString(t)) => SutType(t)
//      case _ => throw new DeserializationException("No type specified in sut definition")
//    }
//
//    Sut(sutName, version, sutsType)
//  }
//}
