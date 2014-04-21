package com.atlassian.jira.web.component.webfragment;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSectionImpl;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.lang.Pair;
import mock.servlet.MockHttpServletRequest;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.format;
import static org.easymock.classextension.EasyMock.capture;
import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestAdminTabsWebComponent
{
    private final static String SECTION = "atl.jira.proj.config";
    private static final String TEMPLATE = "templates/plugins/webfragments/system-admin-tabs.vm";
    private static final String SERVLET_PATH_TO_SELECTED_TAB = "/the/selected/tab";

    private IMocksControl control;
    private VelocityTemplatingEngine templatingEngine;
    private ApplicationProperties applicationProperties;
    private SimpleLinkManager simpleLinkManager;
    private MockSimpleAuthenticationContext authCtx;
    private MockUser user;

    @Before
    public void setUp() throws Exception
    {
        control = createControl();
        templatingEngine = control.createMock(VelocityTemplatingEngine.class);
        applicationProperties = control.createMock(ApplicationProperties.class);
        simpleLinkManager = control.createMock(SimpleLinkManager.class);
        user = new MockUser("brenden");
        authCtx = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper());
    }

    @Test
    public void testSelectedTabWhenNoMetaTagAndNoServletPath()
    {
        final String value = "Smoke on the Watttttter";
        MockProject project = new MockProject(67L, "BJB");

        SimpleLinkSection oneSection = createSection("one");
        SimpleLink one = createLink("one");

        AdminTabsWebComponent.TabGroup group1 = new AdminTabsWebComponent.TabGroup();
        group1.addTab(new AdminTabsWebComponent.Tab(one, false));

        SimpleLinkSection twoSection = createSection("two");
        SimpleLink two = createLink("two");
        SimpleLink three = createLink("three");

        SimpleLink four = createLink("selectedTab", SERVLET_PATH_TO_SELECTED_TAB);

        AdminTabsWebComponent.TabGroup group2 = new AdminTabsWebComponent.TabGroup();
        group2.addTab(new AdminTabsWebComponent.Tab(two, false));
        group2.addTab(new AdminTabsWebComponent.Tab(three, false));

        final AdminTabsWebComponent.Tab selectedTab = new AdminTabsWebComponent.Tab(four, false);
        group2.addTab(selectedTab);

        final MapBuilder<String, Object> contextBuilder = MapBuilder.newBuilder();
        final Map<String, Object> managerContext = contextBuilder.add("project", project)
                .add("projectKeyEncoded", "enc+BJB").toMap();

        final Map<String, Object> velocityContext = contextBuilder.add("tabGroups",
                Arrays.asList(group1, group2)).add("numberOfTabs", 4).toMap();

        final Capture<JiraHelper> helper = new Capture<JiraHelper>();
        expect(simpleLinkManager.getSectionsForLocation(eq(SECTION), eq(user), capture(helper)))
                .andReturn(Arrays.asList(oneSection, twoSection)).anyTimes();

        expect(simpleLinkManager.getLinksForSection(eq(format("%s/%s", SECTION, oneSection.getId())), eq(user), EasyMock.<JiraHelper>notNull()))
                .andReturn(Arrays.asList(one)).anyTimes();

        expect(simpleLinkManager.getLinksForSection(eq(format("%s/%s", SECTION, twoSection.getId())), eq(user), EasyMock.<JiraHelper>notNull()))
                .andReturn(Arrays.asList(two, three, four)).anyTimes();

        control.replay();

        AdminTabsWebComponent component = new AdminTabsWebComponent(templatingEngine,
                applicationProperties, simpleLinkManager, authCtx)
        {
            @Override
            protected String getHtml(String resourceName, Map<String, Object> params)
            {
                assertEquals(velocityContext, params);
                assertEquals(TEMPLATE, resourceName);
                return value;
            }

            @Override
            String encode(String string)
            {
                return "enc+" + string;
            }

            @Override
            HttpServletRequest getHttpRequest()
            {
                MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
                mockHttpServletRequest.setServletPath("/a/path/that/doesnt/match");

                return mockHttpServletRequest;
            }
        };

        assertEquals(Pair.of(value, 4), component.render(project, "atl.jira.proj.config", null));
        assertEquals(Pair.of(value, 4), component.render(project, "atl.jira.proj.config", ""));

        assertContainsAll(managerContext, helper.getValue().getContextParams());

        control.verify();
    }

    @Test
    public void testSelectedTabWhenNoMetaTag()
    {
        final String value = "Smoke on the Watttttter";
        MockProject project = new MockProject(67L, "BJB");

        SimpleLinkSection oneSection = createSection("one");
        SimpleLink one = createLink("one");

        AdminTabsWebComponent.TabGroup group1 = new AdminTabsWebComponent.TabGroup();
        group1.addTab(new AdminTabsWebComponent.Tab(one, false));

        SimpleLinkSection twoSection = createSection("two");
        SimpleLink two = createLink("two");
        SimpleLink three = createLink("three");

        SimpleLink four = createLink("selectedTab", SERVLET_PATH_TO_SELECTED_TAB);

        AdminTabsWebComponent.TabGroup group2 = new AdminTabsWebComponent.TabGroup();
        group2.addTab(new AdminTabsWebComponent.Tab(two, false));
        group2.addTab(new AdminTabsWebComponent.Tab(three, false));
        group2.addTab(new AdminTabsWebComponent.Tab(four, true));

        final MapBuilder<String, Object> contextBuilder = MapBuilder.newBuilder();
        final Map<String, Object> managerContext = contextBuilder.add("project", project)
                .add("projectKeyEncoded", "enc+BJB").toMap();

        final Map<String, Object> velocityContext = contextBuilder.add("tabGroups",
                Arrays.asList(group1, group2)).add("numberOfTabs", 4).toMap();

        final Capture<JiraHelper> helper = new Capture<JiraHelper>();
        expect(simpleLinkManager.getSectionsForLocation(eq(SECTION), eq(user), capture(helper)))
                .andReturn(Arrays.asList(oneSection, twoSection)).anyTimes();

        expect(simpleLinkManager.getLinksForSection(eq(format("%s/%s", SECTION, oneSection.getId())), eq(user), EasyMock.<JiraHelper>notNull()))
                .andReturn(Arrays.asList(one)).anyTimes();

        expect(simpleLinkManager.getLinksForSection(eq(format("%s/%s", SECTION, twoSection.getId())), eq(user), EasyMock.<JiraHelper>notNull()))
                .andReturn(Arrays.asList(two, three, four)).anyTimes();

        control.replay();

        AdminTabsWebComponent component = new AdminTabsWebComponent(templatingEngine,
                applicationProperties, simpleLinkManager, authCtx)
        {
            @Override
            protected String getHtml(String resourceName, Map<String, Object> params)
            {
                assertEquals(velocityContext, params);
                assertEquals(TEMPLATE, resourceName);
                return value;
            }

            @Override
            String encode(String string)
            {
                return "enc+" + string;
            }

            @Override
            HttpServletRequest getHttpRequest()
            {
                MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
                mockHttpServletRequest.setRequestURL(SERVLET_PATH_TO_SELECTED_TAB);
                return mockHttpServletRequest;
            }
        };

        assertEquals(Pair.of(value, 4), component.render(project, "atl.jira.proj.config", null));
        assertEquals(Pair.of(value, 4), component.render(project, "atl.jira.proj.config", ""));

        assertContainsAll(managerContext, helper.getValue().getContextParams());

        control.verify();
    }

    @Test
    public void testNoTabs()
    {
        final String value = "Smoke on the Watttttter";
        MockProject project = new MockProject(67L, "BJB");

        final MapBuilder<String, Object> contextBuilder = MapBuilder.newBuilder();
        final Map<String, Object> managerContext = contextBuilder.add("project", project)
                .add("projectKeyEncoded", "enc+BJB").toMap();
        final Map<String, Object> velocityContext = contextBuilder.add("tabGroups", Collections.<Object>emptyList()).add("numberOfTabs", 0).toMap();

        final Capture<JiraHelper> helper = new Capture<JiraHelper>();
        expect(simpleLinkManager.getSectionsForLocation(eq(SECTION), eq(user), capture(helper)))
                .andReturn(Collections.<SimpleLinkSection>emptyList());

        control.replay();

        AdminTabsWebComponent component = new AdminTabsWebComponent(templatingEngine,
                applicationProperties, simpleLinkManager, authCtx)
        {
            @Override
            protected String getHtml(String resourceName, Map<String, Object> params)
            {
                assertEquals(velocityContext, params);
                assertEquals(TEMPLATE, resourceName);
                return value;
            }

            @Override
            String encode(String string)
            {
                return "enc+" + string;
            }

            @Override
            HttpServletRequest getHttpRequest()
            {
                return new MockHttpServletRequest();
            }
        };

        assertEquals(Pair.of(value, 0), component.render(project, "atl.jira.proj.config", ""));
        assertContainsAll(managerContext, helper.getValue().getContextParams());

        control.verify();
    }

    @Test
    public void testWithTabsAndGroupings()
    {
        final String value = "Smoke on the Watttttter";
        MockProject project = new MockProject(67L, "BJB");

        SimpleLinkSection oneSection = createSection("one");
        SimpleLink one = createLink("one");

        AdminTabsWebComponent.TabGroup group1 = new AdminTabsWebComponent.TabGroup();
        group1.addTab(new AdminTabsWebComponent.Tab(one, false));

        SimpleLinkSection twoSection = createSection("two");
        SimpleLink two = createLink("two");
        SimpleLink three = createLink("three");

        AdminTabsWebComponent.TabGroup group2 = new AdminTabsWebComponent.TabGroup();
        group2.addTab(new AdminTabsWebComponent.Tab(two, false));
        group2.addTab(new AdminTabsWebComponent.Tab(three, false));

        final MapBuilder<String, Object> contextBuilder = MapBuilder.newBuilder();
        final Map<String, Object> managerContext = contextBuilder.add("project", project)
                .add("projectKeyEncoded", "enc+BJB").toMap();

        final Map<String, Object> velocityContext = contextBuilder.add("tabGroups",
                Arrays.asList(group1, group2)).add("numberOfTabs", 3).toMap();

        final Capture<JiraHelper> helper = new Capture<JiraHelper>();
        expect(simpleLinkManager.getSectionsForLocation(eq(SECTION), eq(user), capture(helper)))
                .andReturn(Arrays.asList(oneSection, twoSection)).anyTimes();

        expect(simpleLinkManager.getLinksForSection(eq(format("%s/%s", SECTION, oneSection.getId())), eq(user), EasyMock.<JiraHelper>notNull()))
                .andReturn(Arrays.asList(one)).anyTimes();

        expect(simpleLinkManager.getLinksForSection(eq(format("%s/%s", SECTION, twoSection.getId())), eq(user), EasyMock.<JiraHelper>notNull()))
                .andReturn(Arrays.asList(two, three)).anyTimes();

        control.replay();

        AdminTabsWebComponent component = new AdminTabsWebComponent(templatingEngine,
                applicationProperties, simpleLinkManager, authCtx)
        {
            @Override
            protected String getHtml(String resourceName, Map<String, Object> params)
            {
                assertEquals(velocityContext, params);
                assertEquals(TEMPLATE, resourceName);
                return value;
            }

            @Override
            String encode(String string)
            {
                return "enc+" + string;
            }

            @Override
            HttpServletRequest getHttpRequest()
            {
                MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
                mockHttpServletRequest.setServletPath("/some/wacky/path");
                return mockHttpServletRequest;
            }
        };

        assertEquals(Pair.of(value, 3), component.render(project, "atl.jira.proj.config", null));
        assertEquals(Pair.of(value, 3), component.render(project, "atl.jira.proj.config", ""));
        assertEquals(Pair.of(value, 3), component.render(project, "atl.jira.proj.config", "dontMatch"));

        assertContainsAll(managerContext, helper.getValue().getContextParams());

        control.verify();
    }

    @Test
    public void testWithTabsAndGroupingsSelected()
    {
        final String value = "Smoke on the Watttttter";
        MockProject project = new MockProject(67L, "BJB");

        SimpleLinkSection twoSection = createSection("two");
        SimpleLink two = createLink("two");
        SimpleLink three = createLink("three");

        AdminTabsWebComponent.TabGroup group2 = new AdminTabsWebComponent.TabGroup();
        group2.addTab(new AdminTabsWebComponent.Tab(two, false));
        group2.addTab(new AdminTabsWebComponent.Tab(three, true));

        final MapBuilder<String, Object> contextBuilder = MapBuilder.newBuilder();
        final Map<String, Object> managerContext = contextBuilder.add("project", project)
                .add("projectKeyEncoded", "enc+BJB").toMap();

        final Map<String, Object> velocityContext = contextBuilder.add("tabGroups",
                Arrays.asList(group2)).add("numberOfTabs", 2).toMap();

        final Capture<JiraHelper> helper = new Capture<JiraHelper>();
        expect(simpleLinkManager.getSectionsForLocation(eq(SECTION), eq(user), capture(helper)))
                .andReturn(Arrays.asList(twoSection)).anyTimes();

        expect(simpleLinkManager.getLinksForSection(eq(format("%s/%s", SECTION, twoSection.getId())), eq(user), EasyMock.<JiraHelper>notNull()))
                .andReturn(Arrays.asList(two, three)).anyTimes();

        expect(simpleLinkManager.getSectionForURL(eq(AdminTabsWebComponent.SYSTEM_ADMIN_TOP_NAVIGATION_BAR), eq("http://mockservletrequest"), eq(user), capture(helper)))
                .andReturn(null).anyTimes();              

        control.replay();

        AdminTabsWebComponent component = new AdminTabsWebComponent(templatingEngine,
                applicationProperties, simpleLinkManager, authCtx)
        {
            @Override
            protected String getHtml(String resourceName, Map<String, Object> params)
            {
                assertEquals(velocityContext, params);
                assertEquals(TEMPLATE, resourceName);
                return value;
            }

            @Override
            String encode(String string)
            {
                return "enc+" + string;
            }

            @Override
            HttpServletRequest getHttpRequest()
            {
                return new MockHttpServletRequest();
            }
        };

        assertEquals(Pair.of(value, 2), component.render(project, "atl.jira.proj.config", "three"));
        assertContainsAll(managerContext, helper.getValue().getContextParams());

        control.verify();
    }

    private SimpleLinkSectionImpl createSection(final String id)
    {
        return new SimpleLinkSectionImpl(id, null, null, null, null, null);
    }

    private SimpleLink createLink(final String id)
    {
        return createLink(id, id);
    }

    private SimpleLink createLink(final String id, final String url)
    {
        return new SimpleLinkImpl(id, null, null, null, null, null, url, id);
    }

    public <K,V> void assertContainsAll(Map<K,V> source, Map<K, V> target)
    {
        for (Map.Entry<K, V> entry : source.entrySet())
        {
            assertEquals(entry.getValue(), target.get(entry.getKey()));
        }
    }
}
