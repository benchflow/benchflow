package cloud.benchflow.faban.client.responses;

import cloud.benchflow.faban.client.exceptions.IllegalRunInfoResultException;
import cloud.benchflow.faban.client.exceptions.IllegalRunStatusException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * @author vincenzoferme.
 */
public class RunInfo implements Response {

  //Most of the types could be N/A, that is why most of them are treated as String
  //See: https://github.com/akara/faban/blob/master/harness/src/com/sun/faban/harness/webclient/Results.java#L382
  private String description;
  private Result result;
  private String scale;
  private String metric;
  private RunStatus status;
  private Date dateTime;
  private String submitter;
  private String tags;

  /**
   * Construct a Run Info.
   *
   * @param runInfo the run info
   * @param runId the run id
   */
  public RunInfo(Document runInfo, RunId runId)
      throws IllegalRunInfoResultException, IllegalRunStatusException {

    Elements description = runInfo.select("td#Description");
    this.description = description.text();

    handleResult(runInfo, runId);

    Elements scale = runInfo.select("td#Scale");
    this.scale = scale.text();

    Elements metric = runInfo.select("td#Metric");
    this.metric = metric.text();

    Elements status = runInfo.select("td#Status");
    this.status = new RunStatus(status.text(), runId);

    Elements submitter = runInfo.select("td#Submitter");
    this.submitter = submitter.text();

    Elements tags = runInfo.select("td#Tags");
    this.tags = tags.text();

    handleDateTime(runInfo, runId);


  }

  private void handleDateTime(Document runInfo, RunId runId) {
    //TODO - improve when this is supported by jsoup: https://stackoverflow.com/questions/5153986/css-selector-to-select-an-id-with-a-slash-in-the-id-name
    runInfo.html(runInfo.html().replace("id=\"Date/Time\"", "id=\"DateTime\""));
    Elements dateTime = runInfo.select("td#DateTime");
    //See http://developer.android.com/reference/java/text/SimpleDateFormat.html
    //See https://github.com/akara/faban/blob/master/harness/src/com/sun/faban/harness/webclient/Results.java#L410
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");

    //See https://github.com/akara/faban/blob/master/harness/src/com/sun/faban/harness/webclient/Results.java#L412
    if (!dateTime.text().equals("N/A")) {
      try {
        this.dateTime = df.parse(dateTime.text());
      } catch (ParseException e) {

        //TODO: we need a logger in the faban client as well
        System.err.println("Something went wrong while converting the date " + dateTime.text()
            + " for run " + runId);
        e.printStackTrace();
        //We default to the current date, because it should anyway be very close to the retrieved one
        this.dateTime = new Date();
      }
    } else {
      //We default to the current date, because it should anyway be very close to the retrieved one
      this.dateTime = new Date();
    }
  }

  private void handleResult(Document runInfo, RunId runId) throws IllegalRunInfoResultException {
    Elements result = runInfo.select("td#Result");

    switch (result.text()) {
      case "PASSED":
        this.result = Result.PASSED;
        break;
      case "FAILED":
        this.result = Result.FAILED;
        break;
      case "N/A":
        this.result = Result.NA;
        break;
      case "NOT_AVAILABLE":
        this.result = Result.NOT_AVAILABLE;
        break;
      case "UNKNOWN":
        this.result = Result.UNKNOWN;
        break;
      default:
        throw new IllegalRunInfoResultException(
            "RunId " + runId + "returned illegal run info result " + result.text(), result.text());
    }
  }

  @Override
  public String toString() {
    return "RunInfo{" + "description='" + description + '\'' + ", result=" + result + ", scale="
        + scale + ", metric='" + metric + '\'' + ", status=" + status.getStatus() + ", dateTime="
        + dateTime + ", submitter='" + submitter + '\'' + ", tags='" + tags + '\'' + '}';
  }

  public String getDescription() {
    return description;
  }

  public Result getResult() {
    return result;
  }

  public String getScale() {
    return scale;
  }

  public String getMetric() {
    return metric;
  }

  public RunStatus getStatus() {
    return status;
  }

  public Date getDateTime() {
    return dateTime;
  }

  public String getSubmitter() {
    return submitter;
  }

  public String getTags() {
    return tags;
  }

  /**
   * Possible result codes.
   *
   * <p>Sources: - All possible values assigned to the result variable in:
   * https://github.com/akara/faban/blob/master/harness/src/com/sun/faban/harness/webclient/RunResult.java
   * - All possible values assigned to the runInfo[2] variable in: https://github.com/akara/faban/blob/master/harness/src/com/sun/faban/harness/webclient/Results.java
   */
  public enum Result {
    PASSED, FAILED, NA, NOT_AVAILABLE, UNKNOWN
  }


}
