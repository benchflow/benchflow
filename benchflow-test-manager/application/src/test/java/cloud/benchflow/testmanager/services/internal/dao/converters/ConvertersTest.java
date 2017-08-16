package cloud.benchflow.testmanager.services.internal.dao.converters;

import cloud.benchflow.dsl.ExplorationSpaceAPI;
import cloud.benchflow.dsl.explorationspace.JavaCompatExplorationSpaceConverter.JavaCompatExplorationSpace;
import cloud.benchflow.dsl.explorationspace.JavaCompatExplorationSpaceConverter.JavaCompatExplorationSpaceDimensions;
import cloud.benchflow.testmanager.DockerComposeIT;
import cloud.benchflow.testmanager.helpers.constants.TestConstants;
import cloud.benchflow.testmanager.helpers.constants.TestFiles;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.User;
import cloud.benchflow.testmanager.models.explorationspace.MongoCompatibleExplorationSpace;
import cloud.benchflow.testmanager.models.explorationspace.MongoCompatibleExplorationSpaceDimensions;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-03
 */
public class ConvertersTest extends DockerComposeIT {

  private Datastore datastore;

  @Before
  public void setUp() throws Exception {

    Morphia morphia = new Morphia();
    morphia.map(User.class);
    morphia.map(BenchFlowTestModel.class);
    morphia.getMapper().getConverters()
        .addConverter(new OptionalConverter(morphia.getMapper().getConverters()));
    morphia.getMapper().getConverters().addConverter(new BytesConverter());

    datastore = morphia.createDatastore(mongoClient, "morphia");
    // TODO - to enable see issue https://github.com/benchflow/benchflow/issues/475
    //    datastore.ensureIndexes();
  }

  @After
  public void tearDown() throws Exception {
    datastore.delete(datastore.createQuery(User.class));
    datastore.delete(datastore.createQuery(BenchFlowTestModel.class));
  }

  @Test
  public void testStoringRetrievingExplorationSpace() throws Exception {

    User testUser = TestConstants.TEST_USER;

    datastore.save(testUser);

    final BenchFlowTestModel benchFlowTestModel =
        new BenchFlowTestModel(TestConstants.TEST_USER, TestConstants.VALID_TEST_NAME, 1);

    String testID = benchFlowTestModel.getId();

    datastore.save(benchFlowTestModel);

    //    ==========================

    final Query<BenchFlowTestModel> testModelQuery1 =
        datastore.createQuery(BenchFlowTestModel.class).field(BenchFlowTestModel.ID_FIELD_NAME)
            .equal(testID);

    final BenchFlowTestModel receivedTestModel1 = testModelQuery1.get();

    String testDefinitionString =
        IOUtils.toString(TestFiles.getTestExplorationOneAtATimeUsersMemoryEnvironmentInputStream(),
            StandardCharsets.UTF_8);

    JavaCompatExplorationSpace compatExplorationSpace =
        ExplorationSpaceAPI.explorationSpaceFromTestYaml(testDefinitionString);

    MongoCompatibleExplorationSpace mongoCompatibleExplorationSpace =
        new MongoCompatibleExplorationSpace(compatExplorationSpace);

    receivedTestModel1.getExplorationModel().setExplorationSpace(mongoCompatibleExplorationSpace);

    datastore.save(receivedTestModel1);

    //    ==========================

    final Query<BenchFlowTestModel> testModelQuery2 =
        datastore.createQuery(BenchFlowTestModel.class).field(BenchFlowTestModel.ID_FIELD_NAME)
            .equal(testID);

    final BenchFlowTestModel receivedTestModel2 = testModelQuery2.get();

    JavaCompatExplorationSpaceDimensions explorationSpaceDimensions =
        ExplorationSpaceAPI.explorationSpaceDimensionsFromTestYaml(testDefinitionString);

    MongoCompatibleExplorationSpaceDimensions mongoCompatibleExplorationSpaceDimensions =
        new MongoCompatibleExplorationSpaceDimensions(explorationSpaceDimensions);

    receivedTestModel2.getExplorationModel()
        .setExplorationSpaceDimensions(mongoCompatibleExplorationSpaceDimensions);

    datastore.save(receivedTestModel2);

    //    ==========================

    final Query<BenchFlowTestModel> testModelQuery3 =
        datastore.createQuery(BenchFlowTestModel.class).field(BenchFlowTestModel.ID_FIELD_NAME)
            .equal(testID);

    final BenchFlowTestModel receivedTestModel3 = testModelQuery3.get();

    JavaCompatExplorationSpace javaCompatExplorationSpace =
        receivedTestModel3.getExplorationModel().getExplorationSpace().toJavaCompat();

    Assert.assertEquals(16, javaCompatExplorationSpace.size().intValue());

    Assert.assertEquals(testID, receivedTestModel3.getId());

  }
}
