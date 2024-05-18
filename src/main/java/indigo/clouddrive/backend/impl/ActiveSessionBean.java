package indigo.clouddrive.backend.impl;

import indigo.clouddrive.backend.contracts.ActiveSession;
import indigo.clouddrive.backend.models.User;
import jakarta.enterprise.context.SessionScoped;

import java.io.Serializable;

@SessionScoped
public class ActiveSessionBean implements ActiveSession, Serializable {
    private User user;

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
