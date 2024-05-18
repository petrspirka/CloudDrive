
package indigo.clouddrive.frontend;

import indigo.clouddrive.backend.contracts.UserManager;
import indigo.clouddrive.backend.helpers.CryptoHelpers;
import indigo.clouddrive.backend.models.User;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named("registerController")
@RequestScoped
public class RegisterController {
    @EJB
    private UserManager userManager;

    private String loginName;
    private String firstName;
    private String secondName;
    private String password;
    private String confirmPassword;

    public RegisterController() {
        super();
    }

    public String createUser() {
        userManager.addUser(new User(loginName, CryptoHelpers.getHash(password), firstName, secondName));
        return "login";
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
