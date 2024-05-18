package indigo.clouddrive.backend.impl;

import indigo.clouddrive.backend.contracts.ShareManager;
import indigo.clouddrive.backend.models.Share;
import indigo.clouddrive.backend.models.StorageObject;
import indigo.clouddrive.backend.models.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class ShareManagerBean implements ShareManager {
    @PersistenceContext(unitName = "CloudDrive")
    private EntityManager entityManager;


    @Override
    public List<Share> getShares(StorageObject object) {
        return (List<Share>) entityManager.createQuery("select share from Share share where share.storageObject = :storageObject")
                .setParameter("storageObject", object)
                .getResultList();
    }

    @Override
    public void addShare(StorageObject objectParam, User userParam, boolean isReadOnly) {
        //TODO: Fix this, fetching the same object from db
        StorageObject object = entityManager.find(objectParam.getClass(), objectParam.getStorageObjectId());
        User user = entityManager.find(userParam.getClass(), userParam.getUserId());
        Share existingShare = getShare(user, object);

        //If share already exists, we update it
        if(existingShare != null){
            existingShare.setReadOnly(isReadOnly);
            entityManager.persist(existingShare);
            return;
        }
        Share share = new Share(user, object, isReadOnly);
        object.addShare(share);
        user.addShare(share);
        entityManager.persist(share);
        entityManager.persist(object);
        entityManager.persist(user);
    }

    @Override
    public boolean removeShare(StorageObject object, User user) {
        List<Share> shares = entityManager.createQuery("select share from Share share where share.owner = :user and share.storageObject = :storageObject")
                        .setParameter("user", user)
                        .setParameter("storageObject", object)
                        .getResultList();
        if(shares.isEmpty()){
            return false;
        }
        for(Share share : shares){
            entityManager.remove(share);
        }
        return true;
    }

    @Override
    public boolean canWrite(StorageObject object, User user) {
        return verifyPermission(user, object, false);
    }

    @Override
    public boolean canRead(StorageObject object, User user) {
        return verifyPermission(user, object, true);
    }

    private boolean verifyPermission(User user, StorageObject object, boolean readOnly){
        if(object.getUser().equals(user)){
            return true;
        }
        Share share = getNearestShare(user, object);
        if(share != null){
            return readOnly || !share.isReadOnly();
        }
        return false;
    }

    //Gets the nearest share to a certain object as this is the one that decides permissions. If no such share exists, returns null instead
    private Share getNearestShare(User user, StorageObject object){
        List<Share> shares = entityManager.createQuery("select share from Share share where share.storageObject = :storageObject and share.owner = :user")
                .setParameter("storageObject", object)
                .setParameter("user", user)
                .getResultList();
        if(shares.isEmpty()){
            if(object.isRoot()){
                return null;
            }
            return getNearestShare(user, object.getParent());
        }
        return shares.get(0);
    }

    private Share getShare(User user, StorageObject object){
        List<Share> shares = entityManager.createQuery("select share from Share share where share.storageObject = :storageObject and share.owner = :user")
                .setParameter("storageObject", object)
                .setParameter("user", user)
                .getResultList();
        if(shares.isEmpty()){
            return null;
        }
        return shares.get(0);
    }
}
