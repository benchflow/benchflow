package cloud.benchflow.dsl.definition.sut.suttype

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-05-22
 */

object SutType extends Enumeration {
  type SutType = Value
  val WfMS = Value("wfms")
  val Http = Value("http")
}