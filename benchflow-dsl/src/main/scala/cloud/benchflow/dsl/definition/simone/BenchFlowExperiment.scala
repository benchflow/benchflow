package cloud.benchflow.dsl.definition.simone

import cloud.benchflow.dsl.definition._
import cloud.benchflow.dsl.definition.simone.properties.Properties
import cloud.benchflow.dsl.definition.sut.Sut
import cloud.benchflow.dsl.definition.sut.configuration.SutConfiguration

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 11/02/16.
 */
case class Users(users: Int)
case class BenchFlowExperiment(
    name: String,
    description: String,
    sut: Sut,
    users: Users,
    drivers: Seq[Driver[_ <: Operation]],
    trials: TotalTrials,
    execution: LoadFunction,
    properties: Properties,
    sutConfiguration: SutConfiguration
) {
  def getAliasForService(serviceName: String): Option[String] = sutConfiguration.deployment.get(serviceName)
  // TODO - move me
  //  def getBindingsForService(serviceName: String) = sutConfiguration.bfConfig.bindings(serviceName)
  //  def getBindingConfiguration(from: String, to: String): Option[Properties] =
  //    sutConfiguration.bfConfig.bindings(from).find(b => b.boundService == to).flatMap(_.config)
}
object BenchFlowExperiment
//{

  //  def fromYaml(yaml: String): BenchFlowExperiment = {
  //    yaml.stripMargin.parseYaml.convertTo[BenchFlowExperiment]
  //  }
  //
  //  def toYaml(be: BenchFlowExperiment): String = be.toYaml.prettyPrint

//}

