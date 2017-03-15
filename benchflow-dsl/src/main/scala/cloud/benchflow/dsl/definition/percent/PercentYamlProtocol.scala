package cloud.benchflow.dsl.definition.percent

import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue}

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 14.03.17.
  */
object PercentYamlProtocol extends DefaultYamlProtocol {

  implicit object PercentFormat extends YamlFormat[Try[Percent]] {

    override def read(yaml: YamlValue): Try[Percent] = {

      val stringValue: Try[String] = Try(yaml.asYamlObject.getFields().head.convertTo[String])

      stringValue.map(string => Percent(string.replace("%", "").toDouble / 100))

    }

    override def write(obj: Try[Percent]): YamlValue = YamlString(obj.toString)

  }

}
