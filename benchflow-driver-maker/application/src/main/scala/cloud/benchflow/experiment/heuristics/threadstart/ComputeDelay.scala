package cloud.benchflow.experiment.heuristics.threadstart

import cloud.benchflow.driversmaker.utils.env.ConfigYml
import cloud.benchflow.experiment.heuristics.HeuristicConfiguration
import cloud.benchflow.experiment.heuristics.scale.ScaleBalancer
import cloud.benchflow.test.config.experiment.BenchFlowExperiment

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  *         Created on 30/05/16.
  */
class ComputeDelayConfiguration(mapConfig: Map[String, Any]) extends HeuristicConfiguration(mapConfig) {

  val simultaneous = mapConfig.get("simultaneous").get.asInstanceOf[Boolean]
  val parallel = mapConfig.get("parallel").get.asInstanceOf[Boolean]
  val scaleBalancer = mapConfig.get("scaleBalancer").get.asInstanceOf[BenchFlowExperiment => ScaleBalancer]

}

class ComputeDelayHeuristic(mapConfig: Map[String, Any])(implicit env: ConfigYml)
  extends ThreadStartHeuristic[ComputeDelayConfiguration](mapConfig)(env) {

  override def delay(bb: BenchFlowExperiment, numOfAgentProcesses: Int): Int = {

    val rampUp = bb.execution.rampUp
    val scale = config.scaleBalancer(bb).scale

    val toRound = (rampUp, config.parallel) match {
      case (0, _) => 0
      // rampUp/scale * 1000
      case (_, false) => rampUp.toFloat/scale * 1000
      // (rampUp/scale) * #(agents+master utilised) * 1000
      case _ => rampUp.toFloat/scale * numOfAgentProcesses * 1000
    }

    //round result half to even
    (toRound*2).toInt/2

  }

  override def simultaneous(expConfig: BenchFlowExperiment): Boolean = config.simultaneous

  override def parallel(expConfig: BenchFlowExperiment): Boolean = config.parallel
}
