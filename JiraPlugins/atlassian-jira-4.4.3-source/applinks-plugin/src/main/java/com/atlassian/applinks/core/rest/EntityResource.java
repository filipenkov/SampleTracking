package com.atlassian.applinks.core.rest;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.core.rest.client.EntityRetriever;
import com.atlassian.applinks.core.rest.context.ContextInterceptor;
import com.atlassian.applinks.core.rest.model.ReferenceEntityList;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.net.ResponseException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.applinks.core.rest.util.RestUtil.badRequest;
import static com.atlassian.applinks.core.rest.util.RestUtil.credentialsRequired;
import static com.atlassian.applinks.core.rest.util.RestUtil.notFound;
import static com.atlassian.applinks.core.rest.util.RestUtil.ok;
import static com.atlassian.applinks.core.rest.util.RestUtil.serverError;

/**
 * <p>
 * This service has two responsibilities:
 * <ol>
 * <li>
 * Returning a list of entities local to this instance that are accessible to the logged in user. For example:
 * {@code
 * <entities>
 * <entity type="fisheye-repository">FE</entity>
 * <entity type="fisheye-repository">JFEP</entity>
 * <entity type="crucible-project">CR-FE</entity>
 * </entities>
 * }
 * </li>
 * <li>
 * Proxying the above method from a corresponding {@link EntityResource} in a remote application instance
 * (allowing local views to retrieve lists of remote entities)
 * </li>
 * </ol>
 * </p>
 * <p>
 * <p/>
 * </p>
 */
@Path(EntityResource.CONTEXT)
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@InterceptorChain({ContextInterceptor.class})
public class EntityResource
{
    public static final String CONTEXT = "entities";

    private final InternalHostApplication internalHostApplication;
    private final ApplicationLinkService applicationLinkService;
    private final EntityRetriever entityRetriever;
    private final I18nResolver i18nResolver;

    public EntityResource(final InternalHostApplication internalHostApplication,
                          final ApplicationLinkService applicationLinkService, final EntityRetriever entityRetriever,
                          final I18nResolver i18nResolver)
    {
        this.internalHostApplication = internalHostApplication;
        this.applicationLinkService = applicationLinkService;
        this.entityRetriever = entityRetriever;
        this.i18nResolver = i18nResolver;
    }

    /**
     * Method to return all entities that are visible (user has permissions to see them) for a user.
     * User can be null, when using anonymous access. The host application has to 'protect' the entities and apply
     * the detect if the user has sufficient permissions.
     *
     * @return a {@link com.atlassian.applinks.core.rest.model.ReferenceEntityList} of entity references retrieved from the host application,
     *         considering the user who has requested this list.
     */
    @GET
    @AnonymousAllowed
    public Response listEntities()
    {
        final Iterable<EntityReference> refs = internalHostApplication.getLocalEntities();
        return ok(new ReferenceEntityList(refs));
    }

    /**
     * Proxy method for retrieving a list of entities published from a remote application instance
     *
     * @param applicationId the id of the remote server
     * @return a {@link com.atlassian.applinks.core.rest.model.ReferenceEntityList} of entity references retrieved from the specified application
     */
    @GET
    @Path("{applinkId}")
    public Response listEntities(@PathParam("applinkId") final String applicationId)
    {
        return listEntities(applicationId, false);
    }

    private Response listEntities(final String applicationId, final boolean useAnonymousAccess)
    {
        final ApplicationLink link;
        try
        {
            link = applicationLinkService.getApplicationLink(new ApplicationId(applicationId));
        }
        catch (TypeNotInstalledException e)
        {
            return badRequest(String.format("Failed to load application %s as the %s type is not installed",
                    applicationId, e.getType()));
        }

        Response response;
        if (link == null)
        {
            response = notFound("No application link found with id: " + applicationId);
        }
        else
        {
            try
            {
                if (useAnonymousAccess)
                {
                    response = ok(new ReferenceEntityList(entityRetriever.getEntitiesForAnonymousAccess(link)));
                }
                else
                {
                    response = ok(new ReferenceEntityList(entityRetriever.getEntities(link)));
                }
            }
            catch (CredentialsRequiredException e)
            {
                response = credentialsRequired(i18nResolver);
            }
            catch (ResponseException e)
            {
                response = serverError(e.toString());
            }
        }
        return response;
    }

    @GET
    @Path ("anonymous/{applinkId}")
    public Response listEntitiesForAnonymousAccess(@PathParam ("applinkId") final String applicationId)
    {
        return listEntities(applicationId, true);
    }

}
