package cloud.benchflow.dsl.definition.datacollection.serverside.collector

import cloud.benchflow.dsl.definition.datacollection.serverside.collector.environment.Environment

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 16.03.17.
 */
abstract class Collector
case class CollectorSingle(collector: String) extends Collector
case class CollectorMultiple(collectors: Seq[String]) extends Collector
case class CollectorMultipleEnvironment(collectors: Map[String, Environment]) extends Collector
