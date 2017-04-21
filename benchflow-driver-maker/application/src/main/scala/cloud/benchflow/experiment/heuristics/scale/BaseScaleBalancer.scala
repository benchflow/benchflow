package cloud.benchflow.experiment.heuristics.scale

import cloud.benchflow.test.config.Driver
import cloud.benchflow.test.config.experiment.BenchFlowExperiment

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 27/06/16.
  */
class BaseScaleBalancer(private val expConfig: BenchFlowExperiment) extends ScaleBalancer { self: BaseScaleBalancer =>

  val users = expConfig.users.users

  private def popularity(driver: Driver[_]): Float =
    driver.configuration.flatMap(_.popularity).getOrElse(1.toFloat/expConfig.drivers.size)


  def scale(driver: Driver[_]): Int = (users * popularity(driver)).toInt


  //scale = max(users/pop_d1, users/pop_d2, users/pop_d3...)
  private lazy val s = expConfig.drivers.map(d => users/popularity(d)).max.toInt


  def scale: Int = s


  def threadPerScale(driver: Driver[_]): Float = scale(driver)/scale



}
