package cloud.benchflow.test.config.sut.wfms

import cloud.benchflow.test.config._
import cloud.benchflow.test.config.sut.WorkloadModelYamlProtocol
import net.jcazevedo.moultingyaml._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 21/07/16.
  */
trait WfMSSutYamlProtocol extends DefaultYamlProtocol with WorkloadModelYamlProtocol with CommonsYamlProtocol {

  implicit object WfMSOperationYamlFormat extends YamlFormat[WfMSOperation] {
    override def write(wfmsOp: WfMSOperation): YamlValue = {
      wfmsOp.data match {
        case Some(data) => YamlObject(
          YamlString(wfmsOp.name) -> YamlObject(YamlString("data") -> YamlString(data))
        )
        case None => YamlString(wfmsOp.name)
      }
    }

    override def read(yaml: YamlValue): WfMSOperation = {

      yaml match {
        case YamlString(model) => WfMSOperation(name = model, data = None)
        case _ =>
          val fields = yaml.asYamlObject.fields
          val operationName = fields.seq.head._1.convertTo[String]
          val operationBody = fields.seq.headOption.map(_._2.asYamlObject)
          val data = operationBody.flatMap(_.getFields(YamlString("data")).headOption.map(_.convertTo[String]))
          WfMSOperation(name = operationName, data = data)
      }
    }
  }

  implicit object WfMSDriverYamlFormat extends YamlFormat[WfMSDriver] {

    override def write(wfmsDriver: WfMSDriver): YamlValue = {

      val driverName = wfmsDriver match {
        case WfMSStartDriver(_,_,_) => "start"
      }

      YamlObject(
        YamlString(driverName) ->
          YamlObject(
            YamlString("operations") -> wfmsDriver.operations.toYaml,
            YamlString("configuration") -> wfmsDriver.configuration.toYaml,
            YamlString("properties") -> wfmsDriver.properties.toYaml
          )
      )
    }

    override def read(yaml: YamlValue): WfMSDriver = {

      val fields = yaml.asYamlObject.fields
      val driverName = fields.head._1.convertTo[String]
      val driverBody = fields.head._2.asYamlObject

      val driverProperties = driverBody.getFields(YamlString("properties")).headOption match {
        case None => None
        case Some(properties) => Some(YamlObject(YamlString("properties") -> properties).convertTo[Properties])
      }

      val driverOperations = driverBody.getFields(YamlString("operations")).head match {
        case YamlArray(ops) => ops.map(_.convertTo[WfMSOperation])
        case _ => throw new DeserializationException("invalid format; drivers section of benchflow-benchmark.yml has to be a list")
      }

      val driverConfiguration = driverBody.getFields(YamlString("configuration")).headOption match {
        case None => None
        case Some(driverConfig) => Some(driverConfig.convertTo[DriverConfiguration])
      }

      WfMSDriver(t = driverName,
        properties = driverProperties,
        operations = driverOperations,
        configuration = driverConfiguration)
    }
  }



}
