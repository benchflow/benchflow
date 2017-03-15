package cloud.benchflow.dsl.definition.percent

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 14.03.17.
  */

case class Percent(underlying: Double) extends AnyVal {

  override def toString: String = "" + underlying * 100 + "%"

}
