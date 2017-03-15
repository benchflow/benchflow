package cloud.benchflow.dsl.definition.sut.configuration

import cloud.benchflow.dsl.definition.sut.configuration.targetservice.TargetService
import cloud.benchflow.dsl.definition.sut.simone.Deploy

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */

// TODO - rename to configuration
case class SutConfiguration(targetService: TargetService,
                            deployment: Map[String, String]) {

  def getServiceDeployment(serviceName: String): Option[String] = deployment.get(serviceName)

}
