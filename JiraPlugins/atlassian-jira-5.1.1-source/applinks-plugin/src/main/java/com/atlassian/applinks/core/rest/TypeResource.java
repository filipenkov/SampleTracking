package com.atlassian.applinks.core.rest;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.core.rest.context.ContextInterceptor;
import com.atlassian.applinks.core.rest.model.EntityTypeEntity;
import com.atlassian.applinks.core.rest.model.EntityTypeListEntity;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.applinks.core.rest.util.RestUtil.badRequest;
import static com.atlassian.applinks.core.rest.util.RestUtil.ok;

@Path("type")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Singleton
@InterceptorChain({ContextInterceptor.class})
public class TypeResource
{
    private final InternalTypeAccessor typeAccessor;

    public TypeResource(final InternalTypeAccessor typeAccessor)
    {
        this.typeAccessor = typeAccessor;
    }

    @GET
    @Path(("entity/{applicationType}"))
    public Response getEntityTypesForApplicationType(@PathParam("applicationType") final TypeId applicationTypeId)
    {
        final ApplicationType applicationType = typeAccessor.loadApplicationType(applicationTypeId);

        if (applicationType == null)
        {
            return badRequest(String.format("ApplicationType with id %s not installed", applicationTypeId));
        }
        return response(typeAccessor.getEntityTypesForApplicationType(applicationTypeId));
    }

    private Response response(final Iterable<? extends EntityType> types)
    {
        return ok(new EntityTypeListEntity(
                Lists.newArrayList(
                        Iterables.transform(types,
                                new Function<EntityType, EntityTypeEntity>()
                                {
                                    public EntityTypeEntity apply(final EntityType from)
                                    {
                                        return new EntityTypeEntity(from);
                                    }
                                }))));
    }

}
