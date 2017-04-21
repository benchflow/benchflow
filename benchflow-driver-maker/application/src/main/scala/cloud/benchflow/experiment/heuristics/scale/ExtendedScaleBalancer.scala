package cloud.benchflow.experiment.heuristics.scale

import cloud.benchflow.experiment.heuristics.Configurable
import cloud.benchflow.test.config.Driver
import cloud.benchflow.test.config.experiment.BenchFlowExperiment


/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 27/06/16.
  */
private[scale] abstract class ExtScaleBalancer(val decorate: ScaleBalancer,
                                               val configuration: Map[String, Any])
  extends ScaleBalancer
     with ScaleBalancerDecorator
     with Configurable
{

  private def threshold = configuration.get("threshold").get.asInstanceOf[Int]


  private def scalingFactor = decorate.scale/threshold


  override def threadPerScale(driver: Driver[_]) =
    if(decorate.scale > threshold)
      decorate.threadPerScale(driver) * scalingFactor
    else
      decorate.threadPerScale(driver)


  override def scale = if (decorate.scale > threshold) threshold else decorate.scale


  override def scale(driver: Driver[_]): Int = decorate.scale(driver)


  override def users: Int = decorate.users


}

class ExtendedScaleBalancer(configuration: Map[String, Any])
                           (expConfig: BenchFlowExperiment)
  extends ExtScaleBalancer(new BaseScaleBalancer(expConfig), configuration)