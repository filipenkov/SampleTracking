package com.atlassian.gadgets.directory.internal.rest;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;

import com.atlassian.gadgets.dashboard.PermissionException;
import com.atlassian.gadgets.directory.internal.DirectoryConfigurationPermissionChecker;
import com.atlassian.gadgets.directory.internal.DirectoryUrlBuilder;
import com.atlassian.gadgets.directory.internal.GadgetFeedParsingException;
import com.atlassian.gadgets.directory.internal.GadgetFeedsSpecProvider;
import com.atlassian.gadgets.directory.internal.GadgetFeedsSpecProvider.FeedSpecProvider;
import com.atlassian.gadgets.directory.internal.NonAtomGadgetSpecFeedException;
import com.atlassian.sal.api.message.I18nResolver;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.test.Matchers.badRequest;
import static com.atlassian.gadgets.test.Matchers.created;
import static com.atlassian.gadgets.test.Matchers.unauthorized;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubscribedGadgetFeedsResourceTest
{
    private static final URI BASE_URI = URI.create("http://application/base/");
    private static final URI FEED_URI = BASE_URI.resolve("feed");
    
    @Mock GadgetFeedsSpecProvider provider;
    @Mock DirectoryConfigurationPermissionChecker gadgetUrlChecker;
    @Mock I18nResolver i18n;
    @Mock DirectoryUrlBuilder urlBuilder;

    SubscribedGadgetFeedsResource resource;
    
    @Mock HttpServletRequest request;
    
    @Before
    public void setUp()
    {
        resource = new SubscribedGadgetFeedsResource(provider, gadgetUrlChecker, i18n, urlBuilder);
    }
    
    @Test
    public void assertThatAddingWithInvalidJsonEntityReturnsBadRequest()
    {
        try
        {
            resource.add(request, newJsonReader("not json"));
            fail("expected WebApplicationException with bad request status");
        }
        catch (WebApplicationException e)
        {
            assertThat(e.getResponse(), is(badRequest()));
        }
    }
    
    @Test
    public void assertThatAddingWithMissingUrlReturnsBadRequest()
    {
        try
        {
            resource.add(request, newJsonReader("{'some' : 'valid json' }"));
        }
        catch (WebApplicationException e)
        {
            assertThat(e.getResponse(), is(badRequest()));
        }        
    }
    
    @Test
    public void assertThatAddingWithInvalidUriReturnsBadRequest()
    {
        try
        {
            resource.add(request, withBaseUri("not a valid URI"));
        }
        catch (WebApplicationException e)
        {
            assertThat(e.getResponse(), is(badRequest()));
        }
    }
    
    @Test
    public void assertThatAddingWithUserThatIsNotAllowedToAddReturnsUnauthorized()
    {
        doThrow(new PermissionException()).when(gadgetUrlChecker).checkForPermissionToConfigureDirectory(request);
        assertThat(resource.add(request, withBaseUri("http://base")), is(unauthorized()));
    }
    
    @Test
    public void assertThatAddingWithUriThatIsNotAnAtomFeedReturnsBadRequest()
    {
        when(provider.addFeed(BASE_URI)).thenThrow(new NonAtomGadgetSpecFeedException(FEED_URI));
        assertThat(resource.add(request, withBaseUri(BASE_URI)), is(badRequest()));
    }

    @Test
    public void assertThatAddingWithUriToFeedThatCannotBeParsedReturnsBadRequest()
    {
        when(provider.addFeed(BASE_URI)).thenThrow(new GadgetFeedParsingException("bad feed", FEED_URI, new RuntimeException()));
        assertThat(resource.add(request, withBaseUri(BASE_URI)), is(badRequest()));
    }
    
    @Test
    public void assertThatAddingUriToValidGadgetSpecFeedReturnsCreated()
    {
        FeedSpecProvider applicationProvider = mock(FeedSpecProvider.class);
        when(applicationProvider.getId()).thenReturn("1");
        when(applicationProvider.getUri()).thenReturn(BASE_URI);
        when(provider.addFeed(BASE_URI)).thenReturn(applicationProvider);
        
        when(urlBuilder.buildSubscribedGadgetFeedUrl("1")).thenReturn("http://localhost/new/external/directory");
        when(applicationProvider.entries()).thenReturn(ImmutableSet.<URI>of());
        
        assertThat(resource.add(request, withBaseUri(BASE_URI)), is(created()));
    }

    private InputStreamReader withBaseUri(URI uri)
    {
        return withBaseUri(uri.toASCIIString());
    }
    
    private InputStreamReader withBaseUri(String uri)
    {
        return newJsonReader("{'url' : '" + uri + "' }");
    }
    
    private InputStreamReader newJsonReader(String json)
    {
        return new InputStreamReader(new ByteArrayInputStream(json.getBytes()));
    }
}
