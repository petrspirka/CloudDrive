
package indigo.clouddrive.frontend.validators;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * Validator overujici obskurni podminky pro kvalitu hesla
 */
@FacesValidator("passwordStrengthValidator")
public class PasswordStrengthValidator implements Validator<String> {

    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
       
        int conditions = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c)) conditions |= 0x01;
            if (Character.isLowerCase(c)) conditions |= 0x02;
            if (Character.isAlphabetic(c)) conditions |= 0x04;
            if (Character.isDigit(c)) conditions |= 0x08;
        }
        if (conditions != 0x0f) {
            FacesMessage msg = new FacesMessage("Password is too weak", 
                    "Password has to contain a digit, a lower-case character and upper-case character");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    } 
}
