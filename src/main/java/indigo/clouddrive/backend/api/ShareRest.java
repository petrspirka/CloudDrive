package indigo.clouddrive.backend.api;

import indigo.clouddrive.backend.contracts.ShareManager;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/shares")
public class ShareRest {
    @EJB
    private ShareManager shareManager;

    @EJB
    private RestHelpers restHelpers;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getShares(@HeaderParam("Authorization") String appPassword, @QueryParam("path") String path){
        return restHelpers.authorizeUserThen(appPassword, (u) ->
            restHelpers.getStorageObjectThen(u, path, (o) ->
                restHelpers.getSafeObjectFromFunctionThen(() -> shareManager.getShares(o), "Could not get shares for the specified storage object", restHelpers::getSuccessResponse)
            )
        );
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response addShare(@HeaderParam("Authorization") String appPassword, @QueryParam("path") String path, @QueryParam("username") String username, @QueryParam("canWrite") boolean canWrite){
        return restHelpers.authorizeUserThen(appPassword, (u) ->
            restHelpers.getStorageObjectThen(u, path, (o) ->
                restHelpers.getUserByUsernameThen(username, (other) ->
                    restHelpers.runSafelyThen(() -> shareManager.addShare(o, other, canWrite), "Failed to add the specified share", restHelpers::getSuccessResponse)
                )
            )
        );
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeShare(@HeaderParam("Authorization") String appPassword, @QueryParam("path") String path, @QueryParam("username") String username){
        return restHelpers.authorizeUserThen(appPassword, (u) ->
            restHelpers.getStorageObjectThen(u, path, (o) ->
                restHelpers.getUserByUsernameThen(username, (other) ->
                    restHelpers.runSafelyThen(() -> shareManager.removeShare(o, other), "Failed to add the specified share", restHelpers::getSuccessResponse)
                )
            )
        );
    }
}
