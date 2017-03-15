package cloud.benchflow.dsl.definition.sut

import cloud.benchflow.dsl.definition.sut.configuration.SutConfiguration
import cloud.benchflow.dsl.definition.sut.serviceconfiguration.ServiceConfiguration

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
case class Sut(name: String,
               version: String, // TODO should be Version
               sutType: String, // TODO should be SutType
               configuration: SutConfiguration,
               serviceConfiguration: Option[Any] // TODO - Map[String, ServiceConfiguration
              )

