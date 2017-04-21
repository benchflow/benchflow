package cloud.benchflow.experiment.config

import java.nio.file.Paths

import cloud.benchflow.experiment.GenerationDefaults
import cloud.benchflow.experiment.config.benchflowservices.collectors._
import cloud.benchflow.experiment.config.benchflowservices.monitors.{MonitorRunPhase, MonitorAPI}
import cloud.benchflow.test.config.{Driver, Binding, Properties}
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import cloud.benchflow.test.deployment.docker.compose.DockerCompose
import cloud.benchflow.driversmaker.requests.Trial
import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.experiment.config.benchflowservices._
import cloud.benchflow.test.deployment.docker.service.{Network, Service}

import scala.xml.{Text, Node, Elem}
import scala.xml.transform.{RuleTransformer, RewriteRule}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 23/02/16.
  */
class FabanBenchmarkConfigurationBuilder(expConfig: BenchFlowExperiment,
                                         benv: DriversMakerEnv,
                                         deploymentDescriptor: DockerCompose) {

  //Faban doesn't like when there are newlines in the content of the tags,
  //so we remove them
  private val removeNewlinesRule = new RewriteRule {
    val minimizeEmpty = false
    override def transform(n: Node): Seq[Node] = n match {
      case Elem(prefix, label, attribs, scope, _, Text(content)) =>
        Elem(prefix, label, attribs, scope, minimizeEmpty, Text(content.trim))
      case other => other
    }
  }
  private object removeNewlines extends RuleTransformer(removeNewlinesRule)

  private val updateStatsRewriteRule = new RewriteRule {

    def addIntervalIfNotSpecified(statsChildren: Seq[Node]) = {
      statsChildren.find {
        case c: Elem if c.label == "interval" => true
        case _ => false
      } match {
        case Some(interval) => statsChildren
        case None => <interval>{GenerationDefaults.interval}</interval> :: statsChildren.toList
      }
    }

    val minimizeEmpty = false
    override def transform(n: Node): Seq[Node] = {
      n match {
        case stats: Elem if stats.label == "stats" =>
          Elem(stats.prefix, "stats", stats.attributes, stats.scope, minimizeEmpty, addIntervalIfNotSpecified(stats.child): _*)
        case other => other

      }
    }
  }

  private object insertIntervalIfNotExists extends RuleTransformer(updateStatsRewriteRule)

  private def propertyToNamespace =
    Map(
      "hostConfig" -> "fa",
      "hostPorts" -> "fa",
      "host" -> "fa",
      "tools" -> "fh",
      "scale" -> "fa",
      "runControl" -> "fa",
      "rampUp" -> "fa",
      "steadyState" -> "fa",
      "rampDown" -> "fa",
      "cpus" -> "fh",
      "enabled" -> "fh",
      "timeSync" -> "fh"
    )

  private def addFabanNamespace(elem: Node): Node = {
    elem match {
      case elem: Elem =>
        val ns = propertyToNamespace.getOrElse(elem.label, "")
        <xml>{elem.child.map(addFabanNamespace)}</xml>.copy(label = ns + (if (ns != "") ":" else "") + elem.label,
                                                            attributes = elem.attributes)
      case _ => elem
    }
  }

  private def convert(property: (String, Any)): Elem =
    <xml>{

      property._2 match {
        case map: Map[String, Any] => map.map(convert)
        case nestedList: List[Map[String, Any]] => nestedList.map(n => n.map(convert))
        case plainList: List[_] => plainList
        case other => other
      }

    }</xml>.copy(label = property._1)


  private def convert(properties: Properties): Iterable[Elem] =
    properties.properties.map(convert)


  private def convertDriver(driver: Driver[_], agents: Set[(String, Int)]): Elem =
    <driverConfig name={driver.getClass.getSimpleName}>
      <agents>{agents.map { case (host, numOfAgents) => s"$host:$numOfAgents" }.mkString(" ")}</agents>
      {
        driver.properties match {
          case None => scala.xml.Null
          case Some(properties) => convert(properties).map(insertIntervalIfNotExists)
        }
      }
    </driverConfig>

  private def getJavaOpts: String = {
    val jvm = benv.getHeuristics.jvm
    s"-Xmx${jvm.xmx(expConfig)}m -Xms${jvm.xms(expConfig)}m -XX:+DisableExplicitGC"
  }

  private def collectorDefinition(collectorName: String) = {
    benchFlowServiceDescriptor(collectorName, Collector, Paths.get(benv.getBenchFlowServicesPath))
  }

  private def monitorConfiguration(serviceName: String,
                                   collectorDefinition: Service,
                                   monitorName: String,
                                   bindingConfig: Option[Properties]) = {

    val monitorDescriptor = benchFlowServiceDescriptor(monitorName, Monitor, Paths.get(benv.getBenchFlowServicesPath))
    val monitorAPI = MonitorAPI.fromYaml(monitorDescriptor)
    val monitorDefinition = Service.fromYaml(monitorDescriptor)
    val monitorRunPhase = MonitorRunPhase.fromYaml(monitorDescriptor)

    val monitorEnv = monitorDefinition.environment.vars.keySet
    val collectorEnv = collectorDefinition.environment.vars.keySet

    val unionEnv = monitorEnv union collectorEnv
    val monitorParams = bindingConfig.map(_.properties.keySet).getOrElse(Set.empty) diff unionEnv

    <monitor name={monitorName}>
      <id>{ monitorId(serviceName, collectorDefinition.name, monitorName) }</id>
      <configuration>
        {
          monitorParams.map(paramKey => {
            bindingConfig.flatMap(
              _.properties.get(paramKey).map(paramValue => {
                <param name={paramKey}>{ paramValue }</param>
              })
            ).getOrElse(scala.xml.Null)
          })
        }
      </configuration>
      <api>
        { monitorAPI.start.map(start => <start>{start}</start>).getOrElse(scala.xml.Null) }
        { <monitor>{monitorAPI.monitor}</monitor> }
        { monitorAPI.stop.map(stop => <stop>{stop}</stop>).getOrElse(scala.xml.Null) }
      </api>
      <runPhase>{ monitorRunPhase.toString }</runPhase>
    </monitor>

  }

  private def resolveServiceAddress(serviceName: String) = {
    val serverAlias = expConfig.getAliasForService(serviceName).get
    benv.getPublicIp(serverAlias)
  }

  private def collectorConfiguration(serviceName: String, binding: Binding) = {
    val collectorName = binding.boundService
    val collectorDescriptor = collectorDefinition(collectorName)
    val definition = Service.fromYaml(collectorDescriptor)

    val collectorDependencies = CollectorDependencies.fromYaml(collectorDescriptor).monitors
    val collectorApi = CollectorAPI.fromYaml(collectorDescriptor)

    <collector name={collectorName}>
      <id>{ collectorId(serviceName, collectorName) }</id>
      {
        definition.net match {

          case(Some(Network("host"))) =>
            <address>{s"${resolveServiceAddress(serviceName)}:${definition.getPublicPort.get}"}</address>
          case _ =>
            scala.xml.Null
        }

      }
      <api>
        { collectorApi.start.map(start => <start>{ start }</start>).getOrElse(scala.xml.Null) }
        <stop>{ collectorApi.stop }</stop>
      </api>
      <monitors>
        {
          collectorDependencies.map(monitor =>
            monitorConfiguration(serviceName, definition, monitor, binding.config)
          )
        }
      </monitors>
    </collector>
  }

  private def serviceConfiguration(serviceName: String) = {
    //1. get the list of collectors for the service
    val bindings = expConfig.getBindingsForService(serviceName)

    //2. getCollectorConfiguration(serviceName, collectorName) for each collector
    <service name={serviceName}>
      <collectors>
        { bindings.map(collectorConfiguration(serviceName, _)) }
      </collectors>
    </service>
  }

  private def resolveSutEndpoint() = {
    val targetServiceName = expConfig.sutConfiguration.targetService.name
    val serverAlias = expConfig.getAliasForService(targetServiceName).get
    val serverIp = benv.getIp(serverAlias)
    val publicPort = deploymentDescriptor.services.get(targetServiceName).get.getPublicPort.get
    s"$serverIp:$publicPort"
  }

  def build(trial: Trial) = {

    val scaleBalancer = benv.getHeuristics.scaleBalancer(expConfig)
    val agents = benv.getHeuristics.allocationHeuristic.agents(expConfig)
    val usedHosts = agents.values.reduce(_.union(_))


    def agentProcesses(hostsForDriver: Set[(String, Int)]): Int = {
      hostsForDriver.map(_._2).sum
    }

    val totalAgentProcesses = agents.values.map(agentProcesses).sum

    removeNewlines(
      <xml>

        <jvmConfig xmlns:fh="http://faban.sunsource.net/ns/fabanharness">
          <fh:javaHome>/usr/lib/jvm/java7</fh:javaHome>
          <fh:jvmOptions>{ getJavaOpts }</fh:jvmOptions>
        </jvmConfig>

        <fa:runConfig definition={s"cloud.benchflow.experiment.drivers.${expConfig.drivers.head.getClass.getSimpleName}"}
                      xmlns:fa="http://faban.sunsource.net/ns/faban"
                      xmlns:fh="http://faban.sunsource.net/ns/fabanharness"
                      xmlns="http://faban.sunsource.net/ns/fabandriver">
            <fh:description>{ expConfig.description }</fh:description>

            <fa:scale>{ scaleBalancer.scale }</fa:scale>
            <fh:timeSync>{ GenerationDefaults.timeSync }</fh:timeSync>

            <fa:hostConfig>
              <fa:host>{ usedHosts.map { case (host, numOfAgents) => s"$host" }.mkString(" ") }</fa:host>
              <fh:tools>NONE</fh:tools>
            </fa:hostConfig>

            {
              val xmlProps = convert(expConfig.properties).map(addFabanNamespace)
              xmlProps.map(insertIntervalIfNotExists)
              //++ bb.drivers.map(convertDriver).map(addFabanNamespace) }
            }

            { agents.map { case (d, hosts) => convertDriver(d, hosts) } }

            <fa:runControl unit="time">
              <fa:rampUp>{ expConfig.execution.rampUp }</fa:rampUp>
              <fa:steadyState>{ expConfig.execution.steadyState }</fa:steadyState>
              <fa:rampDown>{ expConfig.execution.rampDown }</fa:rampDown>
            </fa:runControl>

            <threadStart>
              <delay>{ benv.getHeuristics.threadStart.delay(expConfig, totalAgentProcesses) }</delay>
              <simultaneous>{ benv.getHeuristics.threadStart.simultaneous(expConfig) }</simultaneous>
              <parallel>{ benv.getHeuristics.threadStart.parallel(expConfig) }</parallel>
            </threadStart>

        </fa:runConfig>

        <sutConfiguration>
          <serviceName>{ expConfig.sutConfiguration.targetService.name }</serviceName>
          <endpoint>{ expConfig.sutConfiguration.targetService.endpoint }</endpoint>
          <address>{resolveSutEndpoint()}</address>
        </sutConfiguration>

        <benchFlowServices>
            <privatePort>{ benv.getPrivatePort }</privatePort>
            <deploymentManager>{ benv.getDeploymentManagerAddress }</deploymentManager>
            <servicesConfiguration>
              { deploymentDescriptor.services.keys.map(serviceConfiguration)  }
            </servicesConfiguration>
         </benchFlowServices>

         <benchFlowRunConfiguration>
           <trialId>{trial.getTrialId}</trialId>
         </benchFlowRunConfiguration>

      </xml>.copy(label = expConfig.name)
    )

  }

}
