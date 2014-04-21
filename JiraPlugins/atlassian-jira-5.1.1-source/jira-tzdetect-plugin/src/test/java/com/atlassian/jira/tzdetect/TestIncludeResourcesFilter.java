package com.atlassian.jira.tzdetect;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Test case for {@link com.atlassian.jira.tzdetect.IncludeResourcesFilter}.
 *
 * @since 1.5
 */
@RunWith(MockitoJUnitRunner.class)
public class TestIncludeResourcesFilter
{

    @Mock private BannerPreferences bannerPreferences;
    @Mock private WebResourceManager webResourceManager;
    @Mock private FeatureManager featureManager;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @Before
    public void addMockFeatureManagerToComponentManager()
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker().addMock(FeatureManager.class, featureManager));
    }

    private IncludeResourcesFilter createFilter()
    {
        return new IncludeResourcesFilter(webResourceManager, bannerPreferences);
    }

    @Test
    public void shouldNotIncludeIfDisplayPreferenceIsFalse() throws IOException, ServletException
    {
        when(featureManager.isEnabled(CoreFeatures.ON_DEMAND)).thenReturn(false);
        when(bannerPreferences.isDisplayBanner()).thenReturn(false);
        createFilter().doFilter(request, response, filterChain);
        verifyZeroInteractions(webResourceManager);
    }

    @Test
    public void shouldAlwaysIncludeInBtfIdDisplayPreferenceIsTrue() throws IOException, ServletException
    {
        when(featureManager.isEnabled(CoreFeatures.ON_DEMAND)).thenReturn(false);
        when(bannerPreferences.isDisplayBanner()).thenReturn(true);
        when(request.getServletPath()).thenReturn("/browse", "/secure/Administration.jspa", "/secure/Dashboard.jspa",
         "/secure/UserProfile.jspa", "/plugins/servlet/upm");
        for (int i=0; i<5; i++)
        {
            createFilter().doFilter(request, response, filterChain);
        }
        verify(webResourceManager, times(5)).requireResource(anyString());
    }

    @Test
    public void shouldNotIncludeInOnDemandInAdministration() throws IOException, ServletException
    {
        when(featureManager.isEnabled(CoreFeatures.ON_DEMAND)).thenReturn(true);
        when(bannerPreferences.isDisplayBanner()).thenReturn(true);
        when(request.getServletPath()).thenReturn("/secure/Administration.jspa", "/plugins/servlet/upm");
        createFilter().doFilter(request, response, filterChain);
        createFilter().doFilter(request, response, filterChain);
        verifyZeroInteractions(webResourceManager);
    }

    @Test
    public void shouldNotIncludeInOnDemandInUserProfile() throws IOException, ServletException
    {
        when(featureManager.isEnabled(CoreFeatures.ON_DEMAND)).thenReturn(true);
        when(bannerPreferences.isDisplayBanner()).thenReturn(true);
        when(request.getServletPath()).thenReturn("/secure/ViewProfile.jspa");
        createFilter().doFilter(request, response, filterChain);
        verifyZeroInteractions(webResourceManager);
    }

    @Test
    public void shouldIncludeInOnDemandOnBrowseProjectsAndIssues() throws IOException, ServletException
    {
        when(featureManager.isEnabled(CoreFeatures.ON_DEMAND)).thenReturn(true);
        when(bannerPreferences.isDisplayBanner()).thenReturn(true);
        when(request.getServletPath()).thenReturn("/browse");
        createFilter().doFilter(request, response, filterChain);
        verify(webResourceManager, times(1)).requireResource(anyString());
    }

    @Test
    public void shouldIncludeInOnDemandOnDashboard() throws IOException, ServletException
    {
        when(featureManager.isEnabled(CoreFeatures.ON_DEMAND)).thenReturn(true);
        when(bannerPreferences.isDisplayBanner()).thenReturn(true);
        when(request.getServletPath()).thenReturn("/secure/Dashboard.jspa");
        createFilter().doFilter(request, response, filterChain);
        verify(webResourceManager, times(1)).requireResource(anyString());
    }

    @Test
    public void shouldIncludeInOnDemandOnRapidBoard() throws IOException, ServletException
    {
        when(featureManager.isEnabled(CoreFeatures.ON_DEMAND)).thenReturn(true);
        when(bannerPreferences.isDisplayBanner()).thenReturn(true);
        when(request.getServletPath()).thenReturn("/secure/RapidBoard.jspa");
        createFilter().doFilter(request, response, filterChain);
        verify(webResourceManager, times(1)).requireResource(anyString());
    }

    @Test
    public void shouldIncludeInOnDemandOnIssueNavigator() throws IOException, ServletException
    {
        when(featureManager.isEnabled(CoreFeatures.ON_DEMAND)).thenReturn(true);
        when(bannerPreferences.isDisplayBanner()).thenReturn(true);
        when(request.getServletPath()).thenReturn("/secure/IssueNavigator.jspa");
        createFilter().doFilter(request, response, filterChain);
        verify(webResourceManager, times(1)).requireResource(anyString());
    }
}
