package com.atlassian.gadgets.directory.internal.rest;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;

import com.atlassian.gadgets.dashboard.PermissionException;
import com.atlassian.gadgets.directory.internal.DirectoryConfigurationPermissionChecker;
import com.atlassian.gadgets.directory.internal.DirectoryUrlBuilder;
import com.atlassian.gadgets.directory.internal.GadgetFeedsSpecProvider;
import com.atlassian.gadgets.directory.internal.SubscribedGadgetFeed;
import com.atlassian.sal.api.message.I18nResolver;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.test.Matchers.notFound;
import static com.atlassian.gadgets.test.Matchers.unauthorized;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubscribedGadgetFeedResourceTest
{
    private static final SubscribedGadgetFeed FEED = new SubscribedGadgetFeed("1", URI.create("http://some/feed"));

    @Mock GadgetFeedsSpecProvider feedsProvider;
    @Mock I18nResolver i18n;
    @Mock DirectoryConfigurationPermissionChecker gadgetUrlChecker;
    @Mock DirectoryUrlBuilder urlBuilder;

    SubscribedGadgetFeedResource resource;
    
    @Mock HttpServletRequest request;
    
    @Before
    public void setUp()
    {
        resource = new SubscribedGadgetFeedResource(feedsProvider, i18n, gadgetUrlChecker, urlBuilder);
    }
    
    @Test
    public void assertThatTryingToDeleteWithoutPermissionReturnsUnauthorized()
    {
        doThrow(new PermissionException()).when(gadgetUrlChecker).checkForPermissionToConfigureDirectory(request);
        
        try
        {
            resource.remove("http://some/feed", request);
            fail("expected WebApplicationException to be thrown");
        }
        catch (WebApplicationException e)
        {
            assertThat(e.getResponse(), is(unauthorized()));
        }
    }
    
    @Test
    public void assertThatTryingToDeleteFeedThatIsNotSubscribedToReturnsNotFound()
    {
        when(feedsProvider.getFeeds()).thenReturn(ImmutableList.of(FEED));
        try
        {
            resource.remove("1001", request);
            fail("expected WebApplicationException to be thrown");
        }
        catch (WebApplicationException e)
        {
            assertThat(e.getResponse(), is(notFound()));
        }
    }
    
    @Test
    public void verifyThatProviderRemoveIsCalledWhenTryingToDeleteFeedThatIsSubscribedTo()
    {
        when(feedsProvider.containsFeed("1")).thenReturn(true);
        resource.remove("1", request);
        
        verify(feedsProvider).removeFeed("1");
    }
}
