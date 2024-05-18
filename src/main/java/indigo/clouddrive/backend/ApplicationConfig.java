package indigo.clouddrive.backend;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

import indigo.clouddrive.backend.api.*;

@ApplicationPath("api")
public class ApplicationConfig extends Application {
    @Override
    public Set<Class<?>> getClasses(){
        Set<Class<?>> resources = new HashSet<>();
        addRestClasses(resources);
        return resources;
    }

    private void addRestClasses(Set<Class<?>> resources) {
        resources.add(ShareRest.class);
        resources.add(FilesRest.class);
        resources.add(FileRest.class);
        resources.add(UserRest.class);
    }
}
