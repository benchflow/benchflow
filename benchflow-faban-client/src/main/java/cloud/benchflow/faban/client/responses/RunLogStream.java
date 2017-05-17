package cloud.benchflow.faban.client.responses;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 11/11/15.
 */
public class RunLogStream implements AutoCloseable, Response {

  private BufferedReader reader;

  public RunLogStream(BufferedReader reader) {
    this.reader = reader;
  }

  public RunLog readLog() throws IOException {
    String r = reader.readLine();
    return r == null ? null : new RunLog(r);
  }


  @Override
  public void close() throws Exception {
    this.reader.close();
  }

}
