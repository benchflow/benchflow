package cloud.benchflow.dsl.definition.binding

import cloud.benchflow.dsl.definition.properties.Properties

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 13.03.17.
  */
case class Binding(boundService: String, config: Option[Properties])
