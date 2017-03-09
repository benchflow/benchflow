package cloud.benchflow.test.config.sut

import cloud.benchflow.test.config._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 21/07/16.
  */
package object wfms {



  case object WfMS extends SutsType


  case class WfMSOperation(override val name: String, override val data: Option[String]) extends Operation(name, data)


  sealed abstract class WfMSDriver(properties: Option[Properties],
                                   operations: Seq[WfMSOperation],
                                   configuration: Option[DriverConfiguration])
    extends Driver[WfMSOperation](properties, operations, configuration)



  case class WfMSStartDriver(override val properties: Option[Properties],
                             override val operations: Seq[WfMSOperation],
                             override val configuration: Option[DriverConfiguration])
    extends WfMSDriver(properties, operations, configuration)



  object WfMSDriver {
    def apply(t: String,
              properties: Option[Properties],
              operations: Seq[WfMSOperation],
              configuration: Option[DriverConfiguration]) = t match {
      case "start" => WfMSStartDriver(properties, operations, configuration)
      case _ => throw new Exception(s"Illegal driver identifier $t; possible values: start")
    }
  }

}
