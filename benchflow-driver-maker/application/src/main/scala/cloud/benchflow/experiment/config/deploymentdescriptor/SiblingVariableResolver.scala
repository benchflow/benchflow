package cloud.benchflow.experiment.config.deploymentdescriptor

import cloud.benchflow.driversmaker.requests.Trial
import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv

import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import cloud.benchflow.test.deployment.docker.compose.DockerCompose
import cloud.benchflow.test.deployment.docker.service.Service
import cloud.benchflow.experiment.config.BenchFlowEnvString

import org.jgrapht.alg.CycleDetector
import org.jgrapht.graph.{DefaultEdge, DefaultDirectedGraph}
import org.jgrapht.traverse.TopologicalOrderIterator

import scala.collection.JavaConverters._

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 09/07/16.
  */
class SiblingVariableResolver(val deploymentDescriptor: DockerCompose,
                              val env: DriversMakerEnv,
                              val testConfig: BenchFlowExperiment) {

  import SiblingVariableResolver._

  private abstract class DependencyVariable(val name: String,
                                            val service: String) {
    override def toString = s"($name, $service)"
  }

  //a benchflow variable (e.g., to get IP and PORT)
  private case class BenchFlowDependencyVariable(override val name: String,
                                                 override val service: String)
    extends DependencyVariable(name, service)

  //a "right side of the assignment" variable
  private case class RightSideVariable(override val name: String,
                                       override val service: String)
    extends DependencyVariable(name, service)

  private val dependencyGraph = new DefaultDirectedGraph[DependencyVariable, DefaultEdge](classOf[DefaultEdge])

  private def removeBenchFlowPrefix(benchFlowVar: String) = {
    benchFlowVar match {
      case benchFlowPrefixRegex(prefix, varName) => varName
      case _ => benchFlowVar //should never happen
    }
  }


  //inspects a service environment to find dependencies to other services environments
  def inspectDependencies(service: Service): Unit = {
    val boh = for {
      variable  <- service.environment.vars
      bflowVars <- new BenchFlowEnvString(variable._2).findBenchFlowVars
    }
    yield (variable, bflowVars.map(removeBenchFlowPrefix))

    boh.foreach {
      case ((varName, completeVarValue), bflowVars) =>
        val rightNode = RightSideVariable(varName, service.name)
        dependencyGraph.addVertex(rightNode)
        bflowVars.map(bflowVar => {
          bflowVar match {
            case ipRegex(serviceName) => BenchFlowDependencyVariable("IP", serviceName)
            case portRegex(serviceName) => BenchFlowDependencyVariable("PORT", serviceName)
            case arbitraryVarRegex(serviceName, arbitraryVarName) =>
              RightSideVariable(arbitraryVarName, serviceName)
          }
        })
        .foreach(bfv => {
          dependencyGraph.addVertex(bfv)
          dependencyGraph.addEdge(bfv, rightNode)
        })
    }
  }


  def resolveVariableValue(variable: DependencyVariable): String = {
    variable.name match {
      case "IP" =>
        //resolve value from service ip
        val serverAlias = testConfig.getAliasForService(variable.service).get
        env.getIp(serverAlias)
      case "PORT" =>
        //resolve value from service port
        deploymentDescriptor.services.get(variable.service)
            .flatMap(_.getPublicPort).get
      case arbitrary =>
        //get arbitrary from service environment
        deploymentDescriptor.services.get(variable.service)
          .flatMap(_.environment.vars.get(arbitrary)).get
    }
  }


  def updateVariableValue(toReplace: DependencyVariable, value: String, target: DependencyVariable): Unit = {
    val targetService = deploymentDescriptor.services.get(target.service).get
    val oldVariableValue = targetService.environment.vars(target.name)
    targetService.environment.vars(target.name) = oldVariableValue.replace(
      s"$${$benchFlowPrefix${toReplace.service}_${toReplace.name}}", value)
  }


  def resolve(trial: Trial): Unit = {

    deploymentDescriptor.services.values.foreach(inspectDependencies)

    val top = new TopologicalOrderIterator[DependencyVariable, DefaultEdge](dependencyGraph)
    val cycleDetector = new CycleDetector[DependencyVariable, DefaultEdge](dependencyGraph)

    if (cycleDetector.detectCycles) {
      throw new Exception(
        s"""
          |BenchFlow has detected a circular dependency in the variables defined in
          |the deployment descriptor for experiment ${trial.getExperimentId}.
          |BenchFlow currently doesn't support circular dependency resolution.
          |Remove them and submit a new test.
          |Variables involved: ${cycleDetector.findCycles.asScala.map(_.name)}
        """.stripMargin
      )
    }

    while (top.hasNext) {
      val v = top.next

      dependencyGraph.edgesOf(v)
        .asScala.foreach(e => {

        val source = dependencyGraph.getEdgeSource(e)
        val target = dependencyGraph.getEdgeTarget(e)

        if(v == source) {
          //get value of source and replace it in target
          val resolvedValue = resolveVariableValue(source)
          updateVariableValue(source, resolvedValue, target)
          //println(
          //  s"""
          //    |Updated ${source.name} with value $resolvedValue in
          //    |variable ${target.name} of service ${target.service}
          // """.stripMargin)
        }

      })

    }
  }
}
object SiblingVariableResolver {

  val benchFlowPrefix = "BENCHFLOW_"
  val benchFlowPrefixRegex = s"($benchFlowPrefix)(.+)".r
  val ipRegex = "(.+)_IP".r
  val portRegex = "(.+)_PORT".r
  val arbitraryVarRegex = "(.+?)_(.+)".r

}