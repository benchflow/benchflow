package cloud.benchflow.driversmaker.utils;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * A managed directory is automatically deleted when exiting from a try with resources.
 * Created on 02/03/16.
 */
public class ManagedDirectory implements AutoCloseable {

    private Path path;

    public ManagedDirectory(String path) {
        Path p = Paths.get(path);
        assert p.toFile().isDirectory();
        this.path = Paths.get(path);
    }

    public Path getPath() { return this.path; }

    @Override
    public void close() throws IOException {
        if(path.toFile().exists()) {
            FileUtils.deleteDirectory(path.toFile());
        }
    }
}
