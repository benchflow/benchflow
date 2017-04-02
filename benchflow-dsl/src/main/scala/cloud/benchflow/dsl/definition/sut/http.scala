package cloud.benchflow.dsl.definition.sut

import cloud.benchflow.dsl.definition.simone.properties.Properties
import cloud.benchflow.dsl.definition.simone.{ Driver, DriverConfiguration, Operation }

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-03-18
 */
package object http {

  case object Http extends SutType {
    override def toString: String = "http"
  }

  /**
   * Http methods values
   */
  sealed trait HttpMethod
  object HttpMethod {
    def apply(method: String): HttpMethod = method.toLowerCase match {
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

  case class HttpOperation(
    override val name: String,
    endpoint: String,
    override val data: Option[String] = None,
    method: HttpMethod,
    headers: Map[String, String] = Map()) extends Operation(name, data)

  case class HttpDriver(
    override val properties: Option[Properties],
    override val operations: Seq[HttpOperation],
    override val configuration: Option[DriverConfiguration])
      extends Driver[HttpOperation](properties, operations, configuration)

}
