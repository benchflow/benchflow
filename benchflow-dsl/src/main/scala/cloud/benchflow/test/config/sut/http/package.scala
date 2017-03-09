package cloud.benchflow.test.config.sut

import cloud.benchflow.test.config._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 21/07/16.
  */
package object http {

  case object Http extends SutsType

  /**
    * Http methods values
    */
  sealed trait HttpMethod
  object HttpMethod {
    def apply(method: String) = method.toLowerCase match {
      case "get" => Get
      case "put" => Put
      case "delete" => Delete
      case "post" => Post
      case _ => throw new Exception("Invalid http method specified.")
    }
  }
  case object Get extends HttpMethod
  case object Put extends HttpMethod
  case object Delete extends HttpMethod
  case object Post extends HttpMethod

  case class HttpOperation(override val name: String,
                           endpoint: String,
                           override val data: Option[String] = None,
                           method: HttpMethod,
                           headers: Map[String, String] = Map()) extends Operation(name, data)

  case class HttpDriver(override val properties: Option[Properties],
                        override val operations: Seq[HttpOperation],
                        override val configuration: Option[DriverConfiguration])
    extends Driver[HttpOperation](properties, operations, configuration)

}
