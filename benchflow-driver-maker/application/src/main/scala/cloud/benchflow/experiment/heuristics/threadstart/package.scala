package cloud.benchflow.experiment.heuristics

import cloud.benchflow.driversmaker.utils.env.ConfigYml
import cloud.benchflow.test.config.experiment.BenchFlowExperiment

import scala.reflect.ClassTag

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 30/05/16.
  */
package object threadstart {

  abstract class ThreadStartHeuristic[A <: HeuristicConfiguration : ClassTag](config: Map[String, Any])(env: ConfigYml)
    extends Heuristic[A](config)(env) {

    def delay(expConfig: BenchFlowExperiment, numOfUsedHosts: Int): Int
    def simultaneous(expConfig: BenchFlowExperiment): Boolean
    def parallel(expConfig: BenchFlowExperiment): Boolean

  }
  object ThreadStartHeuristic {

    def apply(strategy: String, configuration: Map[String, Any])(implicit env: ConfigYml): ThreadStartHeuristic[_] = strategy match {
      case "computeDelay" => new ComputeDelayHeuristic(configuration)
    }
  }

}
