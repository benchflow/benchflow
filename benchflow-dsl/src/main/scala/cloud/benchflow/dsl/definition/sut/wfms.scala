package cloud.benchflow.dsl.definition.sut

import cloud.benchflow.dsl.definition.simone.properties.Properties
import cloud.benchflow.dsl.definition.simone.{Driver, DriverConfiguration, Operation}

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-03-18
 */
package object wfms {

  case object WfMS extends SutType {
    override def toString: String = "wfms"
  }

  case class WfMSOperation(override val name: String, override val data: Option[String]) extends Operation(name, data)

  sealed abstract class WfMSDriver(
    properties: Option[Properties],
    operations: Seq[WfMSOperation],
    configuration: Option[DriverConfiguration]
  )
      extends Driver[WfMSOperation](properties, operations, configuration)

  case class WfMSStartDriver(
    override val properties: Option[Properties],
    override val operations: Seq[WfMSOperation],
    override val configuration: Option[DriverConfiguration]
  )
      extends WfMSDriver(properties, operations, configuration)

  object WfMSDriver {
    def apply(
      t: String,
      properties: Option[Properties],
      operations: Seq[WfMSOperation],
      configuration: Option[DriverConfiguration]
    ): WfMSDriver = t match {
      case "start" => WfMSStartDriver(properties, operations, configuration)
      case _ => throw new Exception(s"Illegal driver identifier $t; possible values: start")
    }
  }

}
