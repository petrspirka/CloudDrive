package indigo.clouddrive.backend.api;

import indigo.clouddrive.backend.contracts.FileManager;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.nio.charset.StandardCharsets;

@Path("/file")
public class FileRest {
    private @Inject FileManager fileManager;

    @EJB
    private RestHelpers restHelpers;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContent(@HeaderParam("Authorization") String appPassword, @QueryParam("path") String path){
        return restHelpers.authorizeUserThen(appPassword, (u) ->
            restHelpers.getStorageObjectThen(u, path, (o) ->
                restHelpers.getSafeObjectFromFunctionThen(() -> new String(fileManager.getContent(o), StandardCharsets.UTF_8), "Could not read the file contents. It is possible that the file is locked by another user", restHelpers::getSuccessResponse)
            )
        );
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response setContent(@HeaderParam("Authorization") String appPassword, @QueryParam("path") String path, @FormParam("content") String content){
        return restHelpers.authorizeUserThen(appPassword, (u) ->
            restHelpers.getSafeObjectThen(content, "Missing 'content' form parameter in request body", (c) ->
                restHelpers.getStorageObjectThen(u, path, (o) ->
                    restHelpers.runSafelyThen(() -> fileManager.setContent(o, c.getBytes(StandardCharsets.UTF_8)), "Could not set the file contents. It is possible that the file is locked by another user", restHelpers::getSuccessResponse)
                )
            )
        );
    }
}
