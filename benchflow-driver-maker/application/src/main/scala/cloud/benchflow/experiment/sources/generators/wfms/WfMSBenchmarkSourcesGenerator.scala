package cloud.benchflow.experiment.sources.generators.wfms

import java.nio.file.Path

import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.experiment.sources.generators.BenchmarkSourcesGenerator
import cloud.benchflow.experiment.sources.processors.{BenchmarkWfMSPluginLoaderProcessor, WfMSBenchmarkProcessor, WfMSPluginLoaderProcessor, BenchmarkSourcesProcessor}
import cloud.benchflow.experiment.sources.utils.ResolvePlugin
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import cloud.benchflow.test.config.sut.wfms.WfMSStartDriver

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 25/07/16.
  */
class WfMSBenchmarkSourcesGenerator(expConfig: BenchFlowExperiment,
                                    experimentId: String,
                                    generatedBenchmarkOutputDir: Path,
                                    env: DriversMakerEnv)
  extends BenchmarkSourcesGenerator(expConfig, experimentId, generatedBenchmarkOutputDir, env) {

  val benchmarkTemplate: Path = templatesPath.resolve("harness/wfms/WfMSBenchmark.java")

  override protected def benchmarkGenerationResources: Seq[Path] = {
    val wfmsPluginsPath = pluginsPath.resolve(s"wfms/${expConfig.sut.name}")
    val pluginPath = ResolvePlugin(wfmsPluginsPath, "WfMSPlugin.java", expConfig.sut.version)
    val wfmsLibraryPath = librariesPath.resolve("wfms/WfMSApi.java")
    super.benchmarkGenerationResources ++ Seq(wfmsLibraryPath, pluginPath)
  }

  override protected def benchmarkGenerationProcessors: Seq[BenchmarkSourcesProcessor] =
    //Seq(new WfMSPluginLoaderProcessor(expConfig, experimentId)(env),
    Seq(new BenchmarkWfMSPluginLoaderProcessor(expConfig, experimentId)(env),
        new WfMSBenchmarkProcessor(expConfig, experimentId)(env))

  //for each driver type, create a driver generator and run it
  override protected def generateDriversSources() = {
    val startDriver = expConfig.drivers.find(_.isInstanceOf[WfMSStartDriver]).get.asInstanceOf[WfMSStartDriver]
    new WfMSStartDriverGenerator(
      generatedBenchmarkOutputDir.resolve("src"),
      generationResources,
      expConfig,
      experimentId,
      startDriver)(env).generate()
  }

}

object WfMSBenchmarkSourcesGenerator {
  def apply(expConfig: BenchFlowExperiment,
            experimentId: String,
            generatedBenchmarkOutputDir: Path,
            env: DriversMakerEnv) =
    new WfMSBenchmarkSourcesGenerator(expConfig, experimentId, generatedBenchmarkOutputDir, env)
}
