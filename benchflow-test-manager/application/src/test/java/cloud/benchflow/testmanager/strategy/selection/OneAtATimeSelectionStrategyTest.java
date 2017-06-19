package cloud.benchflow.testmanager.strategy.selection;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-27
 */
public class OneAtATimeSelectionStrategyTest {

  //  private MinioService minioMock = Mockito.mock(MinioService.class);
  //  private ExplorationModelDAO explorationModelDAOMock = Mockito.mock(ExplorationModelDAO.class);
  //  private BenchFlowTestModelDAO testModelDAOMock = Mockito.mock(BenchFlowTestModelDAO.class);
  //
  //  private OneAtATimeSelectionStrategy oneAtATimeSelectionStrategy;

  //  @Before
  //  public void setUp() throws Exception {
  //
  //    oneAtATimeSelectionStrategy =
  //        new OneAtATimeSelectionStrategy(minioMock, explorationModelDAOMock);
  //  }

  //  @Test
  //  public void selectNextExperiment() throws Exception {
  //
  //    String testID = TestConstants.VALID_TEST_ID;
  //
  //    String expectedNumUsers = "5";
  //
  //    Mockito.doReturn(TestFiles.getTestExplorationOneAtATimeMultipleInputStream()).when(minioMock)
  //        .getTestDefinition(testID);
  //
  //    String testYaml = IOUtils.toString(TestFiles.getTestExplorationOneAtATimeMultipleInputStream(),
  //        StandardCharsets.UTF_8);
  //
  //    BenchFlowTest test = BenchFlowDSL.testFromYaml(testYaml);
  //
  //    // TODO - should be implemented. Currently commented out since high level case is
  //     covered in TestTaskSchedulerIT
  //
  //        List<Integer> selectionStrategy = StartTask.generateExplorationSpace(test);
  //
  //        Mockito.doReturn(selectionStrategy).when(explorationModelDAOMock)
  //            .getExecutedExplorationPointIndices(testID);
  //
  //        Set<Long> experimentNumbers = new HashSet<>();
  //        // ensure that experiment is available in DB
  //        experimentNumbers.add(0L);
  //
  //        Mockito.doReturn(experimentNumbers).when(testModelDAOMock).getExperimentNumbers(testID);
  //
  //        String experimentYaml = oneAtATimeSelectionStrategy.selectNextExperiment(testID);
  //
  //        Assert.assertNotNull(experimentYaml);
  //        Assert.assertTrue(experimentYaml.contains("users: " + expectedNumUsers));
  //
  //        // run the next experiment
  //        // make sure input stream has not been read already
  //        Mockito.doReturn(TestFiles.getTestExplorationOneAtATimeMultipleInputStream()).when(minioMock)
  //            .getTestDefinition(testID);
  //
  //        experimentNumbers.add(1L);
  //
  //        experimentYaml = oneAtATimeSelectionStrategy.selectNextExperiment(testID);
  //
  //        expectedNumUsers = "10";
  //
  //        Assert.assertNotNull(experimentYaml);
  //        Assert.assertTrue(experimentYaml.contains("users: " + expectedNumUsers));
  //  }
}
