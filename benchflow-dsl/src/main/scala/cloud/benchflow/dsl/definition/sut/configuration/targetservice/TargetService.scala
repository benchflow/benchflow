package cloud.benchflow.dsl.definition.sut.configuration.targetservice

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
case class TargetService(name: String, endpoint: String, sutReadyLogCheck: Option[String])