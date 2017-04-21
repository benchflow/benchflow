package cloud.benchflow.experiment.sources.processors

import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import spoon.reflect.declaration.CtClass

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 20/04/16.
  */
class HttpBenchmarkProcessor(benchFlowBenchmark: BenchFlowExperiment, experimentId: String)(implicit env: DriversMakerEnv)
  extends BenchmarkSourcesProcessor(benchFlowBenchmark, experimentId)(env) {

  override protected def doProcess(element: CtClass[_]): Unit = ???
}
