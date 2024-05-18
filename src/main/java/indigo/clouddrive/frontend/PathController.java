package indigo.clouddrive.frontend;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jdk.jfr.Name;

import java.util.Map;

@RequestScoped
@Named("pathController")
public class PathController {
    public String getPath(){
        Map<String, String> queryParamMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        return queryParamMap.getOrDefault("path", "");
    }
}
