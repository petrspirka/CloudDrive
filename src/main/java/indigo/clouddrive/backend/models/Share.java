package indigo.clouddrive.backend.models;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;

@Entity
@Table(name = "Shares")
public class Share {
    @Id
    @GeneratedValue
    @JsonbTransient
    private long shareId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId")
    private User owner;

    @ManyToOne(optional = false)
    @JoinColumn(name = "storageObjectId")
    private StorageObject storageObject;

    @Column(nullable = false)
    private boolean isReadOnly = false;

    public Share(){}
    public Share(User owner, StorageObject storageObject, boolean isReadOnly){
        this.owner = owner;
        this.storageObject = storageObject;
        this.isReadOnly = isReadOnly;
    }

    public long getShareId() {
        return shareId;
    }
    public indigo.clouddrive.backend.models.User getOwner() {
        return owner;
    }
    public void setOwner(indigo.clouddrive.backend.models.User user) {
        this.owner = user;
    }
    public boolean isReadOnly() {
        return isReadOnly;
    }
    public void setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }
    public StorageObject getStorageObject() {
        return storageObject;
    }
    public void setStorageObject(StorageObject storageObject) {
        this.storageObject = storageObject;
    }
}
