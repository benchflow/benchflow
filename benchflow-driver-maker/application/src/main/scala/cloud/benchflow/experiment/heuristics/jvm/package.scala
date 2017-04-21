package cloud.benchflow.experiment.heuristics

import cloud.benchflow.driversmaker.utils.env.ConfigYml
import cloud.benchflow.test.config.experiment.BenchFlowExperiment

import scala.reflect.ClassTag

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 30/05/16.
  */
package object jvm {

  //configure xmx and xms params
  abstract class JvmParamsHeuristic[A <: HeuristicConfiguration : ClassTag](mapConfig: Map[String, Any])(env: ConfigYml)
    extends Heuristic[A](mapConfig)(env) {

    def xmx(expConfig: BenchFlowExperiment): Int

    def xms(expConfig: BenchFlowExperiment): Int

  }

  object JvmParamsHeuristic {
    def apply(strategy: String, configuration: Map[String, Any])(implicit env: ConfigYml) = strategy match {
      case "simple" => new SimpleJvmParamsHeuristic(configuration)(env)
      case "logistic" => new LogisticsGrowthJvmParamsHeuristic(configuration)(env)
    }
  }

}
