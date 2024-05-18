package indigo.clouddrive.backend.contracts;

import indigo.clouddrive.backend.models.Share;
import indigo.clouddrive.backend.models.StorageObject;
import indigo.clouddrive.backend.models.User;
import jakarta.ejb.Local;

import java.util.List;

/**
 * Interface representing a service that manages {@link Share} instances
 */
@Local
public interface ShareManager {
    /**
     * Gets all shares of a {@link StorageObject}
     * @param object Object for which shares should be fetched
     * @return List of shares
     */
    List<Share> getShares(StorageObject object);

    /**
     * Adds a new share
     * @param object Object for which the share should be added
     * @param user The user to whom this share will be given
     * @param isReadOnly Whether the user can perform write, or only read operations
     */
    void addShare(StorageObject object, User user, boolean isReadOnly);

    /**
     * Removes a share
     * @param object The object to which the share belongs
     * @param user The user to which the share belongs
     * @return Whether the share was successfully removed
     */
    boolean removeShare(StorageObject object, User user);

    /**
     * Checks whether the specified user can perform write operations on the specified object
     * @param object Object for which write permissions should be checked
     * @param user User for which write operations should be checked
     * @return Whether the user can perform write operations
     */
    boolean canWrite(StorageObject object, User user);

    /**
     * Checks whether the specified user can perform read operations on the specified object
     * @param object Object for which read permissions should be checked
     * @param user User for which read operations should be checked
     * @return Whether the user can perform read operations
     */
    boolean canRead(StorageObject object, User user) ;
}
