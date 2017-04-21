package cloud.benchflow.experiment.config.deploymentdescriptor

import java.nio.file.Paths
import cloud.benchflow.driversmaker.requests.Trial
import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.experiment.config._
import benchflowservices.{BenchFlowServiceType, Collector => CollectorType, Monitor => MonitorType, benchFlowServiceDescriptor}
import cloud.benchflow.test.config.Binding
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import cloud.benchflow.test.deployment.docker.compose.DockerCompose
import cloud.benchflow.test.deployment.docker.service._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 06/07/16.
  */
class DeploymentDescriptorBuilder(protected val testConfig: BenchFlowExperiment,
                                  protected val env: DriversMakerEnv) {

  //for convenience in type signatures
  type Monitor = Service
  type Collector = Service

  private def addIpToPorts(ip: String, ports: String) = {
    ports.contains(':') match {
      case true => s"$ip:$ports"
      case false => s"$ip::$ports"
    }
  }

  /***
    * An abstract resolver of BenchFlow services descriptors.
    * Implemented for monitors and collectors
    *
    * @tparam T the source of info that should be available to the resolver to resolve
    *           related BenchFlow services
    */
  private trait BenchFlowServiceResolver[T] {
    val self: T
    val trial: Trial
    def apply: List[Service]

    protected def deploymentDescriptorDefinitionByType(serviceName: String, serviceType: BenchFlowServiceType) = {
      Service.fromYaml(
        benchFlowServiceDescriptor(serviceName, serviceType, Paths.get(env.getBenchFlowServicesPath))
      )
    }

    protected def generateEnvVariables(service: Service): Service = {
      service.environment.vars("BENCHFLOW_CONTAINER_NAME") = service.containerName.get.container_name
      service
    }
  }

  /***
    * An abstract strategy to resolve a BenchFlow variable
    *
    * @tparam T the source of info that should be available to the strategy to resolve the variable
    */
  private trait VariablesResolutionStrategy[T] {
    def resolve(source: T)(variable: String): String
  }

  /***
    *
    * @param self a tuple (service,collector) for which monitors should be resolved
    * @param trial trial info
    */
  private class MonitorResolver(val self: (Service, Collector), val trial: Trial)
    extends BenchFlowServiceResolver[(Service, Collector)] with MonitorResolutionStrategy {

    private val service = self._1
    private val collector = self._2

    private def monitorsNamesForCollector: Seq[String] = {
      import cloud.benchflow.experiment.config.benchflowservices.collectors.CollectorDependencies

      val collectorDescriptor =
        benchFlowServiceDescriptor(CollectorType.getName(collector.name),
                                   CollectorType, Paths.get(env.getBenchFlowServicesPath))

      CollectorDependencies.fromYaml(collectorDescriptor).monitors
    }

    private def deploymentDescriptorDefinition(monitorName: String): Monitor = {
      deploymentDescriptorDefinitionByType(monitorName, MonitorType)
    }

    private def getAliasFromCollector(collector: Collector) = {
      val serviceName = CollectorType.getServiceName(collector.name)
      testConfig.getAliasForService(serviceName).get
    }

    private def resolveDeploymentInfo(monitor: Monitor): Monitor = {
      val collectorBaseName = CollectorType.getName(collector.name)
      val name = benchflowservices.monitorId(service.name, collectorBaseName, monitor.name)
      val alias = getAliasFromCollector(collector)
      val aliasIp = env.getPublicIp(alias)

      //monitor.environment.vars("constraint") = alias
      monitor.environment.vars("constraint") = env.getHostname(alias)

      monitor.copy(
        name = name,
        containerName = Some(ContainerName(s"${name}_${trial.getTrialId}")),
        //ports = monitor.ports.map(p => Ports(Seq(s"$aliasIp:${p.ports.head}"))),
        ports = monitor.ports.map(p => Ports(Seq(addIpToPorts(aliasIp, p.ports.head))))
//        net = Some(Network("bridge"))
      )
    }

    private def resolveMonitorVariables(monitor: Monitor): Monitor = {
      monitor.copy(
        environment = Environment(
          monitor.environment.vars.map {
            case (variableName, variableValue) =>
              variableName -> resolve(service, collector, monitor)(variableValue)
          }
        )
      )
    }

    override protected def generateEnvVariables(m: Monitor): Monitor = {
      (super.generateEnvVariables _ andThen { monitor =>
        monitor.environment.vars("BENCHFLOW_MONITOR_NAME") = MonitorType.getName(monitor.name)
        monitor
      })(m)
    }

    override def apply: List[Monitor] = {
      monitorsNamesForCollector match {
        case noMonitors if noMonitors isEmpty => List.empty
        case monitorNames => monitorNames.map { monitorName =>
          (deploymentDescriptorDefinition _
            andThen resolveDeploymentInfo
            andThen generateEnvVariables
            andThen resolveMonitorVariables)(monitorName)
        }.toList
      }
    }

  }

  /***
    *
    * @param self the service for which collectors should be resolved
    * @param trial trial info
    */
  private class CollectorResolver(val self: Service, val trial: Trial)
    extends BenchFlowServiceResolver[Service] with CollectorResolutionStrategy {

    private def collectorsBindings: Map[String, Binding] = {
      testConfig.sutConfiguration.bfConfig.bindings(self.name)
                .map(binding => binding.boundService -> binding).toMap
    }

    private def deploymentDescriptorDefinition(collectorName: String): Collector = {
      deploymentDescriptorDefinitionByType(collectorName, CollectorType)
    }

    //generates benchflow env variables
    override protected def generateEnvVariables(c: Collector): Collector = {
      (super.generateEnvVariables _ andThen { collector => {
        collector.environment.vars("BENCHFLOW_COLLECTOR_NAME") = CollectorType.getName(collector.name)
        collector.environment.vars("BENCHFLOW_EXPERIMENT_ID") = trial.getExperimentId
        collector.environment.vars("BENCHFLOW_TRIAL_ID") = trial.getTrialId
//        collector.environment.vars("BENCHFLOW_TRIAL_TOTAL") = trial.getTotalTrials.toString
        collector.environment.vars("BENCHFLOW_DATA_NAME") = CollectorType.getName(collector.name)
        collector.environment.vars("MINIO_HOST") = env.getConfigYml.getVariable[String]("BENCHFLOW_MINIO_IP")
        collector.environment.vars("MINIO_PORT") = env.getConfigYml.getVariable[String]("BENCHFLOW_MINIO_PORT")
        collector.environment.vars("MINIO_SECRETACCESSKEY") = env.getConfigYml.getVariable[String]("MINIO_SECRET_KEY")
        collector.environment.vars("MINIO_ACCESSKEYID") = env.getConfigYml.getVariable[String]("MINIO_ACCESS_KEY")
        collector.environment.vars("KAFKA_HOST") = env.getConfigYml.getVariable[String]("BENCHFLOW_KAFKA_IP")
        collector.environment.vars("KAFKA_PORT") = env.getConfigYml.getVariable[String]("BENCHFLOW_KAFKA_PORT")
        collector.environment.vars("SUT_NAME") = testConfig.sut.name
        collector.environment.vars("SUT_VERSION") = testConfig.sut.version.toString
        collector.environment.vars("KAFKA_TOPIC") = collector.environment.vars.get("BENCHFLOW_DATA_NAME").get
        collector
        }
      }).apply(c)

    }

    //resolves constraint, collector name, ports
    private def resolveDeploymentInfo(collector: Collector): Collector = {
      val name = benchflowservices.collectorId(self.name, collector.name)
      val alias = testConfig.getAliasForService(self.name).get
      val aliasIp = env.getPublicIp(alias)

      //collector.environment.vars("constraint") = alias
      collector.environment.vars("constraint") = env.getHostname(alias)

      collector.copy(
        name = name,
        containerName = Some(ContainerName(s"${name}_${trial.getTrialId}")),
//        ports = collector.ports.map(p => Ports(Seq(s"$aliasIp:${p.ports.head}"))),
        ports = collector.ports.map(p => Ports(Seq(addIpToPorts(aliasIp, p.ports.head))))
//        net = Some(Network("bridge"))
      )
    }

    private def mountBoundServiceVolumes(collector: Collector): Collector = {

      collector.volumes.map[Collector] { someVolumes =>

        val mountVolumesIndex = someVolumes.volumes.indexOf("${BENCHFLOW_BOUNDSERVICE_VOLUMES}")
        if(mountVolumesIndex != -1) {
          collector.copy(
            volumes = self.volumes.map(_.volumes ++ someVolumes.volumes.patch(mountVolumesIndex, Nil, 1)) match {
              case Some(vols) => Some(Volumes(vols))
              case None => None
            }
          )
        }
        else collector
      }.get
    }

    private def resolveVolumesFrom(collector: Collector): Collector = {

      collector.volumesFrom.map[Collector] { someVolumesFrom =>

        val mountVolumesIndex = someVolumesFrom.volumes.map(_._1).indexOf("${BENCHFLOW_BOUNDSERVICE_VOLUMES}")
        if(mountVolumesIndex != -1) {

          collector.copy(
            volumesFrom = Some(VolumesFrom(
                (self.name, Some(ReadOnly)) :: someVolumesFrom.volumes.patch(mountVolumesIndex, Nil, 1).toList
              )
            )

          )

        } else collector

      }.getOrElse(collector)

    }


    private def resolveCollectorVariables(collector: Collector): Collector = {
//      println(self.name, collector.name)
      val newC = collector.copy(
        environment = Environment(
          collector.environment.vars.map {
            case (variableName, variableValue) =>
              variableName -> resolve(self, collector)(variableValue)
          }
        )
      )
//      println("finished")
      newC
    }

    override def apply: List[Service] = {

      val collectorDefinitions: Map[String, Collector] = {
        collectorsBindings.map {
          case (collectorName, binding) =>
            collectorName ->
              (deploymentDescriptorDefinition _
                andThen resolveDeploymentInfo
                andThen generateEnvVariables
                andThen resolveVolumesFrom
                //andThen mountBoundServiceVolumes
                andThen resolveCollectorVariables)(collectorName)
        }
      }

      (collectorDefinitions.values ++
      collectorDefinitions.values.flatMap { collector =>
        new MonitorResolver((self, collector), trial).apply
      }).toList

    }
  }

  /***
    * Contains the logic to resolve a BENCHFLOW_ENV variable
    *
    * @param variable the value of the variable after the BENCHFOW_ENV prefix
    */
  private class BenchFlowEnvVariable(variable: String) {
    def resolve: String = {
      env.getConfigYml.getVariable[String](s"BENCHFLOW_$variable")
    }
  }
  private object BenchFlowEnvVariable {
    val prefix = "(BENCHFLOW_ENV_)(.*)".r
  }

  /***
    * Contains the logic to resolve a BENCHFLOW_BENCHMARK_BOUNDSERVICE variable
    *
    * @param variable the value of the variable after the BENCHFOW_BENCHMARK_BOUNDSERVICE prefix
    */
  private class BenchFlowBoundServiceVariable(variable: String) {
    def resolve(boundService: Service): String = {
      variable match {
        case "IP" => env.getIp(testConfig.getAliasForService(boundService.name).get)
        case "PORT" => boundService.getPublicPort.get//.getPorts.get
        case "CONTAINER_NAME" => boundService.containerName.map(_.container_name).get
        case _ =>
          println(boundService.name, variable)
          throw new Exception(s"Unsupported bound service variable: $variable")
      }
    }
  }
  private object BenchFlowBoundServiceVariable {
    val prefix = "(BENCHFLOW_BENCHMARK_BOUNDSERVICE_)(.*)".r
  }

  /***
    * Contains the logic to resolve a BENCHFLOW_BENCHMARK_CONFIG variable
    *
    * @param variable the value of the variable after the BENCHFOW_BENCHMARK_CONFIG prefix
    */
  private class BenchFlowConfigVariable(variable: String) {
    def resolve(boundService: Service, benchFlowService: Service): String = {
      //val benchFlowServiceOriginalName = benchFlowService.name.split("\\.")(2)
      val benchFlowServiceOriginalName = CollectorType.getName(benchFlowService.name)
      testConfig.getBindingConfiguration(boundService.name, benchFlowServiceOriginalName)
                .flatMap(_.properties.get(variable)).get.toString
    }
  }
  private object BenchFlowConfigVariable {
    val prefix = "(BENCHFLOW_BENCHMARK_CONFIG_)(.*)".r
  }

  /***
    * Resolution strategy of BenchFlow variables in monitors
    */
  private trait MonitorResolutionStrategy extends VariablesResolutionStrategy[(Service, Collector, Monitor)] {

    private def resolveBenchFlowVar(source: (Service, Collector, Monitor))(benchFlowVar: String): (String, String) = {
      val service = source._1
      val collector = source._2
      val monitor = source._3

      (benchFlowVar, benchFlowVar match {
        case BenchFlowEnvVariable.prefix(prefix, name) =>
          new BenchFlowEnvVariable(name).resolve
        case BenchFlowBoundServiceVariable.prefix(prefix, name) =>
          new BenchFlowBoundServiceVariable(name).resolve(service)
        case BenchFlowConfigVariable.prefix(prefix, name) =>
          new BenchFlowConfigVariable(name).resolve(service, monitor)
      })
    }

    override def resolve(source: (Service, Collector, Monitor))(variable: String): String = {
      val resolvedVariables = variable.findBenchFlowVars.getOrElse(Seq()).map(resolveBenchFlowVar(source))
      resolvedVariables.foldLeft(variable) {
        case (result, (varKey, resolvedValue)) => result.replace(s"$${$varKey}", resolvedValue)
      }
    }
  }

  /***
    * Resolution strategy of BenchFlow variables in collectors
    */
  private trait CollectorResolutionStrategy extends VariablesResolutionStrategy[(Service, Collector)] {

    //returns a benchflow variable plus the resolved value for that variable
    private def resolveBenchFlowVar(source: (Service, Collector))(benchFlowVar: String): (String, String) = {
      val service = source._1
      val collector = source._2
      (benchFlowVar, benchFlowVar match {
        case BenchFlowEnvVariable.prefix(prefix, name) =>
          new BenchFlowEnvVariable(name).resolve
        case BenchFlowBoundServiceVariable.prefix(prefix, name) =>
          new BenchFlowBoundServiceVariable(name).resolve(service)
        case BenchFlowConfigVariable.prefix(prefix, name) =>
          new BenchFlowConfigVariable(name).resolve(service, collector)
      })
    }

    override def resolve(source: (Service, Collector))(variable: String): String = {
      val resolvedVariables = variable.findBenchFlowVars.getOrElse(Seq()).map(resolveBenchFlowVar(source))
      resolvedVariables.foldLeft(variable) {
        case (result, (varKey, resolvedValue)) => result.replace(s"$${$varKey}", resolvedValue)
      }
    }

  }

  //resolves all collectors for a service
  private def resolveCollectors(service: Service, trial: Trial): List[Service] = new CollectorResolver(service, trial).apply

  //adds constraint:node==alias, ip, ports, container name
  private def resolveDeploymentInfo(service: Service, trial: Trial): Service = {
    val containerName = s"${service.name}_${trial.getTrialId}"
    testConfig.getAliasForService(service.name) match {
      case Some(alias) =>
        service.environment.vars("constraint") = alias
        service.copy(
          containerName = Some(ContainerName(containerName)),
          ports = service.ports.map(p => Ports(Seq(env.getIp(alias) + ":" + p.ports.head)))
//          ports = service.ports.map(p => Ports(Seq(addIpToPorts(env.getIp(alias), p.ports.head))))
        )
      case None => throw new Exception(s"Can't resolve deployment info for service ${service.name}")
    }
  }

  private def resolveService(trial: Trial)(service: Service): List[Service] = {
    val updatedService = resolveDeploymentInfo(service, trial)
    updatedService :: resolveCollectors(updatedService, trial)
  }

  //public API, to get an updated deployment descriptor
  def resolveDeploymentDescriptor(deploymentDescriptor: DockerCompose, trial: Trial): DockerCompose = {

    //resolve variable across services
    new SiblingVariableResolver(deploymentDescriptor,env, testConfig)
      .resolve(trial)

    //generate deployment info from benchflow
    val resolvedServices = Map[String, Service](
                              deploymentDescriptor.services
                              .mapValues(resolveService(trial))
                              .values.flatten
                              .map(v => v.name -> v).toSeq: _*)

    deploymentDescriptor.copy(
      services = resolvedServices
    )
  }
}