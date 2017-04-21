package cloud.benchflow.experiment

import java.nio.file.Path

import cloud.benchflow.experiment.config.FabanBenchmarkConfigurationBuilder
import cloud.benchflow.experiment.config.deploymentdescriptor.DeploymentDescriptorBuilder

import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import cloud.benchflow.test.deployment.docker.compose.DockerCompose

import cloud.benchflow.experiment.sources.generators.BenchmarkSourcesGenerator
import cloud.benchflow.driversmaker.requests.Trial
import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv

import scala.xml.PrettyPrinter

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 30/05/16.
  */
class BenchmarkGenerator(experimentId: String,
                         experimentDescriptor: String,
                         deploymentDescriptor: String,
                         generatedBenchmarkOutputDir: Path,
                         env: DriversMakerEnv) {

    private val dd = DockerCompose.fromYaml(deploymentDescriptor)
    private val (deploymentDescriptorGenerator: DeploymentDescriptorBuilder,
                 sourcesGenerator: BenchmarkSourcesGenerator,
                 fabanConfigGenerator: FabanBenchmarkConfigurationBuilder) =
    {
      val expConfig = BenchFlowExperiment.fromYaml(experimentDescriptor)
      (new DeploymentDescriptorBuilder(expConfig, env),
       BenchmarkSourcesGenerator.apply(experimentId,
                                       expConfig,
                                       generatedBenchmarkOutputDir,
                                       env),
       new FabanBenchmarkConfigurationBuilder(expConfig,env,dd))
    }

    def generateSources() = sourcesGenerator.generate()

    def generateFabanConfigurationForTrial(t: Trial): String =
      """<?xml version="1.0" encoding="UTF-8"?>""" +
      System.lineSeparator() +
      new PrettyPrinter(400, 2).format(fabanConfigGenerator.build(t))

    def generateDeploymentDescriptorForTrial(t: Trial): String = {
      DockerCompose.toYaml(
        deploymentDescriptorGenerator.resolveDeploymentDescriptor(dd, t)
      )
    }
}