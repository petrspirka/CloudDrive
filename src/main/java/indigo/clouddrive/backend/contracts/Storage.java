package indigo.clouddrive.backend.contracts;

import jakarta.ejb.Local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;

/**
 * Interface representing a service used for storing and reading raw byte data from files
 * <br>
 * It is important to note that the Paths received by this interface should be non-absolute. The implementation should save all the paths to a separate folder or another storage (like cloud) with the path acting as a key
 */
@Local
public interface Storage {
    /**
     * Opens the specified file
     * @param path Path to a file that should be opened
     * @return Instance of {@link RandomAccessFile} which can be used to manipulate the file
     * @throws IOException When it is not possible to acquire the specified file
     * @throws FileNotFoundException When the specified file does not exist
     */
    RandomAccessFile openFile(Path path) throws IOException;

    /**
     * Creates a file specified by path. This should include any in-between directories that do not exist un
     * @param path Path to the file that should be created
     * @throws FileAlreadyExistsException When the specified file already exists
     * @throws IOException When creation process fails
     */
    void createFile(Path path) throws IOException;

    /**
     * Checks for existence of the specified path
     * @param path Path which should be checked
     * @return Whether the path exists
     */
    boolean exists(Path path);

    /**
     * Deletes the specified file
     * @param path Path to the file that should be deleted
     * @throws FileNotFoundException When the specified path does not point to a valid file
     * @throws IOException When the file could not be deleted
     */
    void deleteFile(Path path) throws IOException;

    /**
     * Gets all instances of {@link File} that can be found in the specified directory
     * @param path Path to a directory which should be checked
     * @return File array containing all files and folders in the given path
     * @throws IOException When reading data from the path fails
     * @throws FileNotFoundException When the specified path does not point to a valid directory
     */
    File[] getPathContents(Path path) throws IOException;

    /**
     * Renames the specified path to another path
     * @param path The file or directory to rename
     * @param newPath New name of the file or directory
     * @throws FileNotFoundException When the `path` parameter points to a non-existent file or directory
     * @throws FileAlreadyExistsException When the `newPath` parameter point to a file or directory that already exists
     * @throws IOException When the specified path could not be renamed
     */
    void renameFile(Path path, Path newPath) throws IOException;
}
