package cloud.benchflow.experiment.sources.generators

import java.nio.file.{Paths, Path}

import cloud.benchflow.experiment.sources.generators.http.HttpBenchmarkSourcesGenerator
import cloud.benchflow.experiment.sources.generators.wfms.WfMSBenchmarkSourcesGenerator
import cloud.benchflow.experiment.sources.processors._
import cloud.benchflow.experiment.sources.processors.drivers.annotations._
import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.test.config._
import cloud.benchflow.test.config.experiment._
import cloud.benchflow.test.config.sut.http.Http
import cloud.benchflow.test.config.sut.wfms.WfMS

import spoon.Launcher
import spoon.compiler.SpoonResourceHelper

import scala.reflect.ClassTag

/**
  *  @author Simone D'Avico (simonedavico@gmail.com)
  *
  *  Contains the abstract driver and benchmark generators which have to be implemented for each sut
  */

/***
  * A generator for a driver class
  *
  * @param generatedDriverClassOutputDir directory where the generated driver will be saved
  * @param generationResources location on file system of generation resources (libraries, plugins, templates)
  * @param expConfig configuration from which the driver will be generated
  * @param driver driver configuration
  * @tparam A implementation of [[DriverOperationsProcessor]]
  */
abstract class DriverGenerator[A <: DriverOperationsProcessor[_]: ClassTag](val generatedDriverClassOutputDir: Path,
                                                                         val generationResources: Path,
                                                                         val expConfig: BenchFlowExperiment,
                                                                         val driver: Driver[_ <: Operation],
                                                                         val experimentId: String)(val env: DriversMakerEnv)
{

  //JVM doesn't allow this, unfortunately
  //val driverOperationsProcessor = new A(benchFlowBenchmark)
  //so we do the same with a reflection workaround:
  protected val driversPath = generationResources.resolve("templates/driver")
  protected def driverPath: Path

  private val driverOperationsProcessor =
    scala.reflect.classTag[A].runtimeClass
                             .getConstructor(classOf[BenchFlowExperiment],
                                             driver.getClass,
                                             classOf[String],
                                             classOf[DriversMakerEnv])
                             .newInstance(expConfig, driver, experimentId, env)
                             .asInstanceOf[A]

  //each driver generator has to define what template resources has to be added to the spoon launcher
  def templateResources: Seq[Path]
  //each driver generator can specify additional processors
  def additionalProcessors: Seq[BenchmarkSourcesProcessor]

  def generate() = {
      //val driverClassTemplate = generationResources.resolve("templates/driver/Driver.java")
      val driverClassTemplate = driverPath
      val spoonLauncher = new Launcher
      templateResources.foreach(resource =>
        spoonLauncher.addTemplateResource(SpoonResourceHelper.createFile(resource.toFile))
      )
      spoonLauncher.setSourceOutputDirectory(generatedDriverClassOutputDir.toFile)

      //add processors applied to all drivers
      spoonLauncher.addProcessor(driverOperationsProcessor)
      spoonLauncher.addProcessor(new TimeAnnotationProcessor(expConfig, experimentId)(env))
      spoonLauncher.addProcessor(new MixAnnotationProcessor(expConfig, driver, experimentId)(env))
      spoonLauncher.addProcessor(new BenchmarkDriverAnnotationProcessor(expConfig, driver, experimentId)(env))

      //apply driver specific processors
      additionalProcessors.foreach(additionalProcessor =>
        spoonLauncher.addProcessor(additionalProcessor)
      )

      spoonLauncher.addInputResource(driverClassTemplate.toString)
      spoonLauncher.run()
  }
}



/**
  * A generator for a Faban benchmark. Generates the Benchmark and Driver classes
  *
  * @param expConfig configuration from which the benchmark will be generated
  * @param experimentId experiment id
  * @param generatedBenchmarkOutputDir directory where the benchmark will be saved
  * @param env env info (heuristics, resources location, config.yml)
  */
abstract class BenchmarkSourcesGenerator(val expConfig: BenchFlowExperiment,
                                         val experimentId: String,
                                         val generatedBenchmarkOutputDir: Path,
                                         implicit val env: DriversMakerEnv) {

  val generationResources = Paths.get(env.getGenerationResourcesPath)
  val templatesPath = generationResources.resolve("templates")
  val librariesPath = generationResources.resolve("libraries")
  val pluginsPath = generationResources.resolve("plugins")

  protected def benchmarkTemplate: Path
  protected def generateDriversSources(): Unit
  protected def benchmarkGenerationResources: Seq[Path] = Seq()
    //Seq(templatesPath.resolve("harness/base/BenchFlowBenchmark.java"))
  protected def benchmarkGenerationProcessors: Seq[BenchmarkSourcesProcessor]

  protected def generateBenchmarkSource(): Unit = {
    val spoonLauncher = new Launcher

    benchmarkGenerationResources.foreach(resource => {
      spoonLauncher.addTemplateResource(SpoonResourceHelper.createFile(resource.toFile))
    })

    spoonLauncher.setSourceOutputDirectory(generatedBenchmarkOutputDir.resolve("src").toFile)

    benchmarkGenerationProcessors.foreach(processor =>
      spoonLauncher.addProcessor(processor)
    )

    //creates the file benchmark.xml
    spoonLauncher.addProcessor(
      new FabanBenchmarkDeploymentDescriptorProcessor(expConfig,experimentId,generatedBenchmarkOutputDir)(env)
    )

//    val args = Seq("--source-classpath", classPath)
//    spoonLauncher.setArgs(args.toArray)
//    println(spoonLauncher.getEnvironment.getSourceClasspath)

    spoonLauncher.addInputResource(benchmarkTemplate.toAbsolutePath.toString)
    spoonLauncher.run()
  }

  final def generate() = {
    generateBenchmarkSource()
    generateDriversSources()
  }
}
object BenchmarkSourcesGenerator {
  def apply(experimentId: String,
            expConfig: BenchFlowExperiment,
            generatedBenchmarkOutputDir: Path,
            env: DriversMakerEnv) =
    expConfig.sut.sutsType match {
      case Http => HttpBenchmarkSourcesGenerator(expConfig, experimentId, generatedBenchmarkOutputDir, env)
      case WfMS => WfMSBenchmarkSourcesGenerator(expConfig, experimentId, generatedBenchmarkOutputDir, env)
    }
}





