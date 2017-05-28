package cloud.benchflow.datamanager.service;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import cloud.benchflow.datamanager.core.BackupManager;
import cloud.benchflow.datamanager.core.backupstorage.BackupStorage;
import cloud.benchflow.datamanager.core.datarepository.cassandra.Cassandra;
import cloud.benchflow.datamanager.core.datarepository.filestorage.ExperimentFileStorage;
import cloud.benchflow.datamanager.service.configurations.DataManagerConfiguration;
import cloud.benchflow.datamanager.service.resources.BenchflowExperimentResource;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundle;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundleConfiguration;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class DataManagerApplication extends Application<DataManagerConfiguration> {

  public static void main(final String[] args) throws Exception {
    new DataManagerApplication().run(args);
  }

  @Override
  public String getName() {
    return "DataManager";
  }

  @Override
  public void initialize(final Bootstrap<DataManagerConfiguration> bootstrap) {
    // Dropwizard Template Config
    bootstrap.addBundle(new TemplateConfigBundle(
        new TemplateConfigBundleConfiguration().resourceIncludePath("/app")));
  }

  @Override
  public void run(final DataManagerConfiguration configuration, final Environment environment)
      throws Exception {

    ActorSystem system = ActorSystem.create();
    Materializer materializer = ActorMaterializer.create(system);

    Cassandra cassandra = configuration.getCassandraServiceFactory().build(system, materializer);
    BackupStorage googleDrive = configuration.getGoogleDriveServiceFactory().build();
    ExperimentFileStorage minio = configuration.getMinioServiceFactory().build();

    BackupManager backupManager =
        new BackupManager(cassandra, googleDrive, minio, system, materializer);

    final BenchflowExperimentResource resource = new BenchflowExperimentResource(backupManager);
    environment.jersey().register(resource);
  }
}
