package com.atlassian.gadgets.publisher.internal.rest;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.atlassian.gadgets.publisher.internal.GadgetSpecSyndication;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import com.sun.syndication.feed.atom.Feed;

@Path("/g/feed")
public class GadgetSpecSyndicationResource
{
    private final GadgetSpecSyndication syndication;
    
    public GadgetSpecSyndicationResource(GadgetSpecSyndication syndication)
    {
        this.syndication = syndication;
    }
    
    @GET
    @Produces("application/atom+xml")
    @AnonymousAllowed
    public Response get(@Context Request request)
    {
        Feed feed = syndication.getFeed();
        ResponseBuilder builder = request.evaluatePreconditions(feed.getUpdated(), computeETag(feed));
        if (builder != null)
        {
            return builder.build();
        }
        return Response.ok(feed).lastModified(feed.getUpdated()).tag(computeETag(feed)).build();
    }
    
    private EntityTag computeETag(Feed feed)
    {
        return computeETag(feed.getUpdated());
    }
    
    private EntityTag computeETag(Date date)
    {
        return new EntityTag(Long.toString(date.getTime()));
    }
}
