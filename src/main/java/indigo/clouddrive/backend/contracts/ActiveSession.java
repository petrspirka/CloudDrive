package indigo.clouddrive.backend.contracts;

import indigo.clouddrive.backend.models.User;
import jakarta.ejb.Local;

/**
 * Interface used for managing the HTTP session
 */
@Local
public interface ActiveSession {
    /**
     * Gets the user currently set in the active HTTP session
     * @return The user that is currently stored in active session, otherwise null
     */
    User getUser();

    /**
     * Sets the user to the current HTTP session
     * @param user The user that should be set to the active HTTP session
     */
    void setUser(User user);
}
