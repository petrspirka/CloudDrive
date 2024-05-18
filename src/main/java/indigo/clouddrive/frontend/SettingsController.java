package indigo.clouddrive.frontend;

import indigo.clouddrive.backend.contracts.ActiveSession;
import indigo.clouddrive.backend.contracts.UserManager;
import indigo.clouddrive.backend.models.User;
import jakarta.annotation.ManagedBean;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateful;
import jakarta.ejb.TransactionAttribute;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.Random;

@RequestScoped
@Named("settingsController")
public class SettingsController implements Serializable {
    @EJB
    private UserManager userManager;
    private @Inject ActiveSession activeSession;
    public String createAppPassword(){
        return userManager.createAppPassword(activeSession.getUser());
    }

}
