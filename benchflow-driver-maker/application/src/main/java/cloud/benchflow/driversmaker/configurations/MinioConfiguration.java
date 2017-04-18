package cloud.benchflow.driversmaker.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 11/05/16.
 */
public class MinioConfiguration {

    @NotNull
    private String address;

    @JsonProperty("address")
    public String getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(String address) {
        this.address = address;
    }

}
