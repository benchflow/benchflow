package cloud.benchflow.experiment.heuristics

import cloud.benchflow.test.config.Driver
import cloud.benchflow.test.config.experiment.BenchFlowExperiment

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 30/05/16.
  */
package object scale {

  trait ScaleBalancerDecorator {
    def decorate: ScaleBalancer
  }

  trait ScaleBalancer {
    def users: Int
    def scale: Int
    def scale(driver: Driver[_]): Int
    def threadPerScale(driver: Driver[_]): Float
  }

  object ScaleBalancer {

    def apply(strategy: String, configuration: Map[String, Any]) = (expConfig: BenchFlowExperiment) => {
      strategy match {
        case "balance" => new ExtendedScaleBalancer(configuration)(expConfig)
        case "simple" => new FixedScaleBalancer(configuration)(expConfig) //with FixedScaleBalancer
        case _ => throw new Exception("Unknown strategy for ScaleBalancer. Implement the strategy into " +
                                      "the ScaleBalancer factory.")
      }
    }

  }

}