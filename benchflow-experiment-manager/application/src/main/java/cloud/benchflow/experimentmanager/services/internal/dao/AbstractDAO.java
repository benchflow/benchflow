package cloud.benchflow.experimentmanager.services.internal.dao;

import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.models.TrialModel;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-07
 */
public class AbstractDAO {

  protected Datastore datastore;

  protected AbstractDAO(MongoClient mongoClient) {

    final Morphia morphia = new Morphia();

    // tell Morphia where to find your classes
    // can be called multiple times with different packages or classes
    morphia.map(BenchFlowExperimentModel.class);
    morphia.map(TrialModel.class);

    // create the Datastore
    // TODO - set-up mongo DB (http://mongodb.github.io/mongo-java-driver/2.13/getting-started/quick-tour/)
    // TODO - check about resilience and cache
    datastore = morphia.createDatastore(mongoClient, BenchFlowConstants.DB_NAME);
    datastore.ensureIndexes();
  }

  // used for testing
  public Datastore getDatastore() {
    return datastore;
  }
}
