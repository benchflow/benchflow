package cloud.benchflow.dsl.definition

import cloud.benchflow.dsl.definition.configuration.BenchFlowTestConfiguration
import cloud.benchflow.dsl.definition.datacollection.DataCollection
import cloud.benchflow.dsl.definition.sut.Sut
import cloud.benchflow.dsl.definition.version.Version
import cloud.benchflow.dsl.definition.workload.Workload

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 *         Created on 18/07/16.
 */

case class BenchFlowTest(
  version: Version,
  name: String,
  description: Option[String],
  configuration: BenchFlowTestConfiguration,
  sut: Sut,
  workload: Map[String, Workload],
  dataCollection: DataCollection)
