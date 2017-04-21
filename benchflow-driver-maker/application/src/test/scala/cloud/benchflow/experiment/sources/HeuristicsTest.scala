package cloud.benchflow.experiment.sources

import cloud.benchflow.driversmaker.utils.env.{DriversMakerEnv, ConfigYml}
import cloud.benchflow.test.config.experiment.BenchFlowExperiment

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  *         Created on 20/08/16.
  */
object HeuristicsTest extends App {

  val configYml = new ConfigYml("./application/src/test/resources/app/config.yml")

  val benchFlowEnv = new DriversMakerEnv(configYml,
    "./application/src/test/resources/app/benchflow-services",
    "./application/src/test/resources/app/drivers",
    "8080")

  val deploymentDescriptor = "/Users/simonedavico/Desktop/docker-compose.yml"
  val testConfigurationDescriptor = "/Users/simonedavico/Desktop/benchflow-test.yml"

  val expConfig = scala.io.Source.fromFile(testConfigurationDescriptor).mkString
  val parsedExpConfig = BenchFlowExperiment.fromYaml(expConfig)

  println(benchFlowEnv.getHeuristics.scaleBalancer(parsedExpConfig)
    .threadPerScale(parsedExpConfig.drivers.head))

}
