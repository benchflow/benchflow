package cloud.benchflow.dsl.definition.configuration.goal

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
// TODO - read into pre-defined structure
// TODO - should we have one class per goal type??
case class Goal(goalType: String, // TODO - define type
                observation: Option[Any], // TODO - define type
                explorationSpace: Option[Any] // TODO - define type
               )
