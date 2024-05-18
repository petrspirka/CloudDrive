package indigo.clouddrive.backend.api;

import indigo.clouddrive.backend.contracts.UserManager;
import indigo.clouddrive.backend.contracts.UserStorage;
import indigo.clouddrive.backend.helpers.CryptoHelpers;
import indigo.clouddrive.backend.models.StorageObject;
import indigo.clouddrive.backend.models.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

@Stateless
public class RestHelpers {
    @EJB
    private UserManager userManager;

    @EJB
    private UserStorage userStorage;

    public Response authorizeUserThen(String appPassword, Function<User, Response> then){
        return getSafeParameterThen(appPassword, "Authorization", (p) ->
            getSafeObjectFromFunctionThen(() -> userManager.getUser(CryptoHelpers.getHash(appPassword)), "Could not find the user with the specified application password", then)
        );
    }

    public Response getUserByUsernameThen(String username, Function<User, Response> then){
        return getUserByUsernameThen(username, "username", then);
    }
    public Response getUserByUsernameThen(String username, String parameterName, Function<User, Response> then){
        return getSafeParameterThen(username, parameterName, (p) ->
                getSafeObjectFromFunctionThen(() -> userManager.getUserUnsafe(p), "Could not find the user with username '" + username + "' specified by parameter '" + parameterName + "'", then)
        );
    }

    public Response getSafeParameterThen(String parameter, String parameterName, Function<String, Response> then){
        return getSafeObjectThen(parameter, parameterName, (p) -> {
            if(parameter.isEmpty()){
                return getBadResponse("Missing '" + parameterName + "' parameter");
            }
            return then.apply(parameter);
        });
    }

    public Response runSafelyThen(UnsafeRunnable runnable, String errorMessage, Supplier<Response> then){
        try {
            runnable.run();
            return then.get();
        }
        catch(Exception ignored){
            return getBadResponse(errorMessage);
        }
    }

    public <T> Response getSafeObjectThen(T object, String errorMessage, Function<T, Response> then){
        if(object == null){
            return getBadResponse(errorMessage);
        }
        return then.apply(object);
    }

    public <T> Response getSafeObjectFromFunctionThen(Callable<T> unsafeFunction, String errorMessage, Function<T, Response> then){
        try {
            return getSafeObjectThen(unsafeFunction.call(), errorMessage, then);
        }
        catch(Exception ignored) {
            return getBadResponse(errorMessage);
        }
    }

    public Response getStorageObjectThen(User user, String path, Function<StorageObject, Response> then){
        return getStorageObjectThen(user, path, false, then);
    }
    //Part of public API
    @SuppressWarnings("unused")
    public Response getStorageObjectThen(User user, String path, String parameterName, Function<StorageObject, Response> then){
        return getStorageObjectThen(user, path, false, parameterName, then);
    }
    public Response getStorageObjectThen(User user, String path, boolean canWrite, Function<StorageObject, Response> then){
        return getStorageObjectThen(user, path, canWrite, "path", then);
    }
    public Response getStorageObjectThen(User user, String path, boolean canWrite, String parameterName, Function<StorageObject, Response> then){
        return getSafeObjectThen(user, "Could not find the specified user", (u) ->
            getSafeParameterThen(path, parameterName, (p) ->
                getSafeObjectFromFunctionThen(() -> userStorage.getStorageObject(u, p, canWrite), "Could not find the specified storage object", then)
            )
        );
    }

    public interface UnsafeRunnable {
        void run() throws Exception;
    }

    public Response getBadResponse(String message){
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    public Response getSuccessResponse(Object result){
        return Response.ok(result).build();
    }

    public Response getSuccessResponse(){
        return Response.ok().build();
    }
}
