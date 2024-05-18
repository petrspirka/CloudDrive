package indigo.clouddrive.backend.impl;

import indigo.clouddrive.backend.FolderMatch;
import indigo.clouddrive.backend.contracts.FileManager;
import indigo.clouddrive.backend.contracts.ShareManager;
import indigo.clouddrive.backend.contracts.Storage;
import indigo.clouddrive.backend.contracts.UserStorage;
import indigo.clouddrive.backend.models.RootStorageObject;
import indigo.clouddrive.backend.models.StorageObject;
import indigo.clouddrive.backend.models.User;
import indigo.clouddrive.frontend.helpers.PathHelpers;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

//TODO: Possibly revise this class, there are way too many methods, possibly split it into smaller ones
@Stateless
public class UserStorageBean implements UserStorage {
    @PersistenceContext(unitName = "CloudDrive")
    private EntityManager entityManager;

    @EJB
    private Storage storage;


    private @Inject FileManager fileManager;
    @EJB
    private ShareManager shareManager;

    @Override
    public List<StorageObject> getFolderContents(User user, String path) throws IOException {
        if(PathHelpers.getTrimmedPath(path).equals("shares")){
            return entityManager.createQuery("select share.storageObject from Share share where share.owner = :user")
                    .setParameter("user", user)
                    .getResultList();
        }
        StorageObject object = traverseRootToStorageObjectOrThrow(user, path, true);
        return object.getChildren();
    }

    @Override
    public boolean existsStorageObject(User user, String path) {
        try {
            traverseRootToStorageObjectOrThrow(user, path, true);
            return true;
        }
        catch(IOException ex){
            return false;
        }
    }

    @Override
    public void createFile(User user, String path) throws IOException {
        createNewObject(user, path, false);
    }

    @Override
    public void createFolder(User user, String path) throws IOException {
        createNewObject(user, path, true);
    }

    private void createNewObject(User user, String path, boolean isFolder) throws IOException {
        if(PathHelpers.getTrimmedPath(path).equals("shares")){
            throw new IOException("Cannot create a '/shares' path as it is reserved");
        }
        if(path.isEmpty()){
            throw new IOException("Cannot create an empty object");
        }
        String[] splitArray = path.split("[/\\\\]");
        List<String> split = new ArrayList<>(Arrays.stream(splitArray).toList());
        String target = split.get(splitArray.length-1);
        split.remove(splitArray.length-1);
        String root = String.join("/", split);
        StorageObject folder = traverseRootToStorageObjectOrThrow(user, root, false);
        if(!folder.isFolder()){
            throw new NotDirectoryException("The specified path is not a valid directory");
        }
        StorageObject newChild = new StorageObject(target, folder, isFolder);
        folder.addChild(newChild);
        entityManager.persist(newChild);
        entityManager.persist(folder);
        if(!isFolder){
            StringBuilder sb = new StringBuilder();
            newChild.getPath(sb, true);
            String hashedPath = newChild.getHashedPath();
            try {
                storage.createFile(Path.of(hashedPath));
            }
            catch(IOException ex){
                entityManager.remove(newChild);
            }
        }
    }

    @Override
    public void deleteStorageObject(User user, String path) throws IOException {
        StorageObject folder = traverseRootToStorageObjectOrThrow(user, path, false);
        deleteStorageObject(folder);
    }

    @Override
    public StorageObject getStorageObject(User user, String path) throws IOException {
        return getStorageObject(user, path, false);
    }

    @Override
    public StorageObject getStorageObject(User user, String path, boolean readOnly) throws IOException {
        return traverseRootToStorageObjectOrThrow(user, path, readOnly);
    }

    @Override
    public void renameStorageObject(User user, String path, String newName) throws IOException {
        StorageObject object = traverseRootToStorageObjectOrThrow(user, path, false);
        if(newName.isEmpty() || object.getName().equals(newName)){
            return;
        }
        if(!object.isFolder()){
            String currentPath = object.getHashedPath();
            String oldName = object.getName();
            object.setName(newName);
            try {
                updateFile(object, currentPath);
            }
            catch(Exception ex){
                object.setName(oldName);
                throw new IOException(ex);
            }
        }
        else {
            Map<String, FolderMatch> pathMap = new HashMap<>();
            //Required to correctly rename all files in a directory during a rename
            traverseFolder(object, pathMap);
            object.setName(newName);
            for (Map.Entry<String, FolderMatch> entry : pathMap.entrySet()) {
                FolderMatch value = entry.getValue();
                updateFile(value.storageObject(), value.oldPath());
            }
            entityManager.persist(object);
        }
    }

    @Override
    public void copyStorageObject(User user, String path) throws IOException {
        StorageObject object = traverseRootToStorageObjectOrThrow(user, path, true);
        makeCopy(getRootStorageObject(user), object);
    }

    private boolean makeCopy(StorageObject parent, StorageObject toCopy) {
        StorageObject copy = new StorageObject(toCopy.getName(), parent, toCopy.isFolder());
        if(toCopy.isFolder()){
            parent.addChild(copy);
            entityManager.persist(copy);
            entityManager.persist(parent);
            boolean allPassed = true;
            for(StorageObject child : toCopy.getChildren()){
                allPassed &= makeCopy(copy, child);
            }
            return allPassed;
        }

        byte[] data;
        try {
            data = fileManager.getContent(toCopy);
            if(data == null){
                return false;
            }
            fileManager.setContent(copy, data);
        }
        catch(IOException ignored){
            return false;
        }

        try {
            parent.addChild(copy);
            entityManager.persist(copy);
            entityManager.persist(parent);
        } catch(Exception ex){
            try {
                storage.deleteFile(Path.of(copy.getHashedPath()));
            } catch (IOException ignored) {}
            return false;
        }
        return true;
    }

    private void updateFile(StorageObject object, String oldHash) throws IOException {
        storage.renameFile(Path.of(oldHash), Path.of(object.getHashedPath()));
    }

    private void traverseFolder(StorageObject object, Map<String, FolderMatch> pathMap){
        if(!object.isFolder()){
            pathMap.put(object.getStorageObjectId(), new FolderMatch(object.getHashedPath(), object));
        }
        else {
            for(StorageObject child : object.getChildren()){
                traverseFolder(child, pathMap);
            }
        }
    }

    private void deleteStorageObject(StorageObject object) throws IOException {
        if(!object.isFolder()){
            fileManager.setContent(object, null);
        }
        else {
            for (StorageObject child : object.getChildren()) {
                deleteStorageObject(child);
            }
        }
        if(object.isRoot()){
            return;
        }
        StorageObject parent = object.getParent();
        parent.removeChild(object);
        entityManager.remove(object);
        entityManager.persist(parent);
    }

    private RootStorageObject getRootStorageObject(User user){
        List<RootStorageObject> rootStorageObjects = entityManager.createQuery("select storageObject from RootStorageObject storageObject where storageObject.user = :user")
                .setParameter("user", user)
                .getResultList();
        if(rootStorageObjects.isEmpty()){
            return null;
        }
        return rootStorageObjects.get(0);
    }

    private RootStorageObject getRootStorageObjectOrThrow(User user) throws IOException{
        RootStorageObject rootStorageObject = getRootStorageObject(user);
        if(rootStorageObject == null){
            throw new IOException("The specified user lacks a root directory");
        }
        return rootStorageObject;
    }

    private StorageObject traverseStorageObject(StorageObject storageObject, String path) throws NotDirectoryException{
        StorageObject traversalObject = storageObject;
        String[] splitArray = path.split("/");
        //TODO: Verify that Collectors.toList is actually mutable
        List<String> split = Arrays.stream(splitArray).collect(Collectors.toList());
        while(!split.isEmpty()) {
            if(split.get(0).isEmpty()){
                //TODO: DRY, potentially fix later
                split.remove(0);
                continue;
            }
            StorageObject[] matches = traversalObject.getChildren().toArray(new StorageObject[0]);
            boolean valid = false;
            for(StorageObject match : matches){
                if(match.getName().equals(split.get(0))){
                    traversalObject = match;
                    valid = true;
                    split.remove(0);
                    break;
                }
            }
            if(!valid){
                throw new NotDirectoryException(split.get(0));
            }
        }
        return traversalObject;
    }

    private StorageObject traverseRootToStorageObjectOrThrow(User user, String path, boolean readOnly) throws IOException{
        String trimmedPath = PathHelpers.getTrimmedPath(path);
        boolean isShared = trimmedPath.startsWith("shares/");
        if(!isShared) {
            RootStorageObject rootStorageObject = getRootStorageObjectOrThrow(user);
            return traverseStorageObject(rootStorageObject, path);
        }

        String[] pathSplit = trimmedPath.split("/");
        String userName = pathSplit[1];
        String[] pathSplitWithoutUsername = Arrays.stream(pathSplit).skip(2).toArray(String[]::new);
        List<User> users = entityManager.createQuery("select user from User user where user.loginName = :loginName")
                .setParameter("loginName", userName)
                .getResultList();
        if(users.isEmpty()){
            throw new IOException("Could not get the specified user");
        }
        User owner = users.get(0);
        RootStorageObject rootStorageObject = getRootStorageObjectOrThrow(owner);
        StorageObject object = traverseStorageObject(rootStorageObject, String.join("/", pathSplitWithoutUsername));
        if((readOnly && shareManager.canRead(object, user)) || (!readOnly && shareManager.canWrite(object, user))) {
            return object;
        }
        else {
            throw new IOException("The specified user lacks permissions to access this file");
        }
    }
}
