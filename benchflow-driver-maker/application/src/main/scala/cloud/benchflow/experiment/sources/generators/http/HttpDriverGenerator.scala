package cloud.benchflow.experiment.sources.generators.http

import java.nio.file.Path

import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.experiment.sources.generators.DriverGenerator
import cloud.benchflow.experiment.sources.processors.drivers.annotations.BenchmarkDefinitionAnnotation
import cloud.benchflow.experiment.sources.processors.drivers.operations.http.HttpDriverOperationsProcessor
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import cloud.benchflow.test.config.sut.http.{HttpOperation, HttpDriver}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 25/07/16.
  */
class HttpDriverGenerator(generatedDriverClassOutputDir: Path,
                          generationResources: Path,
                          expConfig: BenchFlowExperiment,
                          experimentId: String,
                          driver: HttpDriver)(implicit env: DriversMakerEnv)
  extends DriverGenerator[HttpDriverOperationsProcessor[HttpOperation]](generatedDriverClassOutputDir,
    generationResources,
    expConfig,
    driver,
    experimentId)(env) {

  override def templateResources: Seq[Path] = Seq()
  override def additionalProcessors =
    Seq(new BenchmarkDefinitionAnnotation(expConfig, experimentId)(env))

  protected val driverPath: Path = driversPath.resolve("http/Driver.java")
}
