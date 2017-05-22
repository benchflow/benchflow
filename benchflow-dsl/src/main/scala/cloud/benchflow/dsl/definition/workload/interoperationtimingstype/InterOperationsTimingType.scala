package cloud.benchflow.dsl.definition.workload.interoperationtimingstype

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
object InterOperationsTimingType extends Enumeration {
  type InterOperationsTimingType = Value
  val NegativeExponential = Value("negative-exponential")
  val Uniform = Value("uniform")
  val FixedTime = Value("fixed-time")
}
