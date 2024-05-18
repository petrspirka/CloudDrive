package indigo.clouddrive.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"USER_USERID", "NAME"}))
public class RootStorageObject extends StorageObject {
    @OneToOne(optional = false)
    private User user;

    public RootStorageObject(User user){
        super(user.getLoginName(), null, true);
        this.user = user;
    }

    public RootStorageObject(){}

    @Override
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean isRoot() {
        return true;
    }
}
