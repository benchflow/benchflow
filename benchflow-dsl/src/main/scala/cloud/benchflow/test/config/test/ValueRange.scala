package cloud.benchflow.test.config.test

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  *         Created on 19/07/16.
  */
trait ValueRange[T] { def size: Int }

case class Constant[T](value: T) extends ValueRange[T] { def size = 1 }

case class Factors[T](values: Seq[T]) extends ValueRange[T] {
  def size = values.size

  //TODO: implement increment also for factors?

}

case class Step(min: Double, max: Double, step: Double,
                             stepFunction: (Double,Double) => Double) extends ValueRange[Double] {

  def increment(currentValue: Double): Double =
    stepFunction(currentValue, step)

  def size = {

    val numeric = implicitly[Numeric[Double]]
    val num = numeric.minus(max,min)

    (numeric match {
      case integ: Integral[Double] =>
        integ.quot(num, step).asInstanceOf[Int]
      case frac: Fractional[Double] =>
        frac.div(num, step).toInt
    }) + 1

  }

}
