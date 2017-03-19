package cloud.benchflow.dsl.definition

import cloud.benchflow.dsl.definition.configuration.Configuration
import cloud.benchflow.dsl.definition.datacollection.DataCollection
import cloud.benchflow.dsl.definition.sut.Sut
import cloud.benchflow.dsl.definition.workload.Workload

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  * @author Jesper Findahl (jesper.findahl@usi.ch)
  *
  *         Created on 18/07/16.
  */

case class BenchFlowTest(version: String,
                         name: String,
                         description: String,
                         configuration: Configuration,
                         sut: Sut,
                         workload: Map[String, Workload],
                         dataCollection: Option[DataCollection]
                        )

//case class BenchFlowTest(name: String,
//                         description: String,
//                         sut: Sut,
//                         trials: TotalTrials,
//                         goal: Goal,
//                         drivers: Seq[Driver[_ <: Operation]],
//                         loadFunction: LoadFunction,
//                         properties: Option[Properties],
//                         sutConfiguration: SutConfiguration)
