package com.atlassian.applinks.core.rest.client;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.rest.EntityResource;
import com.atlassian.applinks.core.rest.model.ReferenceEntityList;
import com.atlassian.applinks.core.rest.util.RestUtil;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.google.common.collect.Iterables;
import com.atlassian.applinks.api.auth.Anonymous;

import java.util.ArrayList;
import java.util.List;

public class EntityRetriever
{
    private final InternalTypeAccessor typeAccessor;

    public EntityRetriever(final InternalTypeAccessor typeAccessor)
    {
        this.typeAccessor = typeAccessor;
    }

    public Iterable<EntityReference> getEntities(final ApplicationLink link)
            throws ResponseException, CredentialsRequiredException
    {
        return getEntities(link.createAuthenticatedRequestFactory());
    }

    private Iterable<EntityReference> getEntities(final ApplicationLinkRequestFactory requestFactory)
            throws CredentialsRequiredException, ResponseException
    {
        final Request req = requestFactory.createRequest(Request.MethodType.GET,
                RestUtil.REST_APPLINKS_URL + EntityResource.CONTEXT);

        final List<EntityReference> entities = new ArrayList<EntityReference>();
        req.execute(new ResponseHandler<Response>()
        {
            public void handle(final Response response) throws ResponseException
            {
                if (response.getStatusCode() == 200)
                {
                    Iterables.addAll(entities, response.getEntity(ReferenceEntityList.class).getEntities(typeAccessor));
                }
                else
                {
                    throw new ResponseException(String.format("Failed to retrieve entity list, received %s response: %s",
                            response.getStatusCode(), response.getStatusText()));
                }
            }
        });
        return entities;
    }

    public Iterable<EntityReference> getEntitiesForAnonymousAccess(final ApplicationLink link)
            throws ResponseException
    {
        try
        {
            return getEntities(link.createAuthenticatedRequestFactory(Anonymous.class));
        }
        catch (CredentialsRequiredException e)
        {
            throw new RuntimeException(CredentialsRequiredException.class.getName() + " should never be thrown on anonymous access.", e);
        }
    }
}
