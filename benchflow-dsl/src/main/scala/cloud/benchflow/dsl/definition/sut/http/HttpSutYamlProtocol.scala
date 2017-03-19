package cloud.benchflow.dsl.definition.sut.http

import cloud.benchflow.dsl.definition._
import cloud.benchflow.dsl.definition.sut.CommonsYamlProtocol
import cloud.benchflow.dsl.definition.workload.WorkloadModelYamlProtocol
import net.jcazevedo.moultingyaml._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 21/07/16.
  */
trait HttpSutYamlProtocol extends DefaultYamlProtocol with WorkloadModelYamlProtocol with CommonsYamlProtocol {

  implicit object HttpOperationYamlFormat extends YamlFormat[HttpOperation] {

    override def write(httpOp: HttpOperation): YamlValue = {
      YamlObject(
        YamlString(httpOp.name) -> {
          YamlObject(
            YamlString("endpoint") -> YamlString(httpOp.endpoint),
            YamlString("method") -> YamlString(httpOp.method.toString),
            YamlString("headers") -> httpOp.headers.toYaml,
            YamlString("data") -> httpOp.data.toYaml
          )
        }
      )
    }

    override def read(yaml: YamlValue): HttpOperation = {

      val fields = yaml.asYamlObject.fields
      val operationName = fields.seq.head._1.convertTo[String]
      val operationBody = fields.seq.head._2.asYamlObject
      val method = HttpMethod(operationBody.getFields(YamlString("method")).map(_.convertTo[String]).head)
      val headersMap = operationBody.getFields(YamlString("headers")).headOption match {
        case None => Map[String, String]()
        case Some(YamlObject(headers)) => YamlObject(headers).convertTo[Map[String, String]]
        case _ => throw new DeserializationException("Invalid format for headers in operation " + operationName)
      }
      val data = method match {
        case Post | Put => operationBody.getFields(YamlString("data")).headOption.map(_.convertTo[String])
        case _ => None //force no data for requests that don't have a body
      }
      val endpoint = operationBody.getFields(YamlString("endpoint")).head.convertTo[String]

      HttpOperation(name = operationName,
        endpoint = endpoint,
        method = method,
        headers = headersMap,
        data = data)
    }
  }

  //TODO: figure out how to make drivers yaml format generic (may not be possible)
  implicit object HttpDriverYamlFormat extends YamlFormat[HttpDriver] {
    override def write(httpDriver: HttpDriver): YamlValue = {
      YamlObject(
        YamlString("http") ->
          YamlObject(
            YamlString("operations") -> httpDriver.operations.toYaml,
            YamlString("configuration") -> httpDriver.configuration.toYaml,
            YamlString("properties") -> httpDriver.properties.toYaml
          )
      )
    }

    override def read(yaml: YamlValue): HttpDriver = {

      val fields = yaml.asYamlObject.fields
      //val driverName = fields.head._1.convertTo[String]
      val driverBody = fields.head._2.asYamlObject

      val driverProperties = driverBody.getFields(YamlString("properties")).headOption match {
        case None => None
        case Some(properties) => Some(YamlObject(YamlString("properties") -> properties).convertTo[Properties])
      }

      val driverOperations = driverBody.getFields(YamlString("operations")).head match {
        case YamlArray(ops) => ops.map(_.convertTo[HttpOperation])
        case _ => throw new DeserializationException("invalid format; drivers section of benchflow-benchmark.yml has to be a list")
      }

      val driverConfiguration = driverBody.getFields(YamlString("configuration")).headOption match {
        case None => None
        case Some(driverConfig) => Some(driverConfig.convertTo[DriverConfiguration])
      }

      HttpDriver(properties = driverProperties,
        operations = driverOperations,
        configuration = driverConfiguration)
    }
  }


}