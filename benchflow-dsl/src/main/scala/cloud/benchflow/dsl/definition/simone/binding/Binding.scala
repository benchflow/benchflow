package cloud.benchflow.dsl.definition.simone.binding

import cloud.benchflow.dsl.definition.simone.properties.Properties

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 13.03.17.
 */
case class Binding(boundService: String, config: Option[Properties])
