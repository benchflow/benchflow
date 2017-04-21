package cloud.benchflow.datamanager.service;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import cloud.benchflow.datamanager.core.BackupManager;
import cloud.benchflow.datamanager.core.backupstorage.BackupStorage;
import cloud.benchflow.datamanager.core.backupstorage.GoogleDriveFromConfig;
import cloud.benchflow.datamanager.core.datarepository.cassandra.Cassandra;
import cloud.benchflow.datamanager.core.datarepository.cassandra.CassandraFromConfig;
import cloud.benchflow.datamanager.core.datarepository.filestorage.ExperimentFileStorage;
import cloud.benchflow.datamanager.core.datarepository.objectstorage.MinioFromConfig;
import cloud.benchflow.datamanager.service.resources.RootResource;
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
    // TODO: application initialization
  }

  @Override
  public void run(final DataManagerConfiguration configuration, final Environment environment) {

    ActorSystem system = ActorSystem.create();
    Materializer materializer = ActorMaterializer.create(system);

    Cassandra cassandra = new CassandraFromConfig(system, materializer);
    BackupStorage googleDrive = new GoogleDriveFromConfig();
    ExperimentFileStorage minio = new MinioFromConfig();

    BackupManager backupManager =
        new BackupManager(cassandra, googleDrive, minio, system, materializer);

    final RootResource resource = new RootResource(backupManager);
    environment.jersey().register(resource);
  }
}
