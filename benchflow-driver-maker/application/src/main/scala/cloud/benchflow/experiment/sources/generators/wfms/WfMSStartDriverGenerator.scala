package cloud.benchflow.experiment.sources.generators.wfms

import java.nio.file.Path

import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.experiment.sources.processors.BenchmarkSourcesProcessor
import cloud.benchflow.experiment.sources.processors.drivers.annotations.BenchmarkDefinitionAnnotation
import cloud.benchflow.experiment.sources.processors.drivers.operations.wfms.WfMSStartDriverOperationsProcessor
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import cloud.benchflow.test.config.sut.wfms.{WfMSOperation, WfMSStartDriver}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 25/07/16.
  *
  * A generator for a start driver
  * @param generatedDriverClassOutputDir directory where the generated driver will be saved
  * @param generationResources location on file system of generation resources (libraries, plugins, templates)
  * @param expConfig configuration from which the driver will be generated
  * @param driver driver configuration
  */
class WfMSStartDriverGenerator(generatedDriverClassOutputDir: Path,
                               generationResources: Path,
                               expConfig: BenchFlowExperiment,
                               experimentId: String,
                               driver: WfMSStartDriver)
                              (implicit env: DriversMakerEnv)
  extends WfMSDriverGenerator[WfMSStartDriverOperationsProcessor[WfMSOperation]](generatedDriverClassOutputDir,
    generationResources,
    expConfig,
    experimentId,
    driver)(env)
{
  override def additionalProcessors: Seq[BenchmarkSourcesProcessor] =
    super.additionalProcessors :+ new BenchmarkDefinitionAnnotation(expConfig, experimentId)(env)

  protected val driverPath: Path = driversPath.resolve("wfms/Driver.java")
}
