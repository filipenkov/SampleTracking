package com.atlassian.crowd.plugin.rest.service.resource;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidTokenException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.TokenExpiredException;
import com.atlassian.crowd.exception.TokenNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationAccessDeniedException;
import com.atlassian.crowd.plugin.rest.entity.AuthenticationContextEntity;
import com.atlassian.crowd.plugin.rest.entity.ErrorEntity;
import com.atlassian.crowd.plugin.rest.entity.SessionEntity;
import com.atlassian.crowd.plugin.rest.entity.UserEntity;
import com.atlassian.crowd.plugin.rest.entity.ValidationFactorEntity;
import com.atlassian.crowd.plugin.rest.entity.ValidationFactorEntityList;
import com.atlassian.crowd.plugin.rest.service.controller.TokenController;
import com.atlassian.crowd.plugin.rest.service.util.CacheControl;
import com.atlassian.crowd.plugin.rest.util.LinkUriHelper;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Crowd SSO Token Resource.
 */
@Path("session")
@AnonymousAllowed
@Produces({APPLICATION_XML, APPLICATION_JSON})
public class TokenResource extends AbstractResource
{
    private final TokenController tokenController;

    public TokenResource(TokenController tokenController)
    {
        this.tokenController = tokenController;
    }

    /**
     * Authenticates a user against the {@code application} and returns a Crowd SSO token.
     *
     * @param authenticationContext authentication information
     * @param validatePassword true if the password should be validated (optional, defaults to true)
     * @return status 200 with Crowd SSO token if successful
     * @throws InvalidAuthenticationException
     * @throws InactiveAccountException
     * @throws ExpiredCredentialException
     * @throws ApplicationAccessDeniedException
     * @throws OperationFailedException
     *
     * @request.representation.qname
     *      authentication-context
     *
     * @response.representation.200.qname
     *      token
     *
     * @response.representation.200.mediaType
     *      {application/xml, application/json}
     *
     * @response.representation.200.doc
     *      Returned if the user authentication details are valid. Contains the Crowd SSO token.
     *
     * @response.representation.401.doc
     *      Returned if the user is not allowed to authenticate.
     * @response.representation.403.doc
     *      Returned if the user authentication details are not valid.
     */
    @POST
    public Response authenticateUser(AuthenticationContextEntity authenticationContext, @DefaultValue("true") @QueryParam("validate-password") boolean validatePassword)
            throws InvalidAuthenticationException, InactiveAccountException, ExpiredCredentialException, ApplicationAccessDeniedException, OperationFailedException
    {
        final String applicationName = getApplicationName();
        String username = authenticationContext.getUserName();
        String password = authenticationContext.getPassword();
        List<ValidationFactorEntity> validationFactors = authenticationContext.getValidationFactors();

        final SessionEntity session;
        if (validatePassword)
        {
            session = tokenController.authenticateUser(applicationName, username, password, validationFactors, getBaseUri());
        }
        else
        {
            session = tokenController.authenticateUserWithoutValidatingPassword(applicationName, username, validationFactors, getBaseUri());
        }

        return Response.created(LinkUriHelper.buildSessionUri(getBaseUri(), session.getToken())).entity(session).cacheControl(CacheControl.NO_CACHE).build();
    }

    /**
     * Invalidates the Crowd SSO token.
     * 
     * @param token
     * @return
     */
    @DELETE
    @Path("{token}")
    public Response invalidateToken(@PathParam("token") String token)
    {
        tokenController.invalidateToken(token);
        return Response.noContent().build();
    }

    /**
     * Validates a Crowd SSO token.
     *
     * @param token Crowd SSO token
     * @param validationFactors list of validation factors
     */
    @POST
    @Path("{token}")
    public Response validateToken(@PathParam("token") String token, ValidationFactorEntityList validationFactors)
            throws InvalidTokenException, ApplicationAccessDeniedException, OperationFailedException
    {
        final String applicationName = getApplicationName();
        List<ValidationFactorEntity> factors = validationFactors.getValidationFactors();

        try
        {
            final SessionEntity session = tokenController.validateToken(applicationName, token, factors, getBaseUri());
            return Response.ok(session).cacheControl(CacheControl.NO_CACHE).build();
        }
        catch (TokenNotFoundException e)
        {
            final ErrorEntity error = new ErrorEntity(ErrorEntity.ErrorReason.of(e), e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        catch (TokenExpiredException e)
        {
            final ErrorEntity error = new ErrorEntity(ErrorEntity.ErrorReason.of(e), e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
    }

    /**
     * Retrieves the token.
     * @throws ApplicationAccessDeniedException
     */
    @GET
    @Path("{token}")
    public Response getSession(@PathParam("token") String token) throws InvalidTokenException, OperationFailedException, ApplicationAccessDeniedException
    {
        final String applicationName = getApplicationName();
        final UserEntity user = tokenController.getUserFromToken(token, applicationName, getBaseUri());
        final Link link = LinkUriHelper.buildSessionLink(getBaseUri(), token);
        return Response.ok(new SessionEntity(token, user, link)).build();
    }
}
