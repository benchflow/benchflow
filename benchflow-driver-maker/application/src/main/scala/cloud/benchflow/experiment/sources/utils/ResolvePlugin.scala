package cloud.benchflow.experiment.sources.utils

import java.nio.file.Path

import cloud.benchflow.test.config.Version

import scala.util.{Success, Failure, Try}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 25/07/16.
  * Utility object to resolve a plugin
  */
object ResolvePlugin {

  private val directoriesFilter = new java.nio.file.DirectoryStream.Filter[Path]() {
    def accept(file: Path) = file.toFile.isDirectory
  }

  private def allPluginVersions(pluginsPath: Path): Seq[Version] = {

    import scala.collection.mutable.ListBuffer

    val dirStream = Try(java.nio.file.Files.newDirectoryStream(pluginsPath, directoriesFilter))
    val pluginVersions = new ListBuffer[Version]()

    dirStream match {

      case Failure(e) => throw e

      case Success(stream) =>
        val iterator = stream.iterator()
        while(iterator.hasNext)
          pluginVersions += Version(iterator.next().getFileName.toString)
        stream.close()
    }

    pluginVersions.toList

  }

  def apply(pluginsPath: Path, pluginName: String, version: Version): Path = {

    pluginsPath.resolve(
      allPluginVersions(pluginsPath)
        .find(dirVersion => version.isCompatible(dirVersion)) match {
        case Some(v) => v.toString
        case None => throw new Exception(s"Plugin for version $version couldn't be found.")
      }
    ).resolve(pluginName)

  }

}
