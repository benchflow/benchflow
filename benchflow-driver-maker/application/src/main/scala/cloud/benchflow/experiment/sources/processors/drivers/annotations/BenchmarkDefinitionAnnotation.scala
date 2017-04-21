package cloud.benchflow.experiment.sources.processors.drivers.annotations

import cloud.benchflow.experiment.sources.processors.BenchmarkSourcesProcessor
import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import com.sun.faban.driver.BenchmarkDefinition
import spoon.reflect.declaration.CtClass

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 05/05/16.
  */
class BenchmarkDefinitionAnnotation(expConfig: BenchFlowExperiment,
                                    experimentId: String)(implicit env: DriversMakerEnv)
  extends BenchmarkSourcesProcessor(expConfig, experimentId)(env) {

  override def doProcess(e: CtClass[_]): Unit = {

    //adds @BenchmarkDefinition annotation
    //TODO: make at least metric configurable
    val benchmarkDefinitionAnnotation = getFactory.Annotation().annotate(e, classOf[BenchmarkDefinition])
    benchmarkDefinitionAnnotation.addValue("name", s"[$experimentId] ${expConfig.name} Workload")
    benchmarkDefinitionAnnotation.addValue("version", "0.1")
    benchmarkDefinitionAnnotation.addValue("metric", "req/s")

  }
}
