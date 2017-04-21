package cloud.benchflow.experiment.sources

import java.util.concurrent.TimeUnit

import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.test.config.sut.wfms.{WfMSOperation, WfMSDriver}
import cloud.benchflow.test.config.{Operation, Driver}
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.{CtPackage, CtClass}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 20/04/16.
  */
package object processors {

  /** base class for every processor */
  abstract class BenchmarkSourcesProcessor(val expConfig: BenchFlowExperiment,
                                           val experimentId: String)(implicit env: DriversMakerEnv)
    extends AbstractProcessor[CtClass[_]] {

    /***
      * Default implementation prevents processing of:
      * - anonymous classes
      * - inner classes
      * - libraries and plugins
      * - class BenchFlowBenchmark (base for each benchmark)
      */
    protected def isProcessable(element: CtClass[_]): Boolean = {
      (element match {
        case elemClass: CtClass[_] => !element.isAnonymous
        case _ => true
      }) &&
      (element.getParent() match {
        //if it's part of benchflow libraries or plugins, don't process it
        case elemPackage: CtPackage =>
             !(elemPackage.getQualifiedName.contains("libraries") ||
               elemPackage.getQualifiedName.contains("plugins"))
        //if it's an inner class, don't process it
        case elemClass: CtClass[_] => false
        case _ => true
      }) &&
      (element match {
        case elemClass: CtClass[_] =>
          elemClass.getSimpleName != "BenchFlowBenchmark" ||
          elemClass.getSimpleName != "BenchFlowDriver"
        case _ => true
      })
    }

    protected def doProcess(element: CtClass[_]): Unit

    final override def process(element: CtClass[_]) = {
      if(isProcessable(element))
        doProcess(element)
    }

  }

  /** a processor specific for a driver */
  abstract class DriverProcessor(benchFlowBenchmark: BenchFlowExperiment,
                                 driver: Driver[_ <: Operation],
                                 experimentId: String)(implicit env: DriversMakerEnv)
    extends BenchmarkSourcesProcessor(benchFlowBenchmark, experimentId)(env)

  /** base class for a processor that generates operations for a driver */
  abstract class DriverOperationsProcessor[T <: Operation](benchflowBenchmark: BenchFlowExperiment,
                                           driver: Driver[T],
                                           experimentId: String)(implicit env: DriversMakerEnv)
    extends DriverProcessor(benchflowBenchmark, driver, experimentId)(env) {

    protected def convertMax90th(timeUnit: TimeUnit, max90th: Double): Double = {
      timeUnit.convert(max90th.toLong, TimeUnit.SECONDS).toDouble
    }

    protected def generateOperation(element: CtClass[_])(op: T): Unit

    override def doProcess(element: CtClass[_]) = {
      element.setSimpleName(driver.getClass.getSimpleName)
      driver.operations.foreach(generateOperation(element))
    }

  }

}
