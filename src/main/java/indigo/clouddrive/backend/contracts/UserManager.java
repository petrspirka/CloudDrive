package indigo.clouddrive.backend.contracts;

import indigo.clouddrive.backend.models.User;
import jakarta.ejb.Local;

import java.util.List;

/**
 * Interface used to manage users
 */
@Local
public interface UserManager {
    /**
     * Adds a new user
     * @param user The user to be added
     */
    void addUser(User user);

    /**
     * Removes the specified user
     * @param user The user to remove
     */
    void removeUser(User user);

    /**
     * Tries to get a user based on login name and a password hash
     * @param loginName The user's login name
     * @param passwdHash The user's password
     * @return User instance, if a user with the specified login name and password exists, null otherwise
     */
    User getUser(String loginName, String passwdHash);

    /**
     * Tries to get a user based on their generated app password
     * @param appPasswordHash The hash of an app password that a user has generated
     * @return User instance, if a user with the specified app password hash exists, null otherwise
     */
    User getUser(String appPasswordHash);

    /**
     * Gets the specified user by login name. Should not be used when authenticating (see {@link UserManager#getUser(String, String)}).
     * @param loginName Login name of the user
     * @return User with the given login name, otherwise null
     */
    User getUserUnsafe(String loginName);

    /**
     * Checks if there is a user with the given login name
     * @param loginName The login name for which we should search
     * @return Whether the user with specified login name exists
     */
    boolean existsUser(String loginName);

    /**
     * Gets all users
     * @return List of users
     */
    List<User> getUsers();

    /**
     * Creates a new app password for the specified user
     * @param user The user for which app password should be generated
     * @return The newly-created app password
     */
    String createAppPassword(User user);
}
