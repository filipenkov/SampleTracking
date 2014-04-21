package com.atlassian.jira.gadgets.system;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.dashboard.LegacyGadgetUrlProvider;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.Portlet;
import com.atlassian.jira.portal.PortletConfigurationImpl;
import com.atlassian.jira.portal.PortletConfigurationManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.opensymphony.module.propertyset.PropertySet;
import junit.framework.TestCase;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestLegacyPortletResource extends TestCase
{
    private User admin;
    private ObjectConfiguration mockObjectConfiguration;
    private Portlet mockPortlet;
    private PropertySet mockPropertySet;
    private PortletConfigurationManager mockPortletConfigurationManager;
    private JiraAuthenticationContext mockJiraAuthenticationContext;
    private DashboardPermissionService mockPermissionService;
    private PortalPageService mockPortalPageService;

    @Override
    protected void setUp() throws Exception
    {
        admin = new MockUser("admin");
    }

    public void testGetLegacyPortletUrlNotConfigurable() throws ObjectConfigurationException
    {
        final PortalPage portalPage = PortalPage.id(10020L).name("Default").description("").owner("admin").favouriteCount(0L).layout(Layout.AA).version(0L).build();
        final LegacyPortletResource portletResource = setupMocks(false, true, true, portalPage);
        final Response resp = portletResource.getLegacyPortlet(10000L);
        assertEquals(200, resp.getStatus());
        LegacyPortletResource.LegacyPortlet json = (LegacyPortletResource.LegacyPortlet) resp.getEntity();
        assertEquals("secure/SavePortlet!default.jspa?destination=dashboard&portletConfigId=10000&portalPageId=10020&decorator=none&displayUserSummary=false", json.getEditUrl());
        assertEquals("secure/RunPortlet.jspa?portletKey=viewprojects", json.getUrl());
        assertEquals(false, json.isConfigurable());
        assertEquals(false, json.isRequiresInitialConfiguration());
        verify(this.mockObjectConfiguration, this.mockPortlet, this.mockPropertySet, this.mockPortletConfigurationManager,
                this.mockJiraAuthenticationContext);
    }

    public void testGetLegacyPortletUrlConfigurable() throws ObjectConfigurationException
    {
        final PortalPage portalPage = PortalPage.id(10020L).name("Default").description("").owner("admin").favouriteCount(0L).layout(Layout.AA).version(0L).build();
        final LegacyPortletResource portletResource = setupMocks(true, true, true, portalPage);
        final Response resp = portletResource.getLegacyPortlet(10000L);
        assertEquals(200, resp.getStatus());
        LegacyPortletResource.LegacyPortlet json = (LegacyPortletResource.LegacyPortlet) resp.getEntity();
        assertEquals("secure/SavePortlet!default.jspa?destination=dashboard&portletConfigId=10000&portalPageId=10020&decorator=none&displayUserSummary=false", json.getEditUrl());
        assertEquals("secure/RunPortlet.jspa?portletKey=viewprojects", json.getUrl());
        assertEquals(true, json.isConfigurable());
        assertEquals(true, json.isRequiresInitialConfiguration());
        verify(this.mockObjectConfiguration, this.mockPortlet, this.mockPropertySet, this.mockPortletConfigurationManager,
                this.mockJiraAuthenticationContext, this.mockPermissionService);
    }

    public void testGetLegacyPortletUrlDefaultDashboard() throws ObjectConfigurationException
    {
        final PortalPage portalPage = PortalPage.id(10020L).name("Default").description("").owner("admin").favouriteCount(0L).layout(Layout.AA).version(0L).build();
        final LegacyPortletResource portletResource = setupMocks(true, true, true, portalPage);
        final Response resp = portletResource.getLegacyPortlet(10000L);
        assertEquals(200, resp.getStatus());
        LegacyPortletResource.LegacyPortlet json = (LegacyPortletResource.LegacyPortlet) resp.getEntity();
        assertEquals("secure/SavePortlet!default.jspa?destination=dashboard&portletConfigId=10000&portalPageId=10020&decorator=none&displayUserSummary=false", json.getEditUrl());
        assertEquals("secure/RunPortlet.jspa?portletKey=viewprojects", json.getUrl());
        assertEquals(true, json.isConfigurable());
        assertEquals(true, json.isRequiresInitialConfiguration());
        verify(this.mockObjectConfiguration, this.mockPortlet, this.mockPropertySet, this.mockPortletConfigurationManager,
                this.mockJiraAuthenticationContext, this.mockPermissionService);
    }

    public void testGetLegacyPortletUrlNotFound()
    {
        final PortletConfigurationManager mockPortletConfigurationManager = createMock(PortletConfigurationManager.class);
        expect(mockPortletConfigurationManager.getByPortletId(1000L)).andReturn(null);

        replay(mockPortletConfigurationManager);
        final LegacyPortletResource resource = new LegacyPortletResource(mockPortletConfigurationManager, null, null, null, null, null, null, null, mockPortalPageService);
        final Response resp = resource.getLegacyPortlet(1000L);
        assertEquals(404, resp.getStatus());
        verify(mockPortletConfigurationManager);
    }

    public void testGetLegacyPortletUrlNonLegacyPortlet()
    {
        final PortletConfigurationManager mockPortletConfigurationManager = createMock(PortletConfigurationManager.class);
        expect(mockPortletConfigurationManager.getByPortletId(1000L)).andReturn(new PortletConfigurationImpl(10000L, 10020L, "viewprojects", null, 0, 0, null,
                URI.create("http://www.google.com"), Color.color1, Collections.<String, String>emptyMap()));

        final LegacyGadgetUrlProvider mockLegacyGadgetUrlProvider = createMock(LegacyGadgetUrlProvider.class);
        expect(mockLegacyGadgetUrlProvider.isLegacyGadget(URI.create("http://www.google.com"))).andReturn(false);

        replay(mockPortletConfigurationManager, mockLegacyGadgetUrlProvider);

        final LegacyPortletResource resource = new LegacyPortletResource(mockPortletConfigurationManager, null, null, null, null, null, null, mockLegacyGadgetUrlProvider, mockPortalPageService);
        final Response resp = resource.getLegacyPortlet(1000L);
        assertEquals(404, resp.getStatus());
        verify(mockPortletConfigurationManager, mockLegacyGadgetUrlProvider);
    }

    private LegacyPortletResource setupMocks(boolean isConfigurable, boolean isOwner, boolean isAdmin, PortalPage dashboard)
            throws ObjectConfigurationException
    {
        final ObjectConfiguration mockObjectConfiguration = createMock(ObjectConfiguration.class);
        final Portlet mockPortlet = createMock(Portlet.class);
        if (isOwner)
        {
            expect(mockObjectConfiguration.allFieldsHidden()).andReturn(!isConfigurable);
            expect(mockPortlet.getObjectConfiguration(MapBuilder.<String, User>newBuilder().add("User", admin).toMap())).
                    andReturn(mockObjectConfiguration);
        }

        expect(mockPortlet.getId()).andReturn("viewprojects");

        final PropertySet mockPropertySet = createMock(PropertySet.class);
        expect(mockPropertySet.getKeys()).andReturn(Collections.emptyList()).anyTimes();

        final PortletConfigurationManager mockPortletConfigurationManager = createMock(PortletConfigurationManager.class);
        expect(mockPortletConfigurationManager.getByPortletId(10000L)).
                andReturn(new PortletConfigurationImpl(10000L, 10020L, "viewprojects", mockPortlet, 0, 0, mockPropertySet,
                        null, null, Collections.<String, String>emptyMap()));

        final I18nHelper mockI18nHelper = createNiceMock(I18nHelper.class);

        final JiraAuthenticationContext mockJiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        expect(mockJiraAuthenticationContext.getLoggedInUser()).andReturn(admin);
        expect(mockJiraAuthenticationContext.getI18nHelper()).andReturn(mockI18nHelper);

        final PortalPage mockPortalPage = PortalPage.id(9999L).build();        

        mockPortalPageService = createMock(PortalPageService.class);
        expect(mockPortalPageService.getSystemDefaultPortalPage()).andReturn(mockPortalPage).anyTimes();

        this.mockPermissionService = createMock(DashboardPermissionService.class);
        expect(mockPermissionService.isWritableBy(DashboardId.valueOf(dashboard.getId().toString()), "admin")).andReturn(isAdmin);

        this.mockObjectConfiguration = mockObjectConfiguration;
        this.mockPortlet = mockPortlet;
        this.mockPropertySet = mockPropertySet;
        this.mockPortletConfigurationManager = mockPortletConfigurationManager;
        this.mockJiraAuthenticationContext = mockJiraAuthenticationContext;
        replay(this.mockObjectConfiguration, this.mockPortlet, this.mockPropertySet, this.mockPortletConfigurationManager,
                this.mockJiraAuthenticationContext, this.mockPermissionService, this.mockPortalPageService, mockI18nHelper);

        return new LegacyPortletResource(mockPortletConfigurationManager, mockJiraAuthenticationContext, null, null, null, null, mockPermissionService, null, mockPortalPageService);
    }
}
