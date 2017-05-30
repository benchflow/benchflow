package cloud.benchflow.dsl.definition.configuration.goal.explorationspace.service

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.service.resources.Resources

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-22
 */
case class ServiceExplorationSpace(resources: Option[Resources], environment: Option[Map[String, List[String]]])
