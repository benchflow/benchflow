package cloud.benchflow.experiment.harness;

import cloud.benchflow.driversmaker.generation.BenchFlowBenchmark;

import java.util.logging.Logger;

public class HttpBenchmark extends BenchFlowBenchmark {

    private static Logger logger = Logger.getLogger(HttpBenchmark.class.getName());

    @Override
    protected void initialize() throws Exception {
        super.initialize();
    }

}