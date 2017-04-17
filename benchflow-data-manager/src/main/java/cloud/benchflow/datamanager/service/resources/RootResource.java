package cloud.benchflow.datamanager.service.resources;

import cloud.benchflow.datamanager.core.BackupManager;
import cloud.benchflow.datamanager.service.api.Backup;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class RootResource {
    private final BackupManager backupManager;

    public RootResource(BackupManager backupManager) {
       this.backupManager = backupManager;
    }

    @Path("/backup/{experiment-id}")
    @GET
    @Timed
    public Backup backup(@PathParam("experiment-id") String experimentId) {
        long backupId = backupManager.backupExperiment(experimentId);
        return new Backup(backupId);
    }

    @Path("/restore/{backup-id}")
    @GET
    @Timed
    public Backup restore(@PathParam("backup-id") long backupId) {
        backupManager.recoverBackup(backupId);
        return new Backup(backupId);
    }
}
