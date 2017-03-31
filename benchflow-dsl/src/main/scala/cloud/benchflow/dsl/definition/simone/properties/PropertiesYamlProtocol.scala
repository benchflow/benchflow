package cloud.benchflow.dsl.definition.simone.properties

import net.jcazevedo.moultingyaml.{ DefaultYamlProtocol, DeserializationException, YamlArray, YamlBoolean, YamlDate, YamlFormat, YamlNumber, YamlObject, YamlString, YamlValue, _ }

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 14.03.17.
 */
object PropertiesYamlProtocol extends DefaultYamlProtocol {

  implicit object PropertiesYamlFormat extends YamlFormat[Properties] {

    override def write(props: Properties): YamlValue = {

      def convertSecond(s: Any): YamlValue = {
        s match {
          case str: String => YamlString(str)
          //TODO: add more numeric types?
          case int: Int => YamlNumber(int)
          case double: Double => YamlNumber(double)
          case lst: List[String] => lst.toYaml
          case map: Map[String, Any] => convert(map)
        }
      }

      def convertOne(t: (String, Any)): (YamlValue, YamlValue) = {
        YamlString(t._1) -> convertSecond(t._2)
      }

      def convert(p: Map[String, Any]) = {
        YamlObject(p.map(convertOne))
      }

      convert(props.properties)
    }

    private def toScalaPair(pair: (YamlValue, YamlValue)): (String, Any) = {
      val first = pair._1.convertTo[String]

      def convertValue(value: YamlValue): Any = value match {
        case YamlString(s) => s
        case YamlBoolean(bool) => bool
        case YamlDate(date) => date
        case YamlNumber(num) => num
        case YamlObject(map) => map.seq.map(toScalaPair)
        case YamlArray(values) => values.map(convertValue) //values.toList.map(value => value.convertTo[String])
        case _ => throw DeserializationException("Unexpected format for field properties")
      }

      (first, convertValue(pair._2))

    }

    override def read(yaml: YamlValue): Properties = {
      val properties = yaml.asYamlObject.fields.head
      properties match {
        case (YamlString("properties"), YamlObject(props)) =>
          Properties(YamlObject(props).fields.map(toScalaPair))
        case _ => throw DeserializationException("Unexpected format for field properties")
      }
    }
  }

}
