package cloud.benchflow.experiment.heuristics.scale

import cloud.benchflow.experiment.heuristics.Configurable
import cloud.benchflow.test.config.Driver
import cloud.benchflow.test.config.experiment.BenchFlowExperiment


/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 27/06/16.
  */
class FixedScaleBalancer(val configuration: Map[String, Any])
                        (expConfig: BenchFlowExperiment)
  extends BaseScaleBalancer(expConfig)
  with Configurable
{

  override def scale = users

  override def threadPerScale(driver: Driver[_]) = {
    configuration.get("threadPerScale").get.asInstanceOf[Float]
  }

}
