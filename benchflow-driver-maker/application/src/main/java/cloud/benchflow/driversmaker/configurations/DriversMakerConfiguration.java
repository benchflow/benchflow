package cloud.benchflow.driversmaker.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 06/01/16.
 */
public class DriversMakerConfiguration extends Configuration {

//    @Valid
//    @NotNull
//    private FabanDefaults fabanDefaults = new FabanDefaults();

    @Valid
    @NotNull
    private BenchFlowEnvConfiguration benchFlowEnvConfiguration = new BenchFlowEnvConfiguration();

    @Valid
    @NotNull
    private MinioConfiguration minioConfiguration = new MinioConfiguration();

//    @JsonProperty("fabanDefaults")
//    public FabanDefaults getFabanDefaults() { return fabanDefaults; }
//
//    @JsonProperty("fabanDefaults")
//    public void setFabanDefaults(FabanDefaults fc) { this.fabanDefaults = fc; }

    @JsonProperty("benchflowEnv")
    public BenchFlowEnvConfiguration getBenchFlowEnvConfiguration() {
        return benchFlowEnvConfiguration;
    }

    @JsonProperty("benchflowEnv")
    public void setBenchFlowEnvConfiguration(BenchFlowEnvConfiguration benchFlowEnvConfiguration) {
        this.benchFlowEnvConfiguration = benchFlowEnvConfiguration;
    }

    @JsonProperty("minio")
    public MinioConfiguration getMinioConfiguration() {
        return minioConfiguration;
    }

    @JsonProperty("minio")
    public void setMinioConfiguration(MinioConfiguration minioConfiguration) {
        this.minioConfiguration = minioConfiguration;
    }
}
