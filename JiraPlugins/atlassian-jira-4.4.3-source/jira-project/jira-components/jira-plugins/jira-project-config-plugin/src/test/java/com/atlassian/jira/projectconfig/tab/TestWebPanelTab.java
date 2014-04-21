package com.atlassian.jira.projectconfig.tab;

import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;
import com.google.common.collect.ImmutableMap;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createControl;
import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link com.atlassian.jira.projectconfig.tab.WebPanelTab}.
 *
 * @since v4.4
 */
public class TestWebPanelTab
{
    private IMocksControl control;
    private WebInterfaceManager webInterfaceManager;
    private VelocityContextFactory factory;
    private ProjectConfigTabRenderContext context;
    private Project project = new MockProject(1L);

    @Before
    public void setUp() throws Exception
    {
        control = createControl();
        webInterfaceManager = control.createMock(WebInterfaceManager.class);
        factory = control.createMock(VelocityContextFactory.class);
        context = control.createMock(ProjectConfigTabRenderContext.class);
    }

    @Test
    public void testGetTabNoWebPanels() throws Exception
    {
        String id = "id";
        String link = "link";

        expect(context.getProject()).andReturn(project).anyTimes();

        Map<String, Object> ctx = ImmutableMap.<String,Object>of("default", true);
        Map<String, Object> expectedCtx = ImmutableMap.<String,Object>builder().putAll(ctx)
                .put(WebPanelTab.CURRENT_PROJECT, project)
                .put(WebPanelTab.CURRENT_TAB_NAME, id)
                .build();

        expect(factory.createDefaultVelocityContext()).andReturn(ctx).anyTimes();

        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(WebPanelTab.HEADER_TAB_LOCATION, expectedCtx))
                .andStubReturn(Collections.<WebPanelModuleDescriptor>emptyList());
        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(createLocationForId(id), expectedCtx))
                .andStubReturn(Collections.<WebPanelModuleDescriptor>emptyList());

        control.replay();

        WebPanelTab panelTab = new WebPanelTab(webInterfaceManager, factory, id, link)
        {
            @Override
            public String getTitle(ProjectConfigTabRenderContext context)
            {
                return "";
            }
        };

        assertEquals(id, panelTab.getId());
        assertEquals(link, panelTab.getLinkId());
        assertEquals("", panelTab.getTab(context));
        assertEquals("", panelTab.getTab(context));

        control.verify();
    }

    @Test
    public void testGetTabOneWebPanels() throws Exception
    {
        String id = "id";
        String link = "link";
        String data1 = "data1";
        String data2 = "data2";

        expect(context.getProject()).andReturn(project).anyTimes();

        Map<String, Object> ctx = ImmutableMap.<String,Object>of("default", true);
        expect(factory.createDefaultVelocityContext()).andReturn(ctx).anyTimes();

        Map<String, Object> expectedCtx = ImmutableMap.<String,Object>builder().putAll(ctx)
                .put(WebPanelTab.CURRENT_PROJECT, project)
                .put(WebPanelTab.CURRENT_TAB_NAME, id)
                .build();

        WebPanel panel = control.createMock(WebPanel.class);
        expect(panel.getHtml(expectedCtx)).andReturn(data1).andReturn(data2);

        WebPanelModuleDescriptor descriptor = control.createMock(WebPanelModuleDescriptor.class);
        expect(descriptor.getModule()).andStubReturn(panel);

        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(WebPanelTab.HEADER_TAB_LOCATION, expectedCtx))
                .andStubReturn(Collections.<WebPanelModuleDescriptor>emptyList());
        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(createLocationForId(id), expectedCtx))
                .andStubReturn(Collections.singletonList(descriptor));

        control.replay();

        WebPanelTab panelTab = new WebPanelTab(webInterfaceManager, factory, id, link)
        {
            @Override
            public String getTitle(ProjectConfigTabRenderContext context)
            {
                return "";
            }
        };

        assertEquals(id, panelTab.getId());
        assertEquals(link, panelTab.getLinkId());
        assertEquals(data1, panelTab.getTab(context));
        assertEquals(data2, panelTab.getTab(context));

        control.verify();

    }

    @Test
    public void testGetTabOneMultiplePanels() throws Exception
    {
        String id = "id";
        String link = "link";
        String data1 = "data1";
        String data2 = "data2";

        expect(context.getProject()).andReturn(project).anyTimes();

        Map<String, Object> ctx = ImmutableMap.<String,Object>of("default", true);
        expect(factory.createDefaultVelocityContext()).andReturn(ctx).anyTimes();

        Map<String, Object> expectedCtx = ImmutableMap.<String,Object>builder().putAll(ctx)
                .put(WebPanelTab.CURRENT_PROJECT, project)
                .put(WebPanelTab.CURRENT_TAB_NAME, id)
                .build();

        WebPanel panel = control.createMock(WebPanel.class);
        expect(panel.getHtml(expectedCtx)).andReturn(data1).andReturn(data2);

        WebPanelModuleDescriptor descriptor = control.createMock(WebPanelModuleDescriptor.class);
        expect(descriptor.getModule()).andStubReturn(panel);

        WebPanelModuleDescriptor descriptor2 = control.createMock(WebPanelModuleDescriptor.class);

        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(WebPanelTab.HEADER_TAB_LOCATION, expectedCtx))
                .andStubReturn(Collections.<WebPanelModuleDescriptor>emptyList());
        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(createLocationForId(id), expectedCtx))
                .andStubReturn(Arrays.asList(descriptor, descriptor2));

        control.replay();

        WebPanelTab panelTab = new WebPanelTab(webInterfaceManager, factory, id, link)
        {
            @Override
            public String getTitle(ProjectConfigTabRenderContext context)
            {
                return "";
            }
        };

        assertEquals(id, panelTab.getId());
        assertEquals(link, panelTab.getLinkId());
        assertEquals(data1, panelTab.getTab(context));
        assertEquals(data2, panelTab.getTab(context));

        control.verify();
    }

    @Test
    public void testGetTabWithHeader() throws Exception
    {
        String id = "id";
        String link = "link";
        String headerData = "headerData:";
        String data1 = "data1";
        String data2 = "data2";

        expect(context.getProject()).andReturn(project).anyTimes();

        Map<String, Object> ctx = ImmutableMap.<String,Object>of("default", true);
        expect(factory.createDefaultVelocityContext()).andReturn(ctx).times(2);

        Map<String, Object> expectedCtx = ImmutableMap.<String,Object>builder().putAll(ctx)
                .put(WebPanelTab.CURRENT_PROJECT, project)
                .put(WebPanelTab.CURRENT_TAB_NAME, id)
                .build();

        WebPanel headerPanel = control.createMock(WebPanel.class);
        expect(headerPanel.getHtml(expectedCtx)).andReturn(headerData).andReturn(headerData);

        WebPanelModuleDescriptor headerDescriptor = control.createMock(WebPanelModuleDescriptor.class);
        expect(headerDescriptor.getModule()).andStubReturn(headerPanel);

        WebPanel panel = control.createMock(WebPanel.class);
        expect(panel.getHtml(expectedCtx)).andReturn(data1).andReturn(data2);

        WebPanelModuleDescriptor descriptor = control.createMock(WebPanelModuleDescriptor.class);
        expect(descriptor.getModule()).andStubReturn(panel);

        WebPanelModuleDescriptor descriptor2 = control.createMock(WebPanelModuleDescriptor.class);

        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(WebPanelTab.HEADER_TAB_LOCATION, expectedCtx))
                .andStubReturn(Arrays.asList(headerDescriptor));
        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(createLocationForId(id), expectedCtx))
                .andStubReturn(Arrays.asList(descriptor, descriptor2));

        control.replay();

        WebPanelTab panelTab = new WebPanelTab(webInterfaceManager, factory, id, link)
        {
            @Override
            public String getTitle(ProjectConfigTabRenderContext context)
            {
                return "";
            }
        };

        assertEquals(id, panelTab.getId());
        assertEquals(link, panelTab.getLinkId());
        assertEquals("headerData:data1", panelTab.getTab(context));
        assertEquals("headerData:data2", panelTab.getTab(context));

        control.verify();
    }

    private String createLocationForId(String id)
    {
        return "tabs.admin.projectconfig." + id;
    }
}
