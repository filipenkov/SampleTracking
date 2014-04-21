package com.atlassian.upm.test.rest.resources;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.Sys;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.UpmResources;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.token.TokenManager;

import static com.atlassian.upm.Sys.isUpmDebugModeEnabled;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

import static javax.ws.rs.core.MediaType.TEXT_HTML;


/** This REST resource is only used for testing purpose: it allows simulation of XSRF token failure.
 */
@Path("/tokens")
public class TokenControlResource
{
    private final PermissionEnforcer permissionEnforcer;
    private final TokenManager tokenManager;
    private final UserManager userManager;
    private final RepresentationFactory representationFactory;
    
    public TokenControlResource(PermissionEnforcer permissionEnforcer,
                                TokenManager tokenManager,
                                UserManager userManager,
                                RepresentationFactory representationFactory)
    {
        this.permissionEnforcer = permissionEnforcer;
        this.tokenManager = tokenManager;
        this.userManager = userManager;
        this.representationFactory = representationFactory;
    }

    /**
     * Validates and consumes a token.
     */
    @Path("/consume")
    @POST
    public Response consumeToken(@QueryParam("token") String token)
    {
        permissionEnforcer.enforceSystemAdmin();
        if (!isUpmDebugModeEnabled())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }

        UpmResources.validateToken(token, userManager.getRemoteUsername(), TEXT_HTML, tokenManager, representationFactory);
        return Response.ok().build();
    }
    
    /**
     * Sets a system property which, if true, causes all XSRF tokens to be rejected.
     */
    @Path("/override")
    @PUT
    public Response setOverride(@QueryParam("disable") boolean disable)
    {
        permissionEnforcer.enforceSystemAdmin();
        if (!isUpmDebugModeEnabled())
        {
            return Response.status(PRECONDITION_FAILED).build();
        }
        System.setProperty(Sys.UPM_XSRF_TOKEN_DISABLE, Boolean.toString(disable));
        return Response.ok().build();
    }
}