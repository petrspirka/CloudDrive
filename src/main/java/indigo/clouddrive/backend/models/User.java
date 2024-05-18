package indigo.clouddrive.backend.models;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Users")
public class User implements Serializable {
    @Id
    @GeneratedValue
    @JsonbTransient
    private int userId;
    @Column(nullable = false, unique = true)
    private String loginName;
    @Column(nullable = false)
    @JsonbTransient
    private String passwordHash;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String secondName;
    @Column(nullable = true)
    @JsonbTransient
    private String appPasswordHash;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonbTransient
    private List<Share> shares;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonbTransient
    private RootStorageObject rootStorageObject;

    public User(){}
    public User(String loginName, String passwordHash, String firstName, String secondName) {
        this.loginName = loginName;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.secondName = secondName;
    }

    public int getUserId(){
        return userId;
    }
    public String getLoginName() {
        return loginName;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getSecondName() {
        return secondName;
    }
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }
    public List<Share> getShares() {
        return Collections.unmodifiableList(shares);
    }

    public String getAppPasswordHash() {
        return appPasswordHash;
    }

    public void setAppPasswordHash(String appPasswordHash) {
        this.appPasswordHash = appPasswordHash;
    }

    public RootStorageObject getRootStorageObject() {
        return rootStorageObject;
    }

    public void setRootStorageObject(RootStorageObject rootStorageObject) {
        this.rootStorageObject = rootStorageObject;
    }

    public void addShare(Share share) {
        this.shares.add(share);
    }

    public boolean removeShare(Share share){
        return this.shares.remove(share);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId && Objects.equals(loginName, user.loginName) && Objects.equals(passwordHash, user.passwordHash) && Objects.equals(firstName, user.firstName) && Objects.equals(secondName, user.secondName) && Objects.equals(appPasswordHash, user.appPasswordHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, loginName, passwordHash, firstName, secondName, appPasswordHash);
    }
}
