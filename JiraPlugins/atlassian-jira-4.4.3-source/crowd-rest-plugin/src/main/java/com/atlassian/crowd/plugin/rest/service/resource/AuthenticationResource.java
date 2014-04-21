package com.atlassian.crowd.plugin.rest.service.resource;

import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationAccessDeniedException;
import com.atlassian.crowd.plugin.rest.entity.ErrorEntity;
import com.atlassian.crowd.plugin.rest.entity.PasswordEntity;
import com.atlassian.crowd.plugin.rest.entity.UserEntity;
import com.atlassian.crowd.plugin.rest.service.controller.AuthenticationController;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * User Authentication Resource.
 */
@Path("authentication")
@AnonymousAllowed
@Produces({APPLICATION_XML, APPLICATION_JSON})
public class AuthenticationResource extends AbstractResource
{
    private final AuthenticationController authenticationController;

    public AuthenticationResource(final AuthenticationController authenticationController)
    {
        this.authenticationController = authenticationController;
    }

    /**
     * Authenticates a user. Does not generate an SSO token. See
     * {@link com.atlassian.crowd.plugin.rest.service.resource.TokenResource} for the resource handling SSO.
     *
     * @param username name of the user
     * @param password password of the user
     */
    @POST
    public Response authenticateUser(@QueryParam ("username") String username, PasswordEntity password)
            throws ApplicationAccessDeniedException, ExpiredCredentialException, InactiveAccountException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            final String applicationName = getApplicationName();
            final UserEntity userEntity = authenticationController.authenticateUser(applicationName, username, password, uriInfo.getBaseUri());
            return Response.ok(userEntity).build();
        }
        catch (UserNotFoundException e)
        {
            final ErrorEntity errorEntity = new ErrorEntity(ErrorEntity.ErrorReason.of(e), e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(errorEntity).build();
        }
    }
}
