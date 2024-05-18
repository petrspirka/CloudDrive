package indigo.clouddrive.backend.models;

import indigo.clouddrive.backend.contracts.ActiveSession;
import indigo.clouddrive.backend.exceptions.EncryptionException;
import indigo.clouddrive.backend.helpers.CryptoHelpers;
import indigo.clouddrive.frontend.helpers.PathHelpers;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "StorageObjects", uniqueConstraints = @UniqueConstraint(columnNames = {"NAME", "PARENT_STORAGEOBJECTID", "ISFOLDER"}))
public class StorageObject implements Serializable {
    @Id
    @GeneratedValue
    @JsonbTransient
    private String storageObjectId;
    @Column(nullable = false)
    @JsonbTransient
    private String name;
    @Column(nullable = false, length = 32)
    @JsonbTransient
    private String salt;
    @Column(nullable = false)
    private boolean isFolder;
    @OneToMany(mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonbTransient
    private List<StorageObject> children;
    @ManyToOne()
    @JsonbTransient
    private StorageObject parent;
    @OneToMany(mappedBy = "storageObject", orphanRemoval = true, cascade = CascadeType.REMOVE)
    @JsonbTransient
    private List<Share> shares;

    public StorageObject(){}
    public StorageObject(String name, StorageObject parent, boolean isFolder){
        this.parent = parent;
        this.name = name;
        this.salt = CryptoHelpers.generateRandomAESSalt();
        this.isFolder = isFolder;
    }

    public boolean isFolder() {
        return isFolder;
    }
    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public StorageObject getParent() {
        return parent;
    }

    public void setParent(StorageObject parent) {
        this.parent = parent;
    }

    public List<StorageObject> getChildren() {
        return List.copyOf(children);
    }

    public void setChildren(List<StorageObject> children) {
        this.children = children;
    }
    public void addChild(StorageObject child){
        this.children.add(child);
    }
    public String getSalt() {
        return salt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void getPath(StringBuilder builder, boolean includeRoot){
        if(parent != null){
            parent.getPath(builder, includeRoot);
        }
        if(parent != null || includeRoot) {
            builder.append(this.name)
                    .append('/');
        }
    }
    public String getPath(boolean includeRoot){
        StringBuilder sb = new StringBuilder();
        getPath(sb, includeRoot);
        return PathHelpers.getTrimmedPath(sb.toString());
    }

    public String getPath(){
        return getPath(false);
    }
    public boolean isRoot(){
        return false;
    }
    @JsonbTransient
    public RootStorageObject getRootStorageObject() {
        if(isRoot()){
            return (RootStorageObject)this;
        }
        return this.parent.getRootStorageObject();
    }

    @JsonbTransient
    public String getHashedPath() {
        String path = getPath(true);
        String hash = CryptoHelpers.getHash(path);
        return CryptoHelpers.base64StringToCustomBase64String(hash);
    }

    public User getUser(){
        return parent.getUser();
    }
    public boolean removeChild(StorageObject object) {
        return children.remove(object);
    }
    public String getStorageObjectId() {
        return storageObjectId;
    }

    public List<Share> getShares() {
        return Collections.unmodifiableList(shares);
    }

    public boolean removeShare(Share share){
        return shares.remove(share);
    }

    public void addShare(Share share){
        this.shares.add(share);
    }

}