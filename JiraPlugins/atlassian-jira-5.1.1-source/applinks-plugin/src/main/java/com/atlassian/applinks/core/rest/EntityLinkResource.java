package com.atlassian.applinks.core.rest;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.core.rest.model.ReferenceEntityList;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.link.MutatingEntityLinkService;
import com.atlassian.applinks.spi.link.ReciprocalActionException;
import com.atlassian.applinks.core.link.DefaultEntityLinkBuilderFactory;
import com.atlassian.applinks.core.rest.context.ContextInterceptor;
import com.atlassian.applinks.core.rest.model.EntityLinkEntity;
import com.atlassian.applinks.core.rest.model.RestEntityLinkList;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.rest.util.RestUtil;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.atlassian.applinks.core.rest.util.RestUtil.badRequest;
import static com.atlassian.applinks.core.rest.util.RestUtil.checkParam;
import static com.atlassian.applinks.core.rest.util.RestUtil.created;
import static com.atlassian.applinks.core.rest.util.RestUtil.credentialsRequired;
import static com.atlassian.applinks.core.rest.util.RestUtil.notFound;
import static com.atlassian.applinks.core.rest.util.RestUtil.ok;
import static com.atlassian.applinks.core.rest.util.RestUtil.serverError;
import static com.atlassian.applinks.core.rest.util.RestUtil.unauthorized;

/**
 * TODO <link rel="self"> elements for links? need a method that returns a single link for a specified ltype+lkey+rtype+rkey+appName?
 */
@Path("entitylink")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@InterceptorChain({ContextInterceptor.class})
public class EntityLinkResource
{
    private final MutatingEntityLinkService entityLinkService;
    private final ApplicationLinkService applicationLinkService;
    private final InternalTypeAccessor typeAccessor;
    private final I18nResolver i18nResolver;
    private final DefaultEntityLinkBuilderFactory entityLinkFactory;
    private final InternalHostApplication internalHostApplication;
    private static final Logger log = Logger.getLogger(EntityLinkResource.class);

    public EntityLinkResource(final MutatingEntityLinkService entityLinkService,
            final ApplicationLinkService applicationLinkService,
            final InternalTypeAccessor typeAccessor,
            final I18nResolver i18nResolver,
            final DefaultEntityLinkBuilderFactory entityLinkFactory,
            final InternalHostApplication internalHostApplication)
    {
        this.internalHostApplication = internalHostApplication;
        this.entityLinkService = entityLinkService;
        this.applicationLinkService = applicationLinkService;
        this.typeAccessor = typeAccessor;
        this.i18nResolver = i18nResolver;
        this.entityLinkFactory = entityLinkFactory;
    }

    /**
     * Returns all local entities that have links to entities in the supplied
     * application link. This is used by the upgrade wizard when it needs to
     * create a decent description for the reciprocate-entity-links checkbox.
     * The generated wording contains the internationalised names of the local
     * entity types.
     *
     * @param id    the id of a local applink.
     * @return  all {@link EntityReference}s that have entity links to the given applink.
     */
    @GET
    @Path("localEntitiesWithLinksTo/{applinkId}")
    public Response getLocalEntitiesWithLinksToApplication(@PathParam("applinkId") final ApplicationId id)
   {
        final Set<EntityReference> linkedLocalEntities = new HashSet<EntityReference>();
        for (EntityReference ref : internalHostApplication.getLocalEntities()) // correctly returns only the visible ones
        {
            for (EntityLink link : entityLinkService.getEntityLinksForKey(ref.getKey(), ref.getType().getClass()))
            {
                if (id.equals(link.getApplicationLink().getId()))
                {
                    linkedLocalEntities.add(ref);
                }
            }
        }
        return ok(new ReferenceEntityList(linkedLocalEntities));
    }

    /**
     * @param localTypeId the type of the local application entity to look up links for
     * @param localKey            the key of the local application entity to look up links for
     * @param remoteTypeId (optional) the type of remote application entities to look up links for
     * @return a collection of links, optionally of the specified remote application type, associated with the
     *         specified local application entity
     */
    @GET
    @Path("list/{type}/{key}")
    public Response getApplicationEntityLinks(@PathParam("type") final TypeId localTypeId,
                                              @PathParam("key") final String localKey,
                                              @QueryParam("typeId") final TypeId remoteTypeId)
    {
        final List<EntityLinkEntity> entities;
        final EntityType localType = typeAccessor.loadEntityType(localTypeId.get());
        checkPermissionToManageEntityLink(localKey, localType);
        if (remoteTypeId != null)
        {
            final EntityType remoteType = typeAccessor.loadEntityType(remoteTypeId.get());
            entities = toRestApplicationEntities(entityLinkService.getEntityLinksForKey(localKey, localType.getClass(),
                    remoteType.getClass()));
        }
        else
        {
            entities = toRestApplicationEntities(entityLinkService.getEntityLinksForKey(localKey,
                    localType.getClass()));
        }

        return ok(new RestEntityLinkList(entities));
    }

    private static final Comparator<EntityLinkEntity> PRIMARY_FIRST = new Comparator<EntityLinkEntity>()
    {
        public int compare(final EntityLinkEntity o1, final EntityLinkEntity o2)
        {
            if ((o1.isPrimary() && o2.isPrimary()) || (!o1.isPrimary() && !o2.isPrimary()))
            {
                final int result = o1.getTypeId().compareTo(o2.getTypeId());
                return result != 0 ? result : o1.getKey().compareTo(o2.getKey());
            }

            return o1.isPrimary() ? -1 : 1;
        }
    };

    @GET
    @Path("primaryLinks/{type}/{key}")
    public Response getEntityLinks(@PathParam("type") final TypeId localTypeId,
                                   @PathParam("key") final String localKey)
    {
        final EntityType localType = typeAccessor.loadEntityType(localTypeId.get());
        checkPermissionToManageEntityLink(localKey, localType);
        final Map<String, Set<EntityLinkEntity>> linkMap = new HashMap<String, Set<EntityLinkEntity>>();
        for (final EntityLink link : entityLinkService.getEntityLinksForKey(localKey, localType.getClass()))
        {
            Set<EntityLinkEntity> links = linkMap.get(link.getType().getI18nKey());
            if (links == null)
            {
                links = new TreeSet<EntityLinkEntity>(PRIMARY_FIRST);
                linkMap.put(link.getType().getI18nKey(), links);
            }
            links.add(new EntityLinkEntity(link));
        }

        return ok(linkMap);
    }

    @GET
    @Path("primary/{type}/{key}")
    public Response getPrimaryApplicationEntityLink(@PathParam("type") final TypeId typeId,
                                                    @PathParam("key") final String localKey,
                                                    @QueryParam("typeId") final TypeId remoteTypeId)
    {
        final Response response;
        final EntityType localType = typeAccessor.loadEntityType(typeId.get());
        if (localType == null)
        {
            return RestUtil.typeNotInstalled(typeId);
        }
        checkPermissionToManageEntityLink(localKey, localType);
        if (remoteTypeId == null)
        {
            final Iterable<EntityLink> entityLinks =
                    Iterables.filter(entityLinkService.getEntityLinksForKey(localKey, localType.getClass()), new Predicate<EntityLink>()
                    {
                        public boolean apply(final EntityLink input)
                        {
                            return input.isPrimary();
                        }
                    });
            response = ok(toRestApplicationEntities(entityLinks));
        }
        else
        {
            final EntityType remoteType = typeAccessor.loadEntityType(remoteTypeId);
            final EntityLink primary = entityLinkService.getPrimaryEntityLinkForKey(localKey, localType.getClass(), remoteType.getClass());
            if (primary != null)
            {
                response = ok(new EntityLinkEntity(primary));
            }
            else
            {
                response = notFound(String.format("No primary link of type %s for local %s %s found.",
                        remoteType, localType, localKey));
            }
        }
        return response;
    }

    @PUT
    @Path("{type}/{key}")
    public Response createEntityLink(@PathParam("type") final TypeId localTypeId,
                                     @PathParam("key") final String localKey,
                                     @QueryParam("reciprocate") final Boolean reciprocate,
                                     final EntityLinkEntity entity)
    {
        checkParam("entity", entity);

        final ApplicationLink applicationLink;
        try
        {
            applicationLink = applicationLinkService.getApplicationLink(entity.getApplicationId());
        }
        catch (TypeNotInstalledException e)
        {
            return applicationTypeNotInstalled(entity.getApplicationId(), e.getType());
        }
        if (applicationLink == null)
        {
            return notFound("No application found for id " + entity.getApplicationId());
        }

        final EntityType localType = typeAccessor.loadEntityType(localTypeId.get());
        final EntityType remoteType = typeAccessor.loadEntityType(entity.getTypeId().get());
        if (localType == null)
        {
            return RestUtil.typeNotInstalled(localTypeId);
        }
        if (remoteType == null)
        {
            return RestUtil.typeNotInstalled(entity.getTypeId());
        }
        checkPermissionToManageEntityLink(localKey, localType);

        final EntityLink existingEntityLink = entityLinkService.getEntityLink(localKey, localType.getClass(), entity.getKey(), remoteType.getClass(), entity.getApplicationId());
        final EntityLink newLink;
        if (existingEntityLink != null)
        {
            newLink = entityLinkFactory.builder()
                .applicationLink(applicationLink)
                .key(existingEntityLink.getKey())
                .type(remoteType)
                .name(entity.getName())
                .primary(existingEntityLink.isPrimary())
                .build();
        }
        else
        {
            newLink = entityLinkFactory.builder()
                .applicationLink(applicationLink)
                .key(entity.getKey())
                .type(remoteType)
                .name(entity.getName())
                .primary(false)
                .build();
        }

        if (reciprocate != null && reciprocate)
        {
            try
            {
                entityLinkService.addReciprocatedEntityLink(localKey, localType.getClass(), newLink);
            }
            catch (final CredentialsRequiredException e)
            {
                return credentialsRequired(i18nResolver);
            }
            catch (final ReciprocalActionException e)
            {
                return serverError(i18nResolver.getText("applinks.remote.create.failed", e.getMessage()));
            }
        }
        else
        {
            entityLinkService.addEntityLink(localKey, localType.getClass(), newLink);
        }

        return created();  // todo link for an entity link
    }

    @POST
    @Path("primary/{type}/{key}")
    public Response makePrimary(@PathParam("type") final TypeId localTypeId,
                                @PathParam("key") final String localKey,
                                @QueryParam("typeId") final TypeId remoteTypeId,
                                @QueryParam("key") final String remoteKey,
                                @QueryParam("applicationId") final String applicationIdString)
    {
        checkParam("type", remoteTypeId);
        checkParam("key", remoteKey);
        checkParam("applicationId", applicationIdString);

        final Response response;
        final EntityType localType = typeAccessor.loadEntityType(localTypeId.get());
        checkPermissionToManageEntityLink(localKey, localType);
        final EntityType remoteType = typeAccessor.loadEntityType(remoteTypeId.get());
        final ApplicationId applicationId = new ApplicationId(applicationIdString);
        final EntityLink link = entityLinkService.getEntityLink(localKey, localType.getClass(), remoteKey, remoteType.getClass(), applicationId);
        if (link == null)
        {
            response = linkNotFound(localType.getClass(), localKey, remoteType.getClass(), remoteKey, applicationId);
        }
        else
        {
            response = ok(new EntityLinkEntity(entityLinkService.makePrimary(localKey, localType.getClass(), link)));
        }
        return response;
    }

    @DELETE
    @Path("{type}/{key}")
    public Response deleteApplicationEntityLink(@PathParam("type") final TypeId localTypeId,
                                                @PathParam("key") final String localKey,
                                                @QueryParam("typeId") final TypeId remoteTypeId,
                                                @QueryParam("key") final String remoteKey,
                                                @QueryParam("applicationId") final String applicationIdString,
                                                @QueryParam("reciprocate") final Boolean reciprocate)
    {
        checkParam("type", remoteTypeId);
        checkParam("key", localKey);
        checkParam("applicationId", applicationIdString);

        final ApplicationId applicationId = new ApplicationId(applicationIdString);

        final EntityType localType = typeAccessor.loadEntityType(localTypeId.get());
        final EntityType remoteType = typeAccessor.loadEntityType(remoteTypeId.get());
        checkPermissionToManageEntityLink(localKey, localType);
        final EntityLink entity = entityLinkService.getEntityLink(localKey, localType.getClass(), remoteKey, remoteType.getClass(), applicationId);
        if (entity == null)
        {
            return linkNotFound(localType.getClass(), localKey, remoteType.getClass(), remoteKey, applicationId);
        }
        else
        {
            final boolean deleteSucceeded;
            if (reciprocate != null && reciprocate)
            {
                try
                {
                    deleteSucceeded = entityLinkService.deleteReciprocatedEntityLink(localKey, localType.getClass(), entity);
                }
                catch (CredentialsRequiredException e)
                {
                    return credentialsRequired(i18nResolver);
                }
                catch (ReciprocalActionException e)
                {
                    return serverError(i18nResolver.getText("applinks.remote.delete.failed", e.getMessage()));
                }
            }
            else
            {
                deleteSucceeded = entityLinkService.deleteEntityLink(localKey, localType.getClass(), entity);
            }

            if (deleteSucceeded)
            {
                return ok();
            }
            else
            {
                return serverError("Failed to delete link " + entity);
            }
        }
    }

    /**
     * Check if the user has the permission to manage (create, delete, edit) this local entity.
     * 
     * @param localKey
     * @param localType
     */
    protected void checkPermissionToManageEntityLink(final String localKey, final EntityType localType)
    {
        final EntityReference localEntityReference = internalHostApplication.toEntityReference(localKey, localType.getClass());
        if (!internalHostApplication.canManageEntityLinksFor(localEntityReference))
        {
            throw new WebApplicationException(unauthorized("You are not authorized to create a link for entity  with key '" + localKey + "' and type '" + localType.getClass() + "'"));
        }
    }

    private static Response applicationTypeNotInstalled(final ApplicationId id, final String type)
    {
        return badRequest(String.format("Failed to load application %s as the %s type is not installed", id, type));
    }

    private static Response linkNotFound(final Class<? extends EntityType> localType, final String localKey,
                                         final Class<? extends EntityType> remoteType, final String remoteKey,
                                         final ApplicationId applicationId)
    {
        return notFound(String.format("Couldn't find link to %s:%s (%s) from local entity %s:%s",
                remoteType.getName(), remoteKey, applicationId.get(), localType.getName(), localKey));
    }

    private static List<EntityLinkEntity> toRestApplicationEntities(final Iterable<EntityLink> entities)
    {
        final List<EntityLinkEntity> transformed = new ArrayList<EntityLinkEntity>();
        Iterables.addAll(transformed,
                Iterables.transform(entities,
                        new Function<EntityLink, EntityLinkEntity>()
                        {
                            public EntityLinkEntity apply(final EntityLink from)
                            {
                                return new EntityLinkEntity(from);
                            }
                        }));
        return transformed;
    }
}
