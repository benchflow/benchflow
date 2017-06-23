package cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.experiment.criteriatype.CriteriaType

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
case class ExperimentTerminationCriteria(
  criteriaType: CriteriaType,
  number: Int)
