package indigo.clouddrive.backend.contracts;

import indigo.clouddrive.backend.models.StorageObject;
import jakarta.ejb.Local;

import java.io.Closeable;
import java.io.IOException;
import java.io.FileNotFoundException;
import indigo.clouddrive.backend.exceptions.LockAlreadyAcquiredException;

/**
 * Interface used to manage the content of storage objects
 * <br>
 * This interface is expected to encrypt and decrypt data using {@link ContentEncrypter}. All implementations are also expected to be session-scoped.
 * <br>
 * The {@link #close()} method should unlock the file and release all data related to it
 */
@Local
public interface FileManager extends Closeable {
    /**
     * Gets the byte content of the specified storage object
     * @param object StorageObject instance for which the content should be retrieved
     * @return Bytes of the storage object
     * @throws IOException When the content cannot be retrieved
     * @throws FileNotFoundException When the specified storage object is a directory or when the specific file could not be found in the filesystem
     */
    byte[] getContent(StorageObject object) throws IOException;

    /**
     * Sets the content of the specified storage object
     * @param object StorageObject instance for which the content should be retrieved
     * @param content Byte content that should be set to the file
     * @return Whether the data was successfully written
     * @throws IOException When the content cannot be set
     * @throws FileNotFoundException When the specified storage object is a directory or when the specific file could not be found in the filesystem
     */
    boolean setContent(StorageObject object, byte[] content) throws IOException;

    /**
     * Locks a specified storage object, allowing {@link #read()} and {@link #write(byte[])} operations
     * @param object StorageObject instance which points to the file that should be locked
     * @throws IOException When the file could not be locked
     * @throws FileNotFoundException When the specified storage object is a directory or when the specific file could not be found in the filesystem
     * @throws LockAlreadyAcquiredException When the specified file has already been locked by the application
     */
    void lockFile(StorageObject object) throws IOException;

    /**
     * Writes data to the currently locked object
     * <br>
     * Undefined behaviour when the file has not been set using {@link #lockFile(StorageObject)}
     * <br>
     * If the file lock on the given storage object has expired, the method should attempt to lock it again, and if it fails, throw an appropriate exception
     * @param content The content that should be written to the file
     * @throws IOException When the content could not be written
     */
    void write(byte[] content) throws IOException;
    /**
     * Reads data from the currently locked object
     * <br>
     * Undefined behaviour when the file has not been set using {@link #lockFile(StorageObject)}
     * <br>
     * If the file lock on the given storage object has expired, the method should attempt to lock it again, and if it fails, throw an appropriate exception
     * @return The content inside the file
     * @throws IOException When the content could not be read
     */
    byte[] read() throws IOException;

    /**
     * Compresses the specified storage object into a stream that can be sent to a client. This in most cases should be ZIP
     * @param object StorageObject instance on which compression should be applied
     * @param pathPrefixToRemove String that denotes what qualifies as the start of the archive. Implementation will use this to only compress folders beyond this (as {@link StorageObject} naturally returns the entire path which might not be preferred in some cases)
     * @return Byte array representing a ZIP stream
     * @throws IOException When the compression fails
     */
    //TODO: Potentially refactor to not return a byte array but a stream/something else
    byte[] compressStorageObject(StorageObject object, String pathPrefixToRemove) throws IOException;
}
