package cloud.benchflow.dsl.demo

// need to import writers specifically otherwise can't find right protocol (at least when used in the same method)
import cloud.benchflow.dsl.definition.BenchFlowExperiment
import cloud.benchflow.dsl.definition.datacollection.serverside.collector.CollectorMultipleEnvironmentYamlProtocol.CollectorMultipleEnvironmentWriteFormat
import cloud.benchflow.dsl.definition.datacollection.serverside.collector.{ CollectorMultiple, CollectorMultipleEnvironment }
import cloud.benchflow.dsl.definition.sut.configuration.targetservice.TargetServiceYamlProtocol.TargetServiceWriteFormat
import cloud.benchflow.dsl.definition.types.percent.Percent
import cloud.benchflow.dsl.definition.types.percent.PercentYamlProtocol._
import cloud.benchflow.dsl.definition.workload.Workload
import cloud.benchflow.dsl.definition.workload.mix.{ FixedSequenceMix, FlatMix, FlatSequenceMix, MatrixMix }
import net.jcazevedo.moultingyaml._

import scala.util.Properties

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-07
 */
object DemoConverter {

  val tab = "  "

  //noinspection ScalaStyle
  def convertExperimentToPreviousYamlString(benchFlowExperiment: BenchFlowExperiment): String = {

    val yamlStringBuilder = StringBuilder.newBuilder

    yamlStringBuilder.append("testName" + ": " + benchFlowExperiment.name)
    yamlStringBuilder.append(Properties.lineSeparator)

    benchFlowExperiment.description match {
      case Some(description) =>
        yamlStringBuilder.append("description" + ": " + description)
        yamlStringBuilder.append(Properties.lineSeparator)

      case None => // don't add the description
    }

    // set the number of trials
    yamlStringBuilder.append("trials" + ": " + benchFlowExperiment.configuration.terminationCriteria.experiment.numberOfTrials)
    yamlStringBuilder.append(Properties.lineSeparator)

    // set the number of users
    yamlStringBuilder.append("users" + ": " + benchFlowExperiment.configuration.users)
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append(Properties.lineSeparator)

    appendExecution(benchFlowExperiment, yamlStringBuilder)

    appendSut(benchFlowExperiment, yamlStringBuilder)

    appendDrivers(yamlStringBuilder)

    // currently only supports 1 workload
    val firstWorkload = benchFlowExperiment.workload(benchFlowExperiment.workload.keySet.head)

    appendDriversOperations(yamlStringBuilder, firstWorkload)

    appendDriversMix(yamlStringBuilder, firstWorkload)

    yamlStringBuilder.append(Properties.lineSeparator)

    appendSutConfiguration(benchFlowExperiment, yamlStringBuilder)

    appendDataCollectionServerSide(benchFlowExperiment, yamlStringBuilder)

    appendDataCollectionClientSide(benchFlowExperiment, yamlStringBuilder)

    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.toString()

  }

  def appendDataCollectionClientSide(benchFlowExperiment: BenchFlowExperiment, yamlStringBuilder: StringBuilder): Unit = {

    val fabanConfig = benchFlowExperiment.dataCollection.clientSide.faban

    // properties
    yamlStringBuilder.append("properties" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append(tab + "stats" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append((tab * 2) + "maxRunTime" + ": " + fabanConfig.maxRunTime.toHoursPart)
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append((tab * 2) + "interval" + ": " + fabanConfig.interval.toSecondsPart)
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append(Properties.lineSeparator)
  }

  def appendDataCollectionServerSide(benchFlowExperiment: BenchFlowExperiment, yamlStringBuilder: StringBuilder): Unit = {
    // server side
    yamlStringBuilder.append(tab + "benchflowConfig" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)

    benchFlowExperiment.dataCollection.serverSide match {

      case Some(serverSideConfiguration) =>
        serverSideConfiguration.configurationMap.foreach {

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

            yamlStringBuilder.append(
              CollectorMultipleEnvironmentWriteFormat.write(value).prettyPrint
                .replace("\n", "\n" + (tab * 3))
                .replace("environment:", "config:")
                .replace("mysql:", "- mysql:"))
            yamlStringBuilder.append(Properties.lineSeparator)

          case (_, _) =>
        }

      case None => // don't add server side config
    }

  }

  def appendSutConfiguration(benchFlowExperiment: BenchFlowExperiment, yamlStringBuilder: StringBuilder): Unit = {
    // sutConfiguration
    yamlStringBuilder.append("sutConfiguration" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append(tab + "targetService" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator + (tab * 2))

    yamlStringBuilder.append(TargetServiceWriteFormat.write(benchFlowExperiment.sut.configuration.targetService).prettyPrint.replace("\n", "\n" + (tab * 2)))
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append(tab + "deploy" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    benchFlowExperiment.sut.configuration.deployment.foreach {
      case (key, value) => {
        yamlStringBuilder.append((tab * 2) + key + ": " + value)
        yamlStringBuilder.append(Properties.lineSeparator)
      }
    }
  }

  def appendDriversMix(yamlStringBuilder: StringBuilder, firstWorkload: Workload): Unit = {
    // drivers mix
    firstWorkload.mix match {
      case Some(mix) =>

        yamlStringBuilder.append((tab * 2) + "mix" + ":")
        yamlStringBuilder.append(Properties.lineSeparator)

        // add mix
        mix.mix match {

          case FixedSequenceMix(seqMix: Seq[String]) =>
            yamlStringBuilder.append((tab * 3) + "fixedSequence" + ": " + seqMix.toYaml.print(flowStyle = Flow))

          case FlatMix(flatMix: Seq[Percent]) =>
            yamlStringBuilder.append((tab * 3) + "flat" + ": " + flatMix.map(x => s"${(x.underlying * 100).toInt}%").toYaml.print(flowStyle = Flow))

          case FlatSequenceMix(mixPercents: Seq[Percent], sequences: Seq[Seq[String]]) =>
            yamlStringBuilder.append((tab * 3) + "flat" + ": " + mixPercents.map(x => s"${(x.underlying * 100).toInt}%").toYaml.print(flowStyle = Flow))
            yamlStringBuilder.append((tab * 3) + "sequences" + ": " + sequences.toYaml.print(flowStyle = Flow))

          case MatrixMix(matrixMix: Seq[Seq[Percent]]) =>
            yamlStringBuilder.append((tab * 3) + "matrix" + ": " + matrixMix.map(
              _.map(x => s"${(x.underlying * 100).toInt}%")).toYaml.print(flowStyle = Flow))
        }

        // add deviation
        mix.maxDeviation match {
          case Some(deviation) =>
            yamlStringBuilder.append((tab * 3) + "deviation" + ": " + (deviation.underlying * 100).toInt.toYaml.prettyPrint)
            yamlStringBuilder.append(Properties.lineSeparator)

          case None => // don't add deviation

        }

      case None => // don't add mix
    }
  }

  def appendDriversOperations(yamlStringBuilder: StringBuilder, firstWorkload: Workload): Unit = {
    // drivers operations
    yamlStringBuilder.append((tab * 2) + "operations" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    firstWorkload.operations.foreach(operation => {
      yamlStringBuilder.append((tab * 2) + "- " + operation)
      yamlStringBuilder.append(Properties.lineSeparator)
    })
  }

  def appendDrivers(yamlStringBuilder: StringBuilder): Unit = {
    // drivers
    yamlStringBuilder.append("drivers" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append("- start" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append((tab * 2) + "configuration" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append((tab * 3) + "max90th" + ": " + "60")
    yamlStringBuilder.append(Properties.lineSeparator)
  }

  def appendSut(benchFlowExperiment: BenchFlowExperiment, yamlStringBuilder: StringBuilder): Unit = {
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
  }

  def appendExecution(benchFlowExperiment: BenchFlowExperiment, yamlStringBuilder: StringBuilder): Unit = {

    val workloadExecution = benchFlowExperiment.configuration.workloadExecution

    // Execution
    yamlStringBuilder.append("execution" + ": ")
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append(tab + "rampUp" + ": " + workloadExecution.rampUp.toSecondsPart)
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append(tab + "steadyState" + ": " + workloadExecution.steadyState.toSecondsPart)
    yamlStringBuilder.append(Properties.lineSeparator)
    yamlStringBuilder.append(tab + "rampDown" + ": " + workloadExecution.rampDown.toSecondsPart)
    yamlStringBuilder.append(Properties.lineSeparator)

    yamlStringBuilder.append(Properties.lineSeparator)

  }
}
