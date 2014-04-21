package com.atlassian.gadgets.publisher.internal.rest;

import java.util.Date;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import com.atlassian.gadgets.publisher.internal.GadgetSpecSyndication;

import com.sun.syndication.feed.atom.Feed;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.test.Matchers.entity;
import static com.atlassian.gadgets.test.Matchers.header;
import static com.atlassian.gadgets.test.Matchers.notModified;
import static com.atlassian.gadgets.test.Matchers.ok;
import static javax.ws.rs.core.HttpHeaders.ETAG;
import static javax.ws.rs.core.HttpHeaders.LAST_MODIFIED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetSpecSyndicationResourceTest
{
    private static final long FEED_LAST_UPDATED = 1234567890L * 1000;

    @Mock GadgetSpecSyndication syndication;

    @Mock Request request;
    @Mock Feed feed;
    
    GadgetSpecSyndicationResource resource;
    
    @Before
    public void setUp()
    {
        when(syndication.getFeed()).thenReturn(feed);
        when(feed.getUpdated()).thenReturn(new Date(FEED_LAST_UPDATED));
        
        resource = new GadgetSpecSyndicationResource(syndication);
    }
    
    @Test
    public void assertThatSimpleGetReturnsOkResponse()
    {
        assertThat(resource.get(request), is(ok()));
    }
    
    @Test
    public void assertThatGetReturnsResponseWithFeed()
    {
        assertThat(resource.get(request), entity(is(equalTo(feed))));
    }
    
    @Test
    public void assertThatGetReturnsResponseWithLastModifiedDateSetToWhenFeedWasLastUpdated()
    {
        assertThat(resource.get(request), header(LAST_MODIFIED).is(equalTo(feed.getUpdated())));
    }
    
    @Test
    public void assertThatGetReturnsResponseWithEtagSetToFeedLastUpdateInMillis()
    {
        assertThat(resource.get(request), header(ETAG).is(equalTo(feedEntityTag())));
    }
    
    @Test
    public void assertThatConditionalGetWithLastModifiedSinceReturnsNotModifiedIfTheFeedHasNotBeenChanged()
    {
        when(request.evaluatePreconditions(eq(feed.getUpdated()), isA(EntityTag.class))).thenReturn(Response.notModified());
        assertThat(resource.get(request), is(notModified()));
    }
    
    @Test
    public void assertThatConditionalGetWithMatchingETagReturnsNotModifiedSince()
    {
        when(request.evaluatePreconditions(isA(Date.class), eq(feedEntityTag()))).thenReturn(Response.notModified());
        assertThat(resource.get(request), is(notModified()));
    }
    
    private EntityTag feedEntityTag()
    {
        return new EntityTag(Long.toString(FEED_LAST_UPDATED));
    }
}
