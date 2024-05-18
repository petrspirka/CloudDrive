package indigo.clouddrive.frontend.validators;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * Validuje dve pole pro hesla
 * Puvodni heslo je ziskano pres dodatecny atribut "passwordComponentBinding"
 */
@FacesValidator("confirmPasswordValidator")
public class ConfirmPasswordValidator implements Validator<String> {

    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        
        // ziska komponentu, obsahujici prvni zadane heslo 
        UIInput passwdComponent = (UIInput) component.getAttributes().get("passwordComponentBinding");
        
        // testuje, zda je heslo validni
        if (!passwdComponent.isValid()) return;
        
        // ziska hodnotu puvodniho hesla
        String passwd = (String) passwdComponent.getValue();
        
        if (!value.equals(passwd)) {
            FacesMessage msg = new FacesMessage("Passwords are not matching");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }
    
}