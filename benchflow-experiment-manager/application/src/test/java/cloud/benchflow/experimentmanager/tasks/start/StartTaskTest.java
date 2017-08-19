package cloud.benchflow.experimentmanager.tasks.start;

import cloud.benchflow.experimentmanager.exceptions.BenchMarkDeploymentException;
import cloud.benchflow.experimentmanager.exceptions.BenchmarkGenerationException;
import cloud.benchflow.experimentmanager.helpers.data.BenchFlowData;
import cloud.benchflow.experimentmanager.helpers.data.MinioTestData;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.external.faban.FabanManagerService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-08-19
 */
public class StartTaskTest {

  private BenchFlowExperimentModelDAO experimentModelDAOMock =
      Mockito.mock(BenchFlowExperimentModelDAO.class);
  private MinioService minioServiceMock = Mockito.mock(MinioService.class);
  private FabanManagerService fabanManagerServiceMock = Mockito.mock(FabanManagerService.class);
  private DriversMakerService driversMakerServiceMock = Mockito.mock(DriversMakerService.class);

  private String experimentID = BenchFlowData.VALID_EXPERIMENT_ID_1_TRIAL;

  @Before
  public void setUp() throws Exception {

    Mockito.doReturn(MinioTestData.getExperiment1TrialDefinition()).when(minioServiceMock)
        .getExperimentDefinition(experimentID);

    Mockito.doNothing().when(experimentModelDAOMock).setNumTrials(Mockito.anyString(),
        Mockito.anyInt());

  }

  @Test
  public void deploySuccessTest() throws Exception {

    // benchmark generation successful
    Mockito.doNothing().when(driversMakerServiceMock).generateBenchmark(Mockito.anyString(),
        Mockito.anyLong(), Mockito.anyInt());

    // deployment successful
    Mockito.doNothing().when(fabanManagerServiceMock).deployExperimentToFaban(Mockito.anyString(),
        Mockito.anyString(), Mockito.anyLong());

    StartTask startTask = new StartTask(experimentID, experimentModelDAOMock, minioServiceMock,
        fabanManagerServiceMock, driversMakerServiceMock);

    Boolean result = startTask.call();

    Assert.assertTrue(result);


  }

  @Test
  public void deployBenchMarkGenerationFailureTest() throws Exception {

    // benchmark generation exception
    Mockito.doThrow(BenchmarkGenerationException.class).when(driversMakerServiceMock)
        .generateBenchmark(Mockito.anyString(), Mockito.anyLong(), Mockito.anyInt());

    // deployment successful
    Mockito.doNothing().when(fabanManagerServiceMock).deployExperimentToFaban(Mockito.anyString(),
        Mockito.anyString(), Mockito.anyLong());

    StartTask startTask = new StartTask(experimentID, experimentModelDAOMock, minioServiceMock,
        fabanManagerServiceMock, driversMakerServiceMock);

    Boolean result = startTask.call();

    Assert.assertFalse(result);


  }

  @Test
  public void deployFailureTest() throws Exception {

    // benchmark generation exception
    Mockito.doNothing().when(driversMakerServiceMock).generateBenchmark(Mockito.anyString(),
        Mockito.anyLong(), Mockito.anyInt());

    // deployment successful
    Mockito.doThrow(BenchMarkDeploymentException.class).when(fabanManagerServiceMock)
        .deployExperimentToFaban(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong());

    StartTask startTask = new StartTask(experimentID, experimentModelDAOMock, minioServiceMock,
        fabanManagerServiceMock, driversMakerServiceMock);

    Boolean result = startTask.call();

    Assert.assertFalse(result);


  }
}
