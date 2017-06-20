package cloud.benchflow.faban.test.client;

import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.configurations.FabanClientConfig;
import cloud.benchflow.faban.client.configurations.FabanClientConfigImpl;
import cloud.benchflow.faban.client.exceptions.ConfigFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.JarFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.DeployStatus;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 26/10/15.
 */
public class Main {

  public static void main(String[] args)
      throws IOException, URISyntaxException, RunIdNotFoundException {

    //get an instance of faban client
    FabanClientConfig fcprova = new FabanClientConfigImpl("deployer", "adminadmin", new URI(""));

    FabanClient client = new FabanClient().withConfig(fcprova);
    Path bm = Paths.get("./src/test/resources/foofoofoo.jar");
    Path configFile = Paths.get("./src/test/resources/");

    try {
      client.deploy(bm.toFile()).handle((DeployStatus s) -> System.out.println(s.getCode()));
      client.submit("fooBenchmark", "fooBenchmark", Paths.get("").toFile());
    } catch (JarFileNotFoundException | ConfigFileNotFoundException e) {
      e.printStackTrace();
    }

  }

}
