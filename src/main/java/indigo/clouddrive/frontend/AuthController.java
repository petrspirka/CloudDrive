package indigo.clouddrive.frontend;

import indigo.clouddrive.backend.contracts.ActiveSession;
import indigo.clouddrive.backend.models.User;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;


@RequestScoped
@Named("authController")
public class AuthController {
    private @Inject ActiveSession activeSession;
    public boolean isLoggedIn(){
        return activeSession.getUser() != null;
    }

    public User getUser(){
        return activeSession.getUser();
    }

    // https://stackoverflow.com/questions/8399045/conditional-redirection-in-jsf
    public void accessProtectedPage() throws IOException {
        if(!isLoggedIn()){
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            context.redirect("login.xhtml");
        }
    }

    public void accessLoginPage() throws IOException {
        if(isLoggedIn()){
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            context.redirect("index.xhtml");
        }
    }
}
