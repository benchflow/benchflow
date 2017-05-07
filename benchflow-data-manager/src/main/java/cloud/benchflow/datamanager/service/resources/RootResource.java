package cloud.benchflow.datamanager.service.resources;

import akka.util.Timeout;
import cloud.benchflow.datamanager.core.BackupManager;
import cloud.benchflow.datamanager.service.api.Job;
import cloud.benchflow.datamanager.service.api.JobStatus;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import scala.Option;
import scala.Tuple2;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

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
  public Job backup(@PathParam("experiment-id") String experimentId) {
    Tuple2 result = backupManager.backupExperiment(experimentId);
    Long jobId = (Long) result._1();
    Long backupId = (Long) result._2();
    return new Job(jobId, backupId);
  }

  @Path("/restore/{backup-id}")
  @GET
  @Timed
  public Job restore(@PathParam("backup-id") long backupId) {
    long jobId = backupManager.recoverBackup(backupId);
    return new Job(jobId);
  }

  @Path("/status/{job-id}")
  @GET
  @Timed
  public JobStatus backup(@PathParam("job-id") long jobId) throws Exception {
    Timeout timeout = new Timeout(Duration.create(5, "seconds"));
    Option<Tuple2<Object, Object>> result =
        Await.result(backupManager.getStatus(jobId), timeout.duration());
    if (result.isDefined()) {
      Integer step = (Integer) result.get()._1();
      Boolean finished = (Boolean) result.get()._2();
      return new JobStatus(step, finished);
    } else {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }
}
