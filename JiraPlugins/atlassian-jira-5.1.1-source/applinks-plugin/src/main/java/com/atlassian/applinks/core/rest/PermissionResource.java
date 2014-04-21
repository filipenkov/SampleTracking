package com.atlassian.applinks.core.rest;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.link.MutatingEntityLinkService;
import com.atlassian.applinks.core.rest.model.PermissionCodeEntity;
import com.atlassian.applinks.core.rest.permission.PermissionCode;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ReturningResponseHandler;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static com.atlassian.applinks.core.rest.permission.PermissionCode.AUTHENTICATION_FAILED;
import static com.atlassian.applinks.core.rest.permission.PermissionCode.CREDENTIALS_REQUIRED;
import static com.atlassian.applinks.core.rest.permission.PermissionCode.NO_CONNECTION;
import static com.atlassian.applinks.core.rest.util.RestUtil.*;

/**
 * @since v3.0
 */
@Path("permission")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class PermissionResource extends AbstractResource
{
    private static final Logger LOG = LoggerFactory.getLogger(PermissionResource.class);

    private final UserManager userManager;
    private final AdminUIAuthenticator uiAuthenticator;
    private final MutatingApplicationLinkService applicationLinkService;
    private final InternalHostApplication internalHostApplication;
    private final MutatingEntityLinkService entityLinkService;

    public PermissionResource(final UserManager userManager,
                              final AdminUIAuthenticator uiAuthenticator,
                              final MutatingApplicationLinkService applicationLinkService,
                              final InternalHostApplication internalHostApplication,
                              final MutatingEntityLinkService entityLinkService,
                              final InternalTypeAccessor typeAccessor,
                              final RestUrlBuilder restUrlBuilder,
                              final RequestFactory requestFactory)
    {
        super (restUrlBuilder, typeAccessor, requestFactory, applicationLinkService);
        this.userManager = userManager;
        this.uiAuthenticator = uiAuthenticator;
        this.applicationLinkService = applicationLinkService;
        this.internalHostApplication = internalHostApplication;
        this.entityLinkService = entityLinkService;
    }

    /**
     * Determines whether the context user can delete the specified application link
     */
    @GET
    @Path("delete-application/{id}")
    public Response canDeleteApplicationLink(@PathParam("id") final ApplicationId id)
    {
        return response(hasPermissionToModify(id));
    }

    /**
     * Proxy method for determining whether the current user can delete the local application reference from the target
     * application specified by the id attribute. Passes the local application's id to the {@link #canDeleteApplicationLink} REST
     * method in the remote application.
     */
    @GET
    @Path("reciprocate-application-delete/{id}")
    public Response canDeleteReciprocalApplicationLink(@PathParam("id") final ApplicationId id)
    {
        return checkPermissionFor(id, new RestMethodUrlProvider()
        {
            public String getRestMethodUrl(final ApplicationLink link)
            {
                return getUrlFor(getBaseRestUri(link), PermissionResource.class)
                        .canDeleteApplicationLink(internalHostApplication.getId()).toString(); // pass THIS server's id to remote server
            }
        });
    }

    /**
     * Determines whether the context user can create an entity link belonging to the specified application link
     */
    @GET
    @Path("create-entity/{id}")
    public Response canCreateEntityLink(@PathParam("id") final ApplicationId id)
    {
        return response(hasPermissionToModify(id));
    }

    /**
     * Proxy method for determining whether the current user can create an entity link belonging to the local
     * application from the target application specified by the id attribute. Passes the local application's id to the
     * {@link #canCreateEntityLink(ApplicationId)} REST method in the remote application.
     */
    @GET
    @Path("reciprocate-entity-create/{id}")
    public Response canCreateReciprocalEntityLink(@PathParam("id") final ApplicationId id)
    {
        return checkPermissionFor(id, new RestMethodUrlProvider()
        {
            public String getRestMethodUrl(final ApplicationLink link)
            {
                return getUrlFor(getBaseRestUri(link), PermissionResource.class)
                        .canCreateEntityLink(internalHostApplication.getId()).toString(); // pass THIS server's id to remote server
            }
        });
    }

    @GET
    @Path("delete-entity/{id}/{localType}/{localKey}/{remoteType}/{remoteKey}")
    public Response canDeleteEntityLink(@PathParam("id") final ApplicationId applicationId,
                                        @PathParam("localType") final TypeId localTypeId,
                                        @PathParam("localKey") final String localKey,
                                        @PathParam("remoteType") final TypeId remoteTypeId,
                                        @PathParam("remoteKey") final String remoteKey)
    {
        final PermissionCode canModifyApp = hasPermissionToModify(applicationId);
        if (canModifyApp != PermissionCode.ALLOWED)
        {
            return response(canModifyApp); // if the user can't modify the application link, they can't delete the entity link
        }

        final EntityType localType = typeAccessor.loadEntityType(localTypeId);
        if (localType == null)
        {
            return typeNotInstalled(localTypeId); // todo use exception mapper
        }
        final EntityType remoteType = typeAccessor.loadEntityType(remoteTypeId);
        if (remoteType == null)
        {
            return typeNotInstalled(remoteTypeId); // todo use exception mapper
        }
        if (entityLinkService.getEntityLink(localKey, localType.getClass(), remoteKey, remoteType.getClass(), applicationId) == null)
        {
            return response(PermissionCode.MISSING); // no matching entity link for this application
        }

        return response(PermissionCode.ALLOWED);
    }

    @GET
    @Path("reciprocate-entity-delete/{id}/{localType}/{localKey}/{remoteType}/{remoteKey}")
    public Response canDeleteReciprocalEntityLink(@PathParam("id") final ApplicationId applicationId,
                                                  @PathParam("localType") final TypeId localTypeId,
                                                  @PathParam("localKey") final String localKey,
                                                  @PathParam("remoteType") final TypeId remoteTypeId,
                                                  @PathParam("remoteKey") final String remoteKey)
    {
        return checkPermissionFor(applicationId, new RestMethodUrlProvider()
        {
            public String getRestMethodUrl(final ApplicationLink link)
            {
                return getUrlFor(getBaseRestUri(link), PermissionResource.class)
                        .canDeleteEntityLink(internalHostApplication.getId(), remoteTypeId, remoteKey,
                                localTypeId, localKey)
                        .toString();
            }
        });

    }

    private PermissionCode hasPermissionToModify(final ApplicationId id)
    {
        if (userManager.getRemoteUsername() == null)
        {
            return PermissionCode.NO_AUTHENTICATION;
        }

        if (!uiAuthenticator.canCurrentUserAccessAdminUI())
        {
            return PermissionCode.NO_PERMISSION;
        }

        ApplicationLink applicationLink = null;
        try
        {
            applicationLink = applicationLinkService.getApplicationLink(id);
        }
        catch (TypeNotInstalledException e)
        {
            // ignore, uninstalled type is equivalent to missing for reciprocation purposes
        }
        if (applicationLink == null)
        {
            return PermissionCode.MISSING;
        }

        return PermissionCode.ALLOWED;
    }

    private Response checkPermissionFor(final ApplicationId id, final RestMethodUrlProvider restMethodProvider)
    {
        ApplicationLink tempLink = null;
        try
        {
            tempLink = applicationLinkService.getApplicationLink(id);
        }
        catch (TypeNotInstalledException e)
        {
            // ignore, type not found is equivalent to non-existent
        }

        if (tempLink == null)
        {
            return notFound(String.format("No link found with id %s", id));
        }

        final ApplicationLink applicationLink = tempLink;

        final ApplicationLinkRequestFactory authenticatedRequestFactory =
                applicationLink.createAuthenticatedRequestFactory();

        final String url = restMethodProvider.getRestMethodUrl(applicationLink);

        PermissionCode permissionState;
        try
        {
            permissionState = authenticatedRequestFactory
                    .createRequest(Request.MethodType.GET, url)
                    .executeAndReturn(new ReturningResponseHandler<com.atlassian.sal.api.net.Response,PermissionCode>()
                    {
                        public PermissionCode handle(final com.atlassian.sal.api.net.Response response) throws ResponseException
                        {
                            if (response.getStatusCode() == 200)
                            {
                                try
                                {
                                    return response.getEntity(PermissionCodeEntity.class).getCode();
                                }
                                catch (Exception e)
                                {
                                    throw new ResponseException(
                                            String.format("Permission check failed, exception " +
                                                    "encountered processing response: %s", e));
                                }
                            }
                            else if (response.getStatusCode() == 401)
                            {
                                final ApplicationLinkRequestFactory authenticatedRequestFactory =
                                        applicationLink.createAuthenticatedRequestFactory(AuthenticationProvider.class);
                                //NOT NULL means we were NOT using anonymous access, thus something went wrong when trying to authenticate using a particular authentication type method.
                                if (authenticatedRequestFactory != null)
                                {
                                   LOG.warn("Authentication failed for application link " +
                                           applicationLink + ". Response headers: " + response.getHeaders().toString() +
                                           " body: " + response.getResponseBodyAsString());
                                }
                                else if (LOG.isDebugEnabled())
                                {
                                    LOG.debug("Authentication failed for application link " +
                                            applicationLink + ". Response headers: " + response.getHeaders().toString() +
                                            " body: " + response.getResponseBodyAsString());
                                }
                                return AUTHENTICATION_FAILED;
                            }
                            else
                            {
                                throw new ResponseException(String.format("Permission check failed, received %s",
                                        response.getStatusCode()));
                            }
                        }
                    });
        }
        catch (CredentialsRequiredException e)
        {
            permissionState = CREDENTIALS_REQUIRED;
        }
        catch (ResponseException e)
        {
            LOG.error(String.format("Failed to perform permission check for %s",
                    applicationLink.getRpcUrl()), e);
            permissionState = NO_CONNECTION;
        }

        // add the authorisation URI to the response if credentials are missing/potentially invalid
        switch (permissionState)
        {
            case CREDENTIALS_REQUIRED:
            case AUTHENTICATION_FAILED:
            case NO_AUTHENTICATION:
                return response(permissionState, authenticatedRequestFactory.getAuthorisationURI());
            default:
                return response(permissionState);
        }
    }

    private Response response(final PermissionCode code)
    {
        return ok(new PermissionCodeEntity(code));
    }

    private Response response(final PermissionCode code, final URI authorisationUri)
    {
        return ok(new PermissionCodeEntity(code, authorisationUri));
    }

    private interface RestMethodUrlProvider
    {
        /**
         * @param link the {@link ApplicationLink} that will be the target of this request
         * @return the {@link String} url of the target REST method
         */
        String getRestMethodUrl(ApplicationLink link);
    }

}
