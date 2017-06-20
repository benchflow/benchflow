package cloud.benchflow.faban.client.responses;

import cloud.benchflow.faban.client.exceptions.IllegalRunStatusException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

;

/**
 * @author vincenzoferme
 */
public class RunInfo implements Response {

  private String description;
  private Result result;
  private Integer scale;
  private String metric;
  private RunStatus status;
  private Date date_time;
  private String submitter;
  private String tags;

  /**
   * Construct a Run Info
   *
   * @param runInfo the run info
   * @param runId the run id
   */
  public RunInfo(Document runInfo, RunId runId) {

    Elements description = runInfo.select("td#Description");
    this.description = description.text();

    handleResult(runInfo, runId);

    Elements scale = runInfo.select("td#Scale");
    this.scale = Integer.getInteger(scale.text());

    Elements metric = runInfo.select("td#Metric");
    this.metric = metric.text();

    Elements status = runInfo.select("td#Status");
    this.status = new RunStatus(status.text(),runId);

    Elements submitter = runInfo.select("td#Submitter");
    this.submitter = submitter.text();

    Elements tags = runInfo.select("td#Tags");
    this.tags = tags.text();

    //TODO - improve when this is supported by jsoup: https://stackoverflow.com/questions/5153986/css-selector-to-select-an-id-with-a-slash-in-the-id-name
    runInfo.html(runInfo.html().replace("id=\"Date/Time\"","id=\"DateTime\""));
    Elements date_time = runInfo.select("td#DateTime");
    //See http://developer.android.com/reference/java/text/SimpleDateFormat.html
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");

    try {
      this.date_time = df.parse(date_time.text());
    } catch (ParseException e) {

      //TODO: we need a logger in the faban client as well
      System.err.println(
          "Something went wrong while converting the date " + date_time.text() + " for run " + runId);
      e.printStackTrace();
      //We default to the current date, because it should anyway be very close to the retrieved one
      this.date_time = new Date();
    }

  }

  private void handleResult(Document runInfo, RunId runId) {
    Elements result = runInfo.select("td#Result");

    switch (result.text()) {
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
        throw new IllegalRunStatusException(
            "RunId " + runId + "returned illegal run info result " + result.text());
    }
  }

  /**
   * Possible result codes.
   */
  public enum Result {
    NA, NOT_AVAILABLE, UNKNOWN
  }

  @Override
  public String toString() {
    return "RunInfo{" +
        "description='" + description + '\'' +
        ", result=" + result +
        ", scale=" + scale +
        ", metric='" + metric + '\'' +
        ", status=" + status.getStatus() +
        ", date_time=" + date_time +
        ", submitter='" + submitter + '\'' +
        ", tags='" + tags + '\'' +
        '}';
  }

  public String getDescription() {
    return description;
  }

  public Result getResult() {
    return result;
  }

  public Integer getScale() {
    return scale;
  }

  public String getMetric() {
    return metric;
  }

  public RunStatus getStatus() {
    return status;
  }

  public Date getDate_time() { return date_time; }

  public String getSubmitter() {
    return submitter;
  }

  public String getTags() {
    return tags;
  }


}