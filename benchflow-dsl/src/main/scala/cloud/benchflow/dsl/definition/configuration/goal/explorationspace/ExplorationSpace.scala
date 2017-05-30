package cloud.benchflow.dsl.definition.configuration.goal.explorationspace

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.service.ServiceExplorationSpace
import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.workload.WorkloadExplorationSpace

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-17
 */
case class ExplorationSpace(workload: Option[WorkloadExplorationSpace], services: Option[Map[String, ServiceExplorationSpace]])
