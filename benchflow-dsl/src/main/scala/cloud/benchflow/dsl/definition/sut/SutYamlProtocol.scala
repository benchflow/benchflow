package cloud.benchflow.dsl.definition.sut

import cloud.benchflow.dsl.definition.errorhandling.YamlErrorHandler.deserializationHandler
import cloud.benchflow.dsl.definition.sut.configuration.SutConfiguration
import cloud.benchflow.dsl.definition.sut.configuration.SutConfigurationYamlProtocol._
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 10.03.17.
  */
object SutYamlProtocol extends DefaultYamlProtocol {

  val NameKey = YamlString("name")
  val VersionKey = YamlString("version")
  val TypeKey = YamlString("type")
  val ConfigurationKey = YamlString("configuration")

  private def keyString(key: YamlString) = "sut." + key.value

  implicit object SutYamlFormat extends YamlFormat[Try[Sut]] {
    override def read(yaml: YamlValue): Try[Sut] = {

      val yamlObject = yaml.asYamlObject

      for {

        name <- deserializationHandler(
          yamlObject.fields(NameKey).convertTo[String],
          keyString(NameKey)
        )

        version <- deserializationHandler(
          Version(yamlObject.fields(VersionKey).convertTo[String]),
          keyString(VersionKey)
        )

        sutType <- deserializationHandler(
          SutType(yamlObject.fields(TypeKey).convertTo[String]),
          keyString(VersionKey)
        )

        configuration <- deserializationHandler(
          yamlObject.fields(ConfigurationKey).convertTo[Try[SutConfiguration]].get,
          keyString(ConfigurationKey)
        )

        // TODO - specify
        serviceConfiguration <- Try(Option(None))

      } yield Sut(name = name,
        version = version,
        sutType = sutType,
        configuration = configuration,
        serviceConfiguration = serviceConfiguration
      )

    }

    override def write(obj: Try[Sut]): YamlValue = {

      val sut = obj.get

      val map = Map[YamlValue, YamlValue] (
        NameKey -> YamlString(sut.name),
        VersionKey -> YamlString(sut.version.toString),
        TypeKey -> YamlString(sut.sutType.toString),
        ConfigurationKey -> Try(sut.configuration).toYaml
      )
      
      // TODO - add service configuration

      YamlObject(map)
    }
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
