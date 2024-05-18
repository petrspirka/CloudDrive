package indigo.clouddrive.frontend;

import indigo.clouddrive.backend.contracts.ActiveSession;
import indigo.clouddrive.backend.contracts.UserManager;
import indigo.clouddrive.backend.helpers.CryptoHelpers;
import indigo.clouddrive.backend.models.User;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;

@Named("loginController")
@RequestScoped
public class LoginController implements Serializable {
    @EJB
    private UserManager userManager;
    private @Inject ActiveSession activeSession;

    private String loginName;
    private String password;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String login() throws IOException {
        User u = userManager.getUser(loginName, CryptoHelpers.getHash(password));
        if (u != null) {
            activeSession.setUser(u);
            FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
        }
        return "login";
    }
}
