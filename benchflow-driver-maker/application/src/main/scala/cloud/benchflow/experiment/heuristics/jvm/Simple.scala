package cloud.benchflow.experiment.heuristics.jvm

import cloud.benchflow.driversmaker.utils.env.ConfigYml
import cloud.benchflow.experiment.heuristics.HeuristicConfiguration
import cloud.benchflow.test.config.experiment.BenchFlowExperiment

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  *         Created on 30/05/16.
  */
class JvmParamsHeuristicConfiguration(mapConfig: Map[String, Any]) extends HeuristicConfiguration(mapConfig)
{
  val xmx = mapConfig.get("xmx").get.asInstanceOf[Int]
  val xms = mapConfig.get("xms").get.asInstanceOf[Int]
}


class SimpleJvmParamsHeuristic(mapConfig: Map[String, Any])(implicit env: ConfigYml)
  extends JvmParamsHeuristic[JvmParamsHeuristicConfiguration](mapConfig)(env) {

  override def xms(bb: BenchFlowExperiment) = config.xms
  override def xmx(bb: BenchFlowExperiment) = config.xmx

}
