package cloud.benchflow.experimentmanager.models;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.faban.client.responses.RunInfo;
import cloud.benchflow.faban.client.responses.RunInfo.Result;
import cloud.benchflow.faban.client.responses.RunStatus;
import cloud.benchflow.faban.client.responses.RunStatus.StatusCode;
import org.mongodb.morphia.annotations.Embedded;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-08-18
 */
@Embedded
public class FabanInfo {

  private String fabanRunID;
  private RunStatus.StatusCode fabanStatus;
  private RunInfo.Result fabanResult;
  private String fabanRunStatusURI;

  public FabanInfo() {
    // Empty constructor for MongoDB + Morphia
  }

  public String getFabanRunID() {
    return fabanRunID;
  }

  public void setFabanRunID(String fabanRunID) {
    this.fabanRunID = fabanRunID;
    this.fabanRunStatusURI = BenchFlowExperimentManagerApplication.getFabanManagerServiceAddress()
        + "/resultframe.jsp?runId=" + fabanRunID + "&result=summary.xml";
  }

  public StatusCode getFabanStatus() {
    return fabanStatus;
  }

  public void setFabanStatus(StatusCode fabanStatus) {
    this.fabanStatus = fabanStatus;
  }

  public Result getFabanResult() {
    return fabanResult;
  }

  public void setFabanResult(Result fabanResult) {
    this.fabanResult = fabanResult;
  }

  public String getFabanRunStatusURI() {
    return fabanRunStatusURI;
  }

}
