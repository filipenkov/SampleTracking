package com.atlassian.gadgets.dashboard.internal.impl;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.internal.DashboardUrlBuilder;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DashboardUrlBuilderImplTest
{
    private static final String BASE_URL = "http://example.com/dashboards";
    private static final String BASE_DASHBOARD_SPEC_PATH = "/rest/dashboards/1.0/";
    private static final String DASHBOARD_URL = BASE_URL + BASE_DASHBOARD_SPEC_PATH + "1";
    private static final String DASHBOARD_LAYOUT_URL = DASHBOARD_URL + "/layout";
    private static final String DASHBOARD_DIRECTORY_URL = BASE_URL + BASE_DASHBOARD_SPEC_PATH + "/directory/1";
    private static final String DASHBOARD_DIRECTORY_RESOURCE_URL = BASE_URL + "/rest/config/1.0/directory";
    private static final String GADGET_URL = DASHBOARD_URL + "/gadget/1001";
    private static final String GADGET_COLOR_URL = DASHBOARD_URL + "/gadget/1001/color";
    private static final String GADGET_PREFS_URL = DASHBOARD_URL + "/gadget/1001/prefs";

    private static final DashboardId DASHBOARD_ID = DashboardId.valueOf("1");
    private static final GadgetId GADGET_ID = GadgetId.valueOf("1001");

    @Mock ApplicationProperties applicationProperties;
    @Mock WebResourceManager webResourceManager;

    DashboardUrlBuilder urlBuilder;

    @Before
    public void setUp()
    {
        when(applicationProperties.getBaseUrl()).thenReturn(BASE_URL);
        urlBuilder = new DashboardUrlBuilderImpl(applicationProperties, webResourceManager);
    }

    @Test
    public void testBuildDashboardUrl()
    {
        assertEquals(DASHBOARD_URL, urlBuilder.buildDashboardUrl(DASHBOARD_ID));
    }

    @Test
    public void testBuildDashboardLayoutUrl()
    {
        assertEquals(DASHBOARD_LAYOUT_URL, urlBuilder.buildDashboardLayoutUrl(DASHBOARD_ID));
    }

    @Test
    public void testBuildDashboardDirectoryUrl()
    {
        assertEquals(DASHBOARD_DIRECTORY_URL, urlBuilder.buildDashboardDirectoryUrl(DASHBOARD_ID));
    }

    @Test
    public void testBuildDashboardDirectoryResourceUrl()
    {
        assertEquals(DASHBOARD_DIRECTORY_RESOURCE_URL, urlBuilder.buildDashboardDirectoryResourceUrl());
    }

    @Test
    public void testBuildGadgetUrl()
    {
        assertEquals(GADGET_URL, urlBuilder.buildGadgetUrl(DASHBOARD_ID, GADGET_ID));
    }

    @Test
    public void testBuildGadgetColorUrl()
    {
        assertEquals(GADGET_COLOR_URL, urlBuilder.buildGadgetColorUrl(DASHBOARD_ID, GADGET_ID));
    }

    @Test
    public void testBuildGadgetUserPrefsUrl()
    {
        assertEquals(GADGET_PREFS_URL, urlBuilder.buildGadgetUserPrefsUrl(DASHBOARD_ID, GADGET_ID));
    }

    @Test
    public void testBuildErrorGadgetUrlCallsWebResourceManagerStaticPluginResource()
    {
        // webResourceManager would return null for this case (resource was not defined)
        assertEquals("nullerrorGadget.html", urlBuilder.buildErrorGadgetUrl());

        // verify that getStaticPluginResource with parameters PLUGIN_MODULE_KEY, RESOURCE_NAME
        // was called by the urlBuilder.
        verify(webResourceManager, times(1)).getStaticPluginResource("com.atlassian.gadgets.dashboard:dashboard", "files/", UrlMode.AUTO);
    }
}
