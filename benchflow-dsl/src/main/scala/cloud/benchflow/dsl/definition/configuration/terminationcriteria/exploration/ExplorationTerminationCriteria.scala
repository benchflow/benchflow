package cloud.benchflow.dsl.definition.configuration.terminationcriteria.exploration

import cloud.benchflow.dsl.definition.configuration.terminationcriteria.exploration.explorationtype.ExplorationType.ExplorationType
import cloud.benchflow.dsl.definition.types.percent.Percent
import cloud.benchflow.dsl.definition.types.time.Time

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-22
 */
case class ExplorationTerminationCriteria(
  explorationType: List[ExplorationType],
  number: Option[Int],
  maxTime: Option[Time],
  meanRelativeError: Percent,
  maxFailed: Option[Percent])
