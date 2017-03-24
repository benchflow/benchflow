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

  implicit object SutReadFormat extends YamlFormat[Try[Sut]] {
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

    override def write(obj: Try[Sut]): YamlValue = ???

  }

  implicit object SutWriteFormat extends YamlFormat[Sut] {

    override def write(obj: Sut): YamlValue = YamlObject {

      Map[YamlValue, YamlValue] (
        NameKey -> obj.name.toYaml,
        VersionKey -> obj.version.toString.toYaml,
        TypeKey -> obj.sutType.toString.toYaml,
        ConfigurationKey -> obj.configuration.toYaml
      )

      // TODO - add service configuration

    }

    override def read(yaml: YamlValue): Sut = ???
  }

}

