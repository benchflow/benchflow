package cloud.benchflow.driversmaker.configurations;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 06/01/16.
 */
public class FabanDefaults {

    @NotEmpty
    private String javaHome;

    @NotEmpty
    private String javaOpts;


    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getJavaOpts() {
        return javaOpts;
    }

    public void setJavaOpts(String javaOpts) {
        this.javaOpts = javaOpts;
    }
}
