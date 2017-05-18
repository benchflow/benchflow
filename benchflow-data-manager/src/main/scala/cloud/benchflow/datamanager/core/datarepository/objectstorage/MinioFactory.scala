package cloud.benchflow.datamanager.core.datarepository.objectstorage

import com.typesafe.config.ConfigFactory

import io.minio.MinioClient

class MinioFromConfig extends ExperimentObjectStorage with Minio {
  val configuration = ConfigFactory.load()

  override val minioClient: MinioClient = {
    val url = configuration.getString("minio.url")
    val accessKey = configuration.getString("minio.accessKey")
    val secretKey = configuration.getString("minio.secretKey")
    new MinioClient(url, accessKey, secretKey)
    // new MinioClient("https://play.minio.io:9000", "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG")
  }

  override val defaultBucket: String = configuration.getString("minio.defaultBucket")
}

class MinioFromClient(val minioClient: MinioClient, val defaultBucket: String = "test") extends ExperimentObjectStorage with Minio

object MinioFactory {
  def apply: MinioFromConfig = new MinioFromConfig
  def apply(minioClient: MinioClient): MinioFromClient = new MinioFromClient(minioClient)
  def apply(minioClient: MinioClient, defaultBucket: String): MinioFromClient = new MinioFromClient(minioClient, defaultBucket)
  def apply(url: String, accessKey: String, secretKey: String, defaultBucket: String = "test"): MinioFromClient = {
    val minioClient = new MinioClient(url, accessKey, secretKey)
    new MinioFromClient(minioClient, defaultBucket)
  }
}
