package cloud.benchflow.experimentmanager.demo;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * NOTE: This class is to be removed when driver-maker updates its minio interaction
 *
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-11
 */
public class Hashing {

  private static int numOfCharacters = 4;

  public static String hashKey(String key)
      throws UnsupportedEncodingException, NoSuchAlgorithmException {
    return Hashing.hashMD5(key);
  }

  private static String hashMD5(String key)
      throws UnsupportedEncodingException, NoSuchAlgorithmException {
    byte[] bytesOfMessage = key.getBytes("UTF-8");
    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] hashBytes = md.digest(bytesOfMessage);
    String hashString = new BigInteger(1, hashBytes).toString(16);
    hashString = hashString.substring(0, numOfCharacters);
    return (hashString + "/" + key);
  }
}
