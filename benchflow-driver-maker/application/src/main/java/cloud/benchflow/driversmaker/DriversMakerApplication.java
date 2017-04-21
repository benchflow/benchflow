package cloud.benchflow.driversmaker;

import cloud.benchflow.driversmaker.configurations.DriversMakerConfiguration;
import cloud.benchflow.driversmaker.modules.BenchFlowEnvModule;
import cloud.benchflow.driversmaker.modules.MinioModule;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundle;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import ru.vyarus.dropwizard.guice.GuiceBundle;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 06/01/16.
 */
public class DriversMakerApplication extends Application<DriversMakerConfiguration> {

    public static void main(String[] args) throws Exception {
        new DriversMakerApplication().run(args);
    }

    @Override
    public String getName() {
        return "drivers-maker";
    }

    @Override
    public void initialize(Bootstrap<DriversMakerConfiguration> bootstrap) {
        bootstrap.addBundle(new TemplateConfigBundle());

        GuiceBundle<DriversMakerConfiguration> guiceBundle =
                GuiceBundle.<DriversMakerConfiguration>builder()
                        .enableAutoConfig("cloud.benchflow.driversmaker")
                        .modules(new BenchFlowEnvModule(),
                                 new MinioModule())
//                                 new BenchFlowConfigConverterModule())
                        .build();

        bootstrap.addBundle(guiceBundle);
    }

    @Override
    public void run(DriversMakerConfiguration driversMakerConfiguration, Environment environment) throws Exception {
        environment.jersey().register(MultiPartFeature.class);
    }
}
