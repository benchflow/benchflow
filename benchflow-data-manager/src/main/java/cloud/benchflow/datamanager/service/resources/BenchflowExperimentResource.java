package cloud.benchflow.datamanager.service.resources;

import akka.util.Timeout;
import cloud.benchflow.datamanager.core.BackupManager;
import cloud.benchflow.datamanager.service.api.Job;
import cloud.benchflow.datamanager.service.api.JobStatus;
import cloud.benchflow.datamanager.service.constants.BenchFlowConstants;

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

@Path("/v1/users/{username}/tests/{testName}/{testNumber}/experiments")
@Produces(MediaType.APPLICATION_JSON)
public class BenchflowExperimentResource {
  private final BackupManager backupManager;

  public BenchflowExperimentResource(BackupManager backupManager) {
    this.backupManager = backupManager;
  }

  @Path("/{experimentNumber}/backup")
  @GET
  @Timed
  public Job backup(@PathParam("username") String username, @PathParam("testName") String testName,
      @PathParam("testNumber") int testNumber,
      @PathParam("experimentNumber") int experimentNumber) {
    String experimentId =
        BenchFlowConstants.getExperimentID(username, testName, testNumber, experimentNumber);

    Tuple2 result = backupManager.backupExperiment(experimentId);
    Long jobId = (Long) result._1();
    Long backupId = (Long) result._2();
    return new Job(jobId, backupId);
  }

  @Path("/{experimentNumber}/backup/{backupId}/restore")
  @GET
  @Timed
  public Job restore(@PathParam("username") String username, @PathParam("testName") String testName,
      @PathParam("testNumber") int testNumber, @PathParam("experimentNumber") int experimentNumber,
      @PathParam("backupId") long backupId) {
    long jobId = backupManager.recoverBackup(backupId);
    return new Job(jobId);
  }

  @Path("/{experimentNumber}/backup/{backupId}/status/{jobId}")
  @GET
  @Timed
  public JobStatus backupStatus(@PathParam("username") String username,
      @PathParam("testName") String testName, @PathParam("testNumber") int testNumber,
      @PathParam("experimentNumber") int experimentNumber, @PathParam("backupId") long backupId,
      @PathParam("jobId") long jobId) throws Exception {
    return status(jobId);
  }

  @Path("/{experimentNumber}/backup/{backupId}/restore/status/{jobId}")
  @GET
  @Timed
  public JobStatus restoreStatus(@PathParam("username") String username,
      @PathParam("testName") String testName, @PathParam("testNumber") int testNumber,
      @PathParam("experimentNumber") int experimentNumber, @PathParam("backupId") long backupId,
      @PathParam("jobId") long jobId) throws Exception {
    return status(jobId);
  }

  private JobStatus status(long jobId) throws Exception {
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
