package cloud.benchflow.dsl.definition.simone.binding

import cloud.benchflow.dsl.definition.simone.properties.Properties
import cloud.benchflow.dsl.definition.simone.properties.PropertiesYamlProtocol._
import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, YamlFormat, YamlObject, YamlString, YamlValue, _ }

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 13.03.17.
 */
object BindingYamlProtocol extends DefaultYamlProtocol {

  implicit object BindingYamlFormat extends YamlFormat[Binding] {
    override def write(binding: Binding): YamlValue = {

      binding.config match {
        case Some(config) => YamlObject(
          YamlString(binding.boundService) -> config.toYaml)
        case None => YamlString(binding.boundService)
      }

    }

    override def read(yaml: YamlValue): Binding = {

      def readYamlWithConfig(yaml: YamlValue): Binding = {
        val binding = yaml.asYamlObject.fields.head
        val bfService = binding._1.convertTo[String]
        val props = binding._2.asYamlObject.getFields(YamlString("config")) match {
          case Seq(YamlObject(obj)) =>
            Some(YamlObject(YamlString("properties") -> YamlObject(obj)).convertTo[Properties])
          case _ => None
        }
        Binding(bfService, props)
      }

      yaml match {
        case YamlString(boundName) => Binding(boundName, None)
        case other => readYamlWithConfig(other)
      }

    }

  }

}
