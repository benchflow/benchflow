package cloud.benchflow.experimentmanager.resources;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER_REGEX;

import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.helpers.data.BenchFlowData;
import cloud.benchflow.experimentmanager.scheduler.ExperimentTaskScheduler;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import javax.ws.rs.WebApplicationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-13
 */
public class BenchFlowExperimentResourceTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private MinioService minioMock = Mockito.mock(MinioService.class);
  private BenchFlowExperimentModelDAO experimentModelDAOMock =
      Mockito.mock(BenchFlowExperimentModelDAO.class);
  private ExperimentTaskScheduler experimentTaskSchedulerMock =
      Mockito.mock(ExperimentTaskScheduler.class);

  private BenchFlowExperimentResource experimentResource;

  @Before
  public void setUp() throws Exception {
    experimentResource = new BenchFlowExperimentResource(minioMock, experimentModelDAOMock,
        experimentTaskSchedulerMock);
  }

  @Test
  public void validRequest() throws Exception {

    String experimentID = BenchFlowData.VALID_EXPERIMENT_ID_1_TRIAL;

    Mockito.doReturn(true).when(minioMock).isValidExperimentID(experimentID);

    String[] experimentIDArray = experimentID.split(MODEL_ID_DELIMITER_REGEX);
    String username = experimentIDArray[0];
    String testName = experimentIDArray[1];
    int testNumber = Integer.parseInt(experimentIDArray[2]);
    int experimentNumber = Integer.parseInt(experimentIDArray[3]);

    experimentResource.runBenchFlowExperiment(username, testName, testNumber, experimentNumber);

    Mockito.verify(experimentModelDAOMock, Mockito.times(1)).addExperiment(experimentID);

    Mockito.verify(minioMock, Mockito.times(1)).isValidExperimentID(experimentID);
  }

  @Test
  public void invalidExperimentID() throws Exception {

    String experimentID = BenchFlowData.INVALID_EXPERIMENT_ID;

    Mockito.doReturn(false).when(minioMock).isValidExperimentID(experimentID);

    String[] experimentIDArray = experimentID.split(MODEL_ID_DELIMITER_REGEX);
    String username = experimentIDArray[0];
    String testName = experimentIDArray[1];
    int testNumber = Integer.parseInt(experimentIDArray[2]);
    int experimentNumber = Integer.parseInt(experimentIDArray[3]);

    exception.expect(WebApplicationException.class);
    exception.expectMessage(BenchFlowConstants.INVALID_EXPERIMENT_ID_MESSAGE);

    experimentResource.runBenchFlowExperiment(username, testName, testNumber, experimentNumber);

    Mockito.verify(experimentTaskSchedulerMock, Mockito.times(0))
        .handleStartingExperiment(experimentID);
    Mockito.verify(minioMock, Mockito.times(1)).isValidExperimentID(experimentID);
  }


}
