package cloud.benchflow.dsl.definition.simone

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 10.03.17.
 */
//TODO: if needed, improve types for explored and observed
//will probably need to implement a type for each observable metric
case class GoalOld(
  goalType: GoalType,
  params: Seq[ParameterDefinition[_]],
  explored: Map[String, Seq[String]],
  observed: Option[Map[String, Seq[String]]])
