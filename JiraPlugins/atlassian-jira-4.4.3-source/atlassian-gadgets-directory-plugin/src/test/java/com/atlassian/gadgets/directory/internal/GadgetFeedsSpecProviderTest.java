package com.atlassian.gadgets.directory.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.test.PassThroughTransactionTemplate;
import com.atlassian.gadgets.util.TransactionRunner;

import com.google.common.collect.ImmutableSet;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetFeedsSpecProviderTest
{
    static final URI FEED_URI = URI.create("http://application/base/rest/gadgets/1.0/g/feed");
    static final SubscribedGadgetFeed FEED = new SubscribedGadgetFeed("1", FEED_URI);
    static final HTTPRequest request = new HTTPRequest(FEED_URI);

    @Mock HTTPCache http;
    @Mock SubscribedGadgetFeedStore store;
    @Mock EventPublisher eventPublisher;
    TransactionRunner transactionRunner = new TransactionRunner(new PassThroughTransactionTemplate());
    
    GadgetFeedsSpecProvider provider;
    
    @Mock Payload payload;
    
    @Before
    public void setUp()
    {
        provider = new GadgetFeedsSpecProvider(http, store, transactionRunner, eventPublisher);
    }
    
    @Test
    public void verifyThatAddedFeedIsStored()
    {
        when(http.doCachedRequest(argThat(sameRequestAs(request)))).thenReturn(ok(validGadgetSpecStream(gadget("monkey.xml"))));
        provider.addFeed(FEED_URI);
        
        verify(store).add(argThat(isGadgetFeedWith(FEED_URI)));
    }
    
    
    private Matcher<SubscribedGadgetFeed> isGadgetFeedWith(final URI feedUri)
    {
        return new TypeSafeDiagnosingMatcher<SubscribedGadgetFeed>()
        {
            @Override
            protected boolean matchesSafely(SubscribedGadgetFeed feed, Description mismatchDescription)
            {
                if (!feed.getUri().equals(feedUri))
                {
                    mismatchDescription.appendText("feed uri was ").appendValue(feed.getUri());
                    return false;
                }
                return true;
            }

            public void describeTo(Description description)
            {
                description.appendText("feed with uri ").appendValue(feedUri);
            }
        };
    }

    @Test
    public void assertThatProviderContainsSpecsFromAddedFeed()
    {
        when(store.getAll()).thenReturn(ImmutableSet.of(FEED));
        when(http.doCachedRequest(argThat(sameRequestAs(request)))).thenReturn(ok(validGadgetSpecStream(gadget("monkey.xml"))));
        
        assertTrue(provider.contains(gadget("monkey.xml")));
    }
    
    @Test
    public void assertThatProviderEntriesContainsSpecsFromAddedFeed()
    {
        when(store.getAll()).thenReturn(ImmutableSet.of(FEED));
        when(http.doCachedRequest(argThat(sameRequestAs(request)))).thenReturn(ok(validGadgetSpecStream(gadget("monkey.xml"))));
        
        assertThat(provider.entries(), contains(gadget("monkey.xml")));
    }
    
    @Test(expected=GadgetFeedParsingException.class)
    public void assertThatProviderWillNotAddFeedIfFeedIsInvalid()
    {
        when(http.doCachedRequest(argThat(sameRequestAs(request)))).thenReturn(ok(invalidGadgetSpecStream(gadget("monkey.xml"))));
        provider.addFeed(FEED_URI);
    }
    
    @Test
    public void assertThatGetFeedUrisReturnsUrisInStore()
    {
        when(store.getAll()).thenReturn(ImmutableSet.of(FEED));
        
        assertThat(provider.getFeeds(), contains(FEED));
    }
    
    @Test
    public void verifyThatRemoveDelegatesToStore()
    {
        provider.removeFeed(FEED.getId());
        
        verify(store).remove(FEED.getId());
    }
    
    private URI gadget(String spec)
    {
        return FEED_URI.resolve(spec);
    }
    
    private InputStream validGadgetSpecStream(URI... gadgets)
    {
        String feed = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<feed xmlns=\"http://www.w3.org/2005/Atom\">"
                    + "<title>Gadget Specs for Testing</title>"
                    + "<id>http://application/base/rest/gadgets/1.0/g/feed</id>"
                    + "<updated>2009-10-07T11:00:18Z</updated>";
        for (URI gadget : gadgets)
        {
            feed += "<entry>"
                  + "<title>Gadget spec at " + gadget + "</title>"
                  + "<link rel=\"alternate\" href=\"" + gadget + "\" />"
                  + "<author>"
                  + "<name>Testing</name>"
                  + "</author>"
                  + "<id>" + gadget + "</id>"
                  + "<updated>2009-10-07T11:00:08Z</updated>"
                  + "</entry>";
        }
        feed += "</feed>";
        return new ByteArrayInputStream(feed.getBytes());
    }
    
    // creates an invalid feed by removing a character from the opening "feed" tag, but leaving the closing tag intact 
    private InputStream invalidGadgetSpecStream(URI... gadgets)
    {
        String feed = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<fed xmlns=\"http://www.w3.org/2005/Atom\">"
                    + "<title>Gadget Specs for Testing</title>"
                    + "<id>http://application/base/rest/gadgets/1.0/g/feed</id>"
                    + "<updated>2009-10-07T11:00:18Z</updated>";
        for (URI gadget : gadgets)
        {
            feed += "<entry>"
                  + "<title>Gadget spec at " + gadget + "</title>"
                  + "<link rel=\"alternate\" href=\"" + gadget + "\" />"
                  + "<author>"
                  + "<name>Testing</name>"
                  + "</author>"
                  + "<id>" + gadget + "</id>"
                  + "<updated>2009-10-07T11:00:08Z</updated>"
                  + "</entry>";
        }
        feed += "</feed>";
        return new ByteArrayInputStream(feed.getBytes());
    }

    private HTTPResponse ok(InputStream entity)
    {
        return new HTTPResponse(new InputStreamPayload(entity, new MIMEType("application/xml")), Status.OK, new Headers());
    }

    private Matcher<HTTPRequest> sameRequestAs(final HTTPRequest request)
    {
        return new TypeSafeDiagnosingMatcher<HTTPRequest>()
        {
            @Override
            protected boolean matchesSafely(HTTPRequest item, Description mismatchDescription)
            {
                if (!request.getRequestURI().equals(item.getRequestURI()))
                {
                    mismatchDescription.appendText("request uri was ").appendValue(item.getRequestURI());
                    return false;
                }
                return true;
            }

            public void describeTo(Description description)
            {
                description.appendText("request uri is ").appendValue(request.getRequestURI());
            }
        };
    }
}
