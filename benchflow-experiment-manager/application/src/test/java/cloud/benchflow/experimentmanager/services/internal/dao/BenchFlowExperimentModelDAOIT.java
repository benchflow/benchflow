package cloud.benchflow.experimentmanager.services.internal.dao;

import cloud.benchflow.experimentmanager.DockerComposeIT;
import cloud.benchflow.experimentmanager.helpers.TestConstants;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-13
 */
public class BenchFlowExperimentModelDAOIT extends DockerComposeIT {

    private BenchFlowExperimentModelDAO experimentModelDAO;

    @Before
    public void setUp() throws Exception {

        MongoClient mongoClient = new MongoClient(MONGO_CONTAINER.getIp(), MONGO_CONTAINER.getExternalPort());

        experimentModelDAO = new BenchFlowExperimentModelDAO(mongoClient);

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void addExperiment() throws Exception {

        String experimentID = TestConstants.BENCHFLOW_EXPERIMENT_ID;

        experimentModelDAO.addExperiment(experimentID);

        BenchFlowExperimentModel experimentModel = experimentModelDAO.getExperimentModel(experimentID);

        assertEquals(experimentID, experimentModel.getId());

    }

}