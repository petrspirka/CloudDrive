package indigo.clouddrive.backend.impl;

import indigo.clouddrive.backend.contracts.UserManager;
import indigo.clouddrive.backend.helpers.CryptoHelpers;
import indigo.clouddrive.backend.models.RootStorageObject;
import indigo.clouddrive.backend.models.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class UserManagerBean implements UserManager {
    @PersistenceContext(unitName = "CloudDrive")
    private EntityManager entityManager;

    @Override
    public void addUser(User user) {
        entityManager.persist(user);
        entityManager.persist(new RootStorageObject(user));
    }

    @Override
    public void removeUser(User user) {
        entityManager.remove(user);
    }

    @Override
    public User getUser(String loginName, String passwdHash) {
        List<User> users = (List<User>)entityManager.createQuery("select user from User user where user.loginName = :loginName and user.passwordHash = :passwordHash")
                .setParameter("loginName", loginName)
                .setParameter("passwordHash", passwdHash)
                .getResultList();
        if(!users.isEmpty()){
            return users.get(0);
        }
        return null;
    }

    @Override
    public User getUser(String appPasswordHash) {
        List<User> users = (List<User>)entityManager.createQuery("select user from User user where user.appPasswordHash = :appPasswordHash")
                .setParameter("appPasswordHash", appPasswordHash)
                .getResultList();
        if(!users.isEmpty()){
            return users.get(0);
        }
        return null;
    }

    @Override
    public User getUserUnsafe(String loginName) {
        List<User> users = (List<User>)entityManager.createQuery("select user from User user where user.loginName = :loginName")
                .setParameter("loginName", loginName)
                .getResultList();
        if(!users.isEmpty()){
            return users.get(0);
        }
        return null;
    }

    @Override
    public boolean existsUser(String loginName) {
        int firstResult = entityManager.createQuery("select user from User user where user.loginName = :loginName")
                .setParameter("loginName", loginName)
                .getFirstResult();
        return firstResult != 0;
    }

    @Override
    public List<User> getUsers() {
        return (List<User>)entityManager.createQuery("select user from User user")
                .getResultList();
    }

    @Override
    public String createAppPassword(User user) {
        //Workaround because the user reference here causes issues
        User dbUser = entityManager.find(user.getClass(), user.getUserId());
        String appPassword = CryptoHelpers.generateRandomHexString(32);
        String appPasswordHash = CryptoHelpers.getHash(appPassword);
        dbUser.setAppPasswordHash(appPasswordHash);
        entityManager.persist(dbUser);
        return appPassword;
    }
}
