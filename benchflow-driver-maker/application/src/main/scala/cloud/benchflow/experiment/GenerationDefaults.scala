package cloud.benchflow.experiment

import java.util.concurrent.TimeUnit

import cloud.benchflow.driversmaker.utils.env.ConfigYml
import cloud.benchflow.experiment.heuristics.allocation._
import cloud.benchflow.experiment.heuristics.jvm._
import cloud.benchflow.experiment.heuristics.scale._
import cloud.benchflow.experiment.heuristics.threadstart._

class GenerationDefaults(private implicit val bEnv: ConfigYml) {

  lazy val jvm = JvmParamsHeuristic(
    strategy = "simple",
    configuration = Map(
      "xms" -> 124,
      "xmx" -> 2048
    )
  )

  lazy val threadStart = ThreadStartHeuristic(
    strategy = "computeDelay",
    configuration = Map(
      "simultaneous" -> false,
      "parallel" -> true,
      "scaleBalancer" -> scaleBalancer
    )
  )

  lazy val scaleBalancer = ScaleBalancer(
    strategy = "balance",
    configuration = Map(
      "threshold" -> 500
    )
  )

  lazy val allocationHeuristic = AllocationHeuristic(
    strategy = "static",
    configuration = Map(
      "agentHostWeight" -> 1f,
      "masterHostWeight" -> 0.5f,
      "scaleBalancer" -> scaleBalancer,
      "agentAllocationThreshold" -> 100
    )
  )

}
object GenerationDefaults {

  val percentiles = Seq("25", "50", "75", "90", "95", "99.9")

  def percentileLimits(max90th: Double) = Seq[Double](0,0,0,max90th,0,0)

  val timeUnit = TimeUnit.MICROSECONDS

  val deviation: Double = 5

  val timeSync = false

  val interval = 1

  val max90th = Double.MaxValue


}
