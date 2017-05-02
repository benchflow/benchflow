package cloud.benchflow.experiment.sources.processors

import java.nio.file.Path

import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import com.sun.faban.harness.DefaultFabanBenchmark2
import org.apache.commons.io.{Charsets, FileUtils}
import spoon.reflect.declaration.CtClass
import spoon.reflect.reference.CtTypeReference

import scala.xml.PrettyPrinter

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 11/05/16.
  */
class FabanBenchmarkDeploymentDescriptorProcessor(expConfig: BenchFlowExperiment,
                                                  experimentId: String,
                                                  benchmarkOutputDir: Path)(implicit env: DriversMakerEnv)
  extends BenchmarkSourcesProcessor(expConfig, experimentId)(env) {

  override def isProcessable(element: CtClass[_]): Boolean = {
    super.isProcessable(element) &&
      (element.getSuperclass match {
        //is processable only if it extends DefaultFabanBenchmark2
        //case aClass: CtTypeReference[_] => aClass.getActualClass == classOf[DefaultFabanBenchmark2]
        case aClass: CtTypeReference[_] => aClass.isSubtypeOf(getFactory.Type.createReference(classOf[DefaultFabanBenchmark2]))
        //case aClass: CtTypeReference[_] => aClass.getActualClass.getName == "cloud.benchflow.experiment.harness.BenchFlowBenchmark"
        case _ => false
      })
  }

  override protected def doProcess(element: CtClass[_]): Unit = {

    val benchmarkClassQualifiedName = element.getQualifiedName
    val benchmarkName = expConfig.name

    val benchmarkDeploymentDescriptor =
      <benchmark>
          <name>{experimentId.replace('.', '_')}</name>
          <version></version>
          <config-form></config-form>
          <config-stylesheet></config-stylesheet>
          <config-file-name>run.xml</config-file-name>
          <benchmark-class>{benchmarkClassQualifiedName}</benchmark-class>
          <result-file-path></result-file-path>
          <metric></metric>
          <scaleName></scaleName>
          <scaleUnit></scaleUnit>
      </benchmark>

    val bmarkDeploymentDescriptorPath = benchmarkOutputDir.resolve("deploy/benchmark.xml")
    FileUtils.writeStringToFile(
      bmarkDeploymentDescriptorPath.toFile,
      new PrettyPrinter(400, 2).format(benchmarkDeploymentDescriptor),
      Charsets.UTF_8
    )

  }

}
