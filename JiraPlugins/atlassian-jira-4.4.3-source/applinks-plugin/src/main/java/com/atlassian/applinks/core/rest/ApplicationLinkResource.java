package com.atlassian.applinks.core.rest;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.rest.auth.AdminApplicationLinksInterceptor;
import com.atlassian.applinks.core.rest.context.ContextInterceptor;
import com.atlassian.applinks.core.rest.model.ApplicationLinkEntity;
import com.atlassian.applinks.core.rest.model.ApplicationLinkListEntity;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.link.MutableApplicationLink;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.link.ReciprocalActionException;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.websudo.WebSudoNotRequired;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.sun.jersey.spi.resource.Singleton;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.applinks.core.rest.util.RestUtil.badRequest;
import static com.atlassian.applinks.core.rest.util.RestUtil.created;
import static com.atlassian.applinks.core.rest.util.RestUtil.credentialsRequired;
import static com.atlassian.applinks.core.rest.util.RestUtil.notFound;
import static com.atlassian.applinks.core.rest.util.RestUtil.ok;
import static com.atlassian.applinks.core.rest.util.RestUtil.serverError;
import static com.atlassian.applinks.core.rest.util.RestUtil.typeNotInstalled;
import static com.atlassian.applinks.core.rest.util.RestUtil.updated;

/*

 This rest end point exposes an API to retrieve, create, update and delete ApplicationLinks.

  TODO as per REST Guidelines @ http://confluence.atlassian.com/display/DEVNET/Atlassian+REST+API+Design+Guidelines+version+1
                                                                                      `
  1. <link>s
  2. JSONP ?
  3. Entity IDs
  4. Authentication
  5. Authorisation
  6. Check XSRF (do we need to do this? does atlassian-rest handle it for us?
     see http://confluence.atlassian.com/display/DEVNET/Atlassian+REST+API+Design+Guidelines+version+1#AtlassianRESTAPIDesignGuidelinesversion1-XSRF
  7. Return <status> for responses with no body, incl. 201 (created) for created entities

  What we won't be doing:

  1. Expansion (won't need it for applications due to the relatively low entity count)

  @since 3.0
 */

@Path(ApplicationLinkResource.CONTEXT)
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Singleton
@WebSudoRequired
@InterceptorChain ({ ContextInterceptor.class, AdminApplicationLinksInterceptor.class })
public class ApplicationLinkResource extends AbstractResource
{
    public static final String CONTEXT = "applicationlink";

    private final MutatingApplicationLinkService applicationLinkService;
    private final ManifestRetriever manifestRetriever;
    private final I18nResolver i18nResolver;

    public ApplicationLinkResource(final MutatingApplicationLinkService applicationLinkService,
                                   final I18nResolver i18nResolver,
                                   final InternalTypeAccessor typeAccessor,
                                   final ManifestRetriever manifestRetriever,
                                   final RestUrlBuilder restUrlBuilder,
                                   final RequestFactory requestFactory)
    {
        super(restUrlBuilder, typeAccessor, requestFactory, applicationLinkService);
        this.i18nResolver = i18nResolver;
        this.applicationLinkService = applicationLinkService;
        this.manifestRetriever = manifestRetriever;
    }

    @GET
    public Response getApplicationLinks()
    {
        final List<ApplicationLinkEntity> applicationLinks = new ArrayList<ApplicationLinkEntity>();
        for (final ApplicationLink application : applicationLinkService.getApplicationLinks())
        {
            applicationLinks.add(toApplicationLinkEntity(application));
        }
        return ok(new ApplicationLinkListEntity(applicationLinks));
    }

    @GET
    @Path ("type/{type}")
    public Response getApplicationLinks(@PathParam ("type") final TypeId typeId)
    {
        final ApplicationType type = typeAccessor.loadApplicationType(typeId);
        if (type == null) {
            return typeNotInstalled(typeId);
        }
        final List<ApplicationLinkEntity> applicationLinks = new ArrayList<ApplicationLinkEntity>();
        for (final ApplicationLink application : applicationLinkService.getApplicationLinks(type.getClass()))
        {
            applicationLinks.add(toApplicationLinkEntity(application));
        }
        return ok(new ApplicationLinkListEntity(applicationLinks));
    }

    @GET
    @Path ("{id}")
    public Response getApplicationLink(@PathParam ("id") final String id) throws TypeNotInstalledException
    {
        final ApplicationLink application = applicationLinkService.getApplicationLink(new ApplicationId(id));
        return ok(toApplicationLinkEntity(application));
    }

    @GET
    @Path ("primary/{type}")
    public Response getPrimaryApplicationLink(@PathParam ("type") final TypeId typeId)
    {
        final ApplicationType type = typeAccessor.loadApplicationType(typeId);
        if (type == null)
        {
            return typeNotInstalled(typeId);
        }
        final ApplicationLink application = applicationLinkService.getPrimaryApplicationLink(type.getClass());
        if (application == null)
        {
            return notFound(i18nResolver.getText("applinks.error.noprimary", type.getClass()));
        }
        return ok(toApplicationLinkEntity(application));
    }

    @PUT
    @Path ("{id}")
    public Response updateApplicationLink(@PathParam ("id") final String id, final ApplicationLinkEntity applicationLink)
            throws TypeNotInstalledException
    {
        try
        {
            final ApplicationType applicationType = typeAccessor.loadApplicationType(applicationLink.getTypeId());
            if (applicationType == null)
            {
                LOG.warn(String.format("Couldn't load type %s for application link. Type is not installed?", applicationLink.getTypeId()));
                throw new TypeNotInstalledException(applicationLink.getTypeId().get());
            }
            manifestRetriever.getManifest(applicationLink.getRpcUrl(), applicationType);
            final MutableApplicationLink existing = applicationLinkService.getApplicationLink(new ApplicationId(id));

            if (existing == null)
            {
                final ApplicationType type = typeAccessor.loadApplicationType(applicationLink.getTypeId());
                applicationLinkService.addApplicationLink(applicationLink.getId(), type, applicationLink.getDetails());
                return created(createSelfLinkFor(applicationLink.getId()));
            }
            else
            {
                existing.update(applicationLink.getDetails());
                return updated(createSelfLinkFor(applicationLink.getId()));
            }
        }
        catch (ManifestNotFoundException e)
        {
            return badRequest(i18nResolver.getText("applinks.error.url.application.not.reachable", applicationLink.getRpcUrl().toString()));
        }
    }

    @DELETE
    @WebSudoNotRequired
    @Path ("{id}")
    public Response deleteApplicationLink(@PathParam ("id") final String idString,
                                          @QueryParam ("reciprocate") final Boolean reciprocate)
            throws TypeNotInstalledException
    {
        final ApplicationId id = new ApplicationId(idString);
        final MutableApplicationLink link = applicationLinkService.getApplicationLink(id);
        if (link == null)
        {
            return notFound(i18nResolver.getText("applinks.notfound", id.get()));
        }
        if (reciprocate != null && reciprocate)
        {
            try
            {
                applicationLinkService.deleteReciprocatedApplicationLink(link);
            }
            catch (final CredentialsRequiredException e)
            {
                return credentialsRequired(i18nResolver);
            }
            catch (final ReciprocalActionException e)
            {
                return serverError(i18nResolver.getText("applinks.remote.delete.failed", e.getMessage()));
            }
        }
        else
        {
            applicationLinkService.deleteApplicationLink(link);
        }
        return ok(i18nResolver.getText("applinks.deleted", id.get()));
    }

    @POST
    @Path ("primary/{id}")
    public Response makePrimary(@PathParam ("id") final String idString) throws TypeNotInstalledException
    {
        final ApplicationId id = new ApplicationId(idString);
        applicationLinkService.makePrimary(id);
        return updated(Link.self(applicationLinkService.createSelfLinkFor(id)), i18nResolver.getText("applinks.primary", id.get()));
    }

}
