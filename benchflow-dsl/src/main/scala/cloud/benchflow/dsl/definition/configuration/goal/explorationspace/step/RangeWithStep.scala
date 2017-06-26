package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.step

import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException

import scala.annotation.tailrec
import scala.util.{ Failure, Success, Try }

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-23
 */
object RangeWithStep {

  def stepList[T](
    range: List[T],
    step: T,
    func: (T, T) => T)(implicit num: Numeric[T]): Try[List[T]] = {

    generateList(List(range.head), step, range(1), func)

  }

  @tailrec private def generateList[T](
    list: List[T], step: T, maxValue: T, func: (T, T) => T)(implicit num: Numeric[T]): Try[List[T]] = {

    val next = func(list.last, step)

    if (num.equiv(next, list.last)) {
      // if we don't change value we stop
      // to avoid infinite recursion
      Failure(new BenchFlowDeserializationException(s"same value generated multiple times:${list :+ next}", new Throwable))

    } else if (list.size == 1 &&
      num.gt(
        num.abs(num.minus(num.abs(maxValue), num.abs(next))),
        num.abs(num.minus(num.abs(maxValue), num.abs(list.head))))) {
      // if we diverge from reaching the maxValue we stop
      Failure(new BenchFlowDeserializationException(s"diverging list:${list :+ next}", new Throwable))

    } else if (num.gt(maxValue, list.head) && num.gteq(next, maxValue)) {
      // if increasing step list and next value is greater or equal to maxValue we
      // stop recursion and add the maxValue as last element
      Success(list :+ maxValue)

    } else if (num.lt(maxValue, list.head) && num.lteq(next, maxValue)) {
      // if decreasing step list and next value is less or equal to maxValue we
      // stop recursion and add the maxValue as last element
      Success(list :+ maxValue)

    } else {
      // add the next value and recurse
      generateList(list :+ next, step, maxValue, func)
    }
  }

}
