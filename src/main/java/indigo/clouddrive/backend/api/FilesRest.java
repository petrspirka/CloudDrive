package indigo.clouddrive.backend.api;

import indigo.clouddrive.backend.contracts.UserStorage;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/files")
public class FilesRest {
    @EJB
    private UserStorage userStorage;

    @EJB
    private RestHelpers restHelpers;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStorageObject(@HeaderParam("Authorization") String appPassword, @QueryParam("path") String path){
        return restHelpers.authorizeUserThen(appPassword, (u) ->
            restHelpers.getStorageObjectThen(u, path, restHelpers::getSuccessResponse)
        );
    }

    @GET
    @Path("/contents")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContents(@HeaderParam("Authorization") String appPassword, @QueryParam("path") String path){
        return restHelpers.authorizeUserThen(appPassword, (u) ->
            restHelpers.getStorageObjectThen(u, path, (o) -> restHelpers.getSuccessResponse(o.getChildren()))
        );
    }

    @PUT
    @Path("/folder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addFolder(@HeaderParam("Authorization") String appPassword, @QueryParam("path") String path){
        return restHelpers.authorizeUserThen(appPassword, (u) ->
            restHelpers.getSafeParameterThen(path, "path", (p) ->
                restHelpers.runSafelyThen(() -> userStorage.createFolder(u, p), "Could not create the specified folder", restHelpers::getSuccessResponse)
            )
        );
    }

    @PUT
    @Path("/file")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addFile(@HeaderParam("Authorization") String appPassword, @QueryParam("path") String path){
        return restHelpers.authorizeUserThen(appPassword, (u) ->
            restHelpers.getSafeParameterThen(path, "path", (p) ->
                restHelpers.runSafelyThen(() -> userStorage.createFile(u, p), "Could not create the specified file", restHelpers::getSuccessResponse)
            )
        );
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeObject(@HeaderParam("Authorization") String appPassword, @QueryParam("path") String path){
        return restHelpers.authorizeUserThen(appPassword, (u) ->
            restHelpers.getSafeParameterThen(path, "path", (p) ->
                restHelpers.runSafelyThen(() -> userStorage.deleteStorageObject(u, p), "Could not delete the specified folder", restHelpers::getSuccessResponse)
            )
        );
    }
}
