package cloud.benchflow.experiment

import cloud.benchflow.driversmaker.utils.env.ConfigYml

import scala.reflect.ClassTag

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 23/05/16.
  */
package object heuristics {

  private[heuristics] trait Configurable {
    protected val configuration: Map[String, Any]
  }

  abstract class HeuristicConfiguration(config: Map[String, Any])

  abstract class Heuristic[A <: HeuristicConfiguration : ClassTag](val mapConfig: Map[String, Any])(val env: ConfigYml)
  {
    protected val config = scala.reflect.classTag[A].runtimeClass
      .getConstructor(classOf[Map[String, Any]])
      .newInstance(mapConfig).asInstanceOf[A]
  }

}
