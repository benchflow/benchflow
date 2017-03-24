package cloud.benchflow.dsl.definition

import cloud.benchflow.dsl.definition.configuration.Configuration
import cloud.benchflow.dsl.definition.datacollection.DataCollection
import cloud.benchflow.dsl.definition.sut.Sut
import cloud.benchflow.dsl.definition.workload.Workload

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 2017-03-24
  */
case class BenchFlowExperiment(version: String,
                               name: String,
                               description: String,
                               configuration: Configuration,
                               sut: Sut,
                               workload: Map[String, Workload],
                               dataCollection: Option[DataCollection])
