package cloud.benchflow.dsl.demo

import cloud.benchflow.dsl.definition.BenchFlowExperiment
import cloud.benchflow.dsl.definition.datacollection.serverside.collector.CollectorMultipleEnvironmentYamlProtocol._
import cloud.benchflow.dsl.definition.datacollection.serverside.collector.{ CollectorMultiple, CollectorMultipleEnvironment }
import cloud.benchflow.dsl.definition.sut.configuration.targetservice.TargetServiceYamlProtocol._
import net.jcazevedo.moultingyaml._

import scala.util.Properties

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-07
 */
object DemoConverter {

  //noinspection ScalaStyle
  def convertExperimentToPreviousYamlString(benchFlowExperiment: BenchFlowExperiment): String = {

    val tab = "  "

    val yamlStringBuilder = StringBuilder.newBuilder

    yamlStringBuilder.append("testName" + ": " + benchFlowExperiment.name)
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append("description" + ": " + benchFlowExperiment.description)
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append("trials" + ": " + benchFlowExperiment.configuration.terminationCriteria.get.experiment.number)
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append("users" + ": " + benchFlowExperiment.configuration.users.get)
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append(Properties.lineSeparator)

    // Execution
    yamlStringBuilder.append("execution" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append(tab + "rampUp" + ": " + benchFlowExperiment.configuration.workloadExecution.get.rampUp.toSecondsPart)
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append(tab + "steadyState" + ": " + benchFlowExperiment.configuration.workloadExecution.get.steadyState.toSecondsPart)
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append(tab + "rampDown" + ": " + benchFlowExperiment.configuration.workloadExecution.get.rampDown.toSecondsPart)
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append(Properties.lineSeparator)

    // sut
    yamlStringBuilder.append("sut" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append(tab + "name" + ": " + benchFlowExperiment.sut.name)
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append(tab + "type" + ": " + "WfMS") // this is hardcoded because of the case of the letters
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append(tab + "version" + ": " + benchFlowExperiment.sut.version.toString)
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append(Properties.lineSeparator)

    // properties
    yamlStringBuilder.append("properties" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append(tab + "stats" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append((tab * 2) + "maxRunTime" + ": " + "6")
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append((tab * 2) + "interval" + ": " + "1")
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append(Properties.lineSeparator)

    // drivers
    yamlStringBuilder.append("drivers" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append("- start" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append((tab * 2) + "configuration" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append((tab * 3) + "max90th" + ": " + "60")
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append((tab * 2) + "operations" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    val operationsHead = benchFlowExperiment.workload.keySet.head
    benchFlowExperiment.workload(operationsHead).operations.foreach(operation => {
      yamlStringBuilder.append((tab * 2) + "- " + operation)
      yamlStringBuilder.append(Properties.lineSeparator)
    })

    yamlStringBuilder.append(Properties.lineSeparator)

    // sutConfiguration
    yamlStringBuilder.append("sutConfiguration" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append(tab + "targetService" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator + (tab * 2))
    yamlStringBuilder.append(benchFlowExperiment.sut.configuration.targetService.toYaml.prettyPrint.replace("\n", "\n" + (tab * 2)))
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append(tab + "deploy" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    benchFlowExperiment.sut.configuration.deployment.foreach {
      case (key, value) => {
        yamlStringBuilder.append((tab * 2) + key + ": " + value)
        yamlStringBuilder.append(Properties.lineSeparator)
      }
    }

    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append(tab + "benchflowConfig" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)

    benchFlowExperiment.dataCollection.get.serverSide.get.configurationMap.foreach {
      case (key: String, value: CollectorMultiple) =>

        yamlStringBuilder.append((tab * 2) + key + ": ")
        yamlStringBuilder.append(Properties.lineSeparator)

        value.collectors.foreach(name => {
          yamlStringBuilder.append((tab * 2) + "- " + name)
          yamlStringBuilder.append(Properties.lineSeparator)
        })

      case (key: String, value: CollectorMultipleEnvironment) =>
        yamlStringBuilder.append((tab * 2) + key + ": ")
        yamlStringBuilder.append(Properties.lineSeparator + (tab * 2))

        yamlStringBuilder.append(value.toYaml.prettyPrint
          .replace("\n", "\n" + (tab * 3))
          .replace("environment:", "config:")
          .replace("mysql:", "- mysql:"))
        yamlStringBuilder.append(Properties.lineSeparator)

      case (_, _) =>
    }

    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.toString()

  }

}
