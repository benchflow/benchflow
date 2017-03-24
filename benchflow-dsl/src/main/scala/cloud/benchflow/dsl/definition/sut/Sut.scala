package cloud.benchflow.dsl.definition.sut

import cloud.benchflow.dsl.definition.sut.configuration.SutConfiguration

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
case class Sut(
  name: String,
  version: Version,
  sutType: SutType,
  configuration: SutConfiguration,
  serviceConfiguration: Option[Any] // TODO - Map[String, ServiceConfiguration]
)

