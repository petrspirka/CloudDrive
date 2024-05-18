package indigo.clouddrive.backend.api;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/user")
public class UserRest {
    @EJB
    private RestHelpers restHelpers;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSelf(@HeaderParam("Authorization") String appPassword) {
        return restHelpers.authorizeUserThen(appPassword, restHelpers::getSuccessResponse);
    }
}
