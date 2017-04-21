package cloud.benchflow.driversmaker.modules;

import cloud.benchflow.driversmaker.configurations.DriversMakerConfiguration;
import cloud.benchflow.driversmaker.utils.env.ConfigYml;
import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.io.FileNotFoundException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 13/02/16.
 */
public class BenchFlowEnvModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    @Named("generationEnv")
    public DriversMakerEnv providesBenchFlowEnv(DriversMakerConfiguration dmc) throws FileNotFoundException {

        String configYmlPath = dmc.getBenchFlowEnvConfiguration().getConfigPath();
        String bfServicesPath = dmc.getBenchFlowEnvConfiguration().getBenchFlowServicesPath();
        String generationResourcesPath = dmc.getBenchFlowEnvConfiguration().getGenerationResourcesPath();
        String privatePort = dmc.getBenchFlowEnvConfiguration().getPrivatePort();

        ConfigYml benv = new ConfigYml(configYmlPath);

        return new DriversMakerEnv(benv, bfServicesPath, generationResourcesPath, privatePort);
    }

}
