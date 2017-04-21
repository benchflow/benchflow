package cloud.benchflow.experiment.sources.processors.drivers.operations.wfms

import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.experiment.sources.processors.DriverOperationsProcessor
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import cloud.benchflow.test.config.sut.wfms.{WfMSDriver, WfMSOperation}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 28/07/16.
  */
/** base class for a processor that generates operations for a wfms driver */
abstract class WfMSDriverOperationsProcessor[T <: WfMSOperation](benchFlowBenchmark: BenchFlowExperiment,
                                                                 driver: WfMSDriver,
                                                                 experimentId: String)(implicit env: DriversMakerEnv)
  extends DriverOperationsProcessor(benchFlowBenchmark, driver, experimentId)(env)
