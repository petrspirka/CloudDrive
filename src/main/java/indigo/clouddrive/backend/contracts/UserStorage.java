package indigo.clouddrive.backend.contracts;

import indigo.clouddrive.backend.models.StorageObject;
import indigo.clouddrive.backend.models.User;
import jakarta.ejb.Local;

import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.List;

/**
 * Interface representing a service used for managing {@link StorageObject} instances
 * <br>
 * This class should also handle shares using {@link ShareManager}. A share should be considered any path that starts with 'shares' or '/shares'. As such, no file or folder named 'shares' should be allowed to be created in the {@link indigo.clouddrive.backend.models.RootStorageObject}
 */
@Local
public interface UserStorage {
    /**
     * Gets all children of a storage object given its owned and path. If the `path` argument starts with 'shares' or '/shares',
     * @param user The user that is trying to access the contents of this object
     * @param path The path to the object
     * @return List of objects contained within this {@link StorageObject}
     * @throws IOException When the objects could not be fetched
     */
    List<StorageObject> getFolderContents(User user, String path) throws IOException;

    /**
     * Checks whether there is a storage object matching the user and path
     * @param user The user that is trying to see if the object exists
     * @param path Path to the storage object that might or might not exist
     * @return Whether the storage object exists
     */
    boolean existsStorageObject(User user, String path);

    /**
     * Creates a new file StorageObject
     * @param user User for whom the StorageObject which be created
     * @param path The path where the storage object should be created, including its name as the last element
     * @throws IOException When the file could not be created
     * @throws NotDirectoryException When the target path would cause the file to be created as a child of a file
     */
    void createFile(User user, String path) throws IOException;
    /**
     * Creates a new directory StorageObject
     * @param user User for whom the StorageObject which be created
     * @param path The path where the storage object should be created, including its name as the last element
     * @throws IOException When the directory could not be created
     * @throws NotDirectoryException When the target path would cause the directory to be created as a child of a file
     */
    void createFolder(User user, String path) throws IOException;

    /**
     * Deletes a storage object and all it's children
     * @param user The user for whom the storage object should be deleted
     * @param path The path to the storage object
     * @throws IOException When the storage object could not be deleted
     */
    void deleteStorageObject(User user, String path) throws IOException;
    /**
     * Behaves the same as {@link #getStorageObject(User, String, boolean)} with readOnly set to `false`
     * @param user The user for whom the object should be fetched
     * @param path The path to the storage object
     * @return StorageObject instance
     * @throws IOException When the storage object could not be acquired or if the user lacks write or read permission to this object
     */
    StorageObject getStorageObject(User user, String path) throws IOException;
    /**
     * Gets a storage object by user and path. When possible, other methods should be used for storage manipulation
     * @param user The user for whom the object should be fetched
     * @param path The path to the storage object
     * @param readOnly Whether the storage object lookup requires the user to have write access
     * @return StorageObject instance
     * @throws IOException When the storage object could not be acquired or if the user lacks write or read permission to this object
     */
    StorageObject getStorageObject(User user, String path, boolean readOnly) throws IOException;

    /**
     * Renames a storage object
     * @param user The user for whom the object should be retrieved
     * @param path The path to the storage object
     * @param newName New name of the storage object
     * @throws IOException When the storage object could not be renamed
     */
    void renameStorageObject(User user, String path, String newName) throws IOException;

    /**
     * Copies the shared storage object to the specified user's home directory
     * @param user The user for whom the object should be retrieved and then copied
     * @param path The path to the storage object. Must be a share path
     * @throws IOException When the storage object could not be copied
     */
    void copyStorageObject(User user, String path) throws IOException;
}
