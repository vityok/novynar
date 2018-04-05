import java.io.IOException;
import java.io.File;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.NoSuchFileException;

import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

import org.junit.jupiter.api.AfterEach;

import org.bb.vityok.novinar.Novinar;


/** Common methods and variables for the Novinar tests. */
public class BaseTest
{
    public static final File opmlFile = new File("test/resources/opml-file.opml");
    public static final String DB_NAME = "testDB";
    Novinar novinar = null;

    /** Ensures a clean start for the test. */
    public void configure() {
        // todo: important note The directory is created under the directory that the
        // system property derby.system.home points to, or the current
        // directory (user.dir) if derby.system.home is not set.
        try {
            // remove the derby DB if it exists
            Path testDB = FileSystems.getDefault().getPath(DB_NAME);
            deleteFileOrFolder(testDB);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            novinar = new Novinar(opmlFile, DB_NAME);
            novinar.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void close () {
        try {
            novinar.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // see: https://stackoverflow.com/questions/3775694/deleting-folder-from-java
    public static void deleteFileOrFolder(final Path path)
        throws IOException
    {
        try {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                    throws IOException {
                    Files.delete(file);
                    return CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(final Path file, final IOException e) {
                    return handleException(e);
                }

                private FileVisitResult handleException(final IOException e) {
                    e.printStackTrace(); // replace with more robust error handling
                    return TERMINATE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
                    throws IOException {
                    if(e!=null)return handleException(e);
                    Files.delete(dir);
                    return CONTINUE;
                }
            });
        } catch (NoSuchFileException nsfe) {
            // not a problem
        } catch (IOException e) {
            throw e;
        }
    }
}
