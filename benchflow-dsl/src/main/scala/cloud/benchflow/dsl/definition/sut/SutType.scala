package cloud.benchflow.dsl.definition.sut

import cloud.benchflow.dsl.definition.sut.http.Http
import cloud.benchflow.dsl.definition.sut.wfms.WfMS

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 *
 *  Has to be extended for each SUT type
 */
trait SutType
object SutType {

  def apply(sutType: String): SutType = sutType.toLowerCase match {
    case "wfms" => WfMS
    case "http" => Http
    case _ => throw new Exception("Illegal value for field suts_type; possible values: wfms, http")
  }
}
