package cloud.benchflow.testmanager.services.internal.dao;

import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.BenchFlowTestNumber;
import cloud.benchflow.testmanager.models.ExplorationModel;
import cloud.benchflow.testmanager.models.User;
import cloud.benchflow.testmanager.services.internal.dao.converters.OptionalConverter;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-27
 */
public abstract class DAO {

  protected final Datastore datastore;

  protected DAO(MongoClient mongoClient) {

    final Morphia morphia = new Morphia();

    // tell Morphia where to find your classes
    // can be called multiple times with different packages or classes
    morphia.map(BenchFlowTestModel.class);
    morphia.map(BenchFlowTestNumber.class);
    morphia.map(User.class);
    morphia.map(ExplorationModel.class);

    // add custom mappers
    morphia.getMapper().getConverters()
        .addConverter(new OptionalConverter(morphia.getMapper().getConverters()));

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
