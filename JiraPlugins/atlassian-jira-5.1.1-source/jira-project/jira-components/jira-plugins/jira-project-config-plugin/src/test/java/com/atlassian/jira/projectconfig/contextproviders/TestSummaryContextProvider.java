package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.renderer.ProjectDescriptionRenderer;
import com.atlassian.jira.projectconfig.beans.SimplePanel;
import com.atlassian.jira.projectconfig.beans.SimpleProject;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.component.ModuleWebComponent;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebLabel;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.plugin.web.model.WebParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link SummaryContextProvider}
 *
 * @since v4.4
 */
@SuppressWarnings ("unchecked")
public class TestSummaryContextProvider
{
    private IMocksControl mockControl;
    private ContextProviderUtils contextProviderUtils;
    private WebInterfaceManager webInterfaceManager;
    private ModuleWebComponent webComponent;
    private Project project;
    private Map<String, Object> testContext;
    private Map<String, Object> defaultContext;
    private Map<String, Object> webItemContext;
    private JiraAuthenticationContext authenticationContext;
    private User user;
    private JiraHelper helper;
    private ProjectDescriptionRenderer projectDescriptionRenderer;

    @Before
    public void setUp()
    {
        mockControl = EasyMock.createControl();

        webInterfaceManager = mockControl.createMock(WebInterfaceManager.class);
        contextProviderUtils = mockControl.createMock(ContextProviderUtils.class);
        webComponent = mockControl.createMock((ModuleWebComponent.class));
        user = new MockUser("admin", "administrator", "admin@admin.com");
        authenticationContext = new MockAuthenticationContext(user, null, null);
        projectDescriptionRenderer = mockControl.createMock(ProjectDescriptionRenderer.class);

        testContext = MapBuilder.build(
                "a", "aValue",
                "b", "bValue",
                "c", new Object()
        );

        project = projectFixture();
        helper = new JiraHelper(null, project);

        webItemContext = MapBuilder.build(
                "project", project,
                "user", user,
                "request", null,
                "helper", helper
        );

        defaultContext = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .add(ContextProviderUtils.CONTEXT_PROJECT_KEY, project)
                .add(ContextProviderUtils.CONTEXT_I18N_KEY, new NoopI18nHelper())
                .toMap();

    }

    @After
    public void tearDown()
    {
        mockControl = null;
        webInterfaceManager = null;
        contextProviderUtils = null;
        testContext = null;
        project = null;
        defaultContext = null;
    }


    @Test
    public void testGetContextMapWithDisplayablePanels() throws Exception
    {
        final WebPanelModuleDescriptor webPanelModuleDescriptorA = panelFixture("FIXTURE_A");
        final WebPanelModuleDescriptor webPanelModuleDescriptorB = panelFixture("FIXTURE_B");

        final List<WebPanelModuleDescriptor> leftPanels = Lists.newArrayList(webPanelModuleDescriptorA);
        final List<WebPanelModuleDescriptor> rightPanels = Lists.newArrayList(webPanelModuleDescriptorB);

        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(eq(SummaryContextProvider.SUMMARY_LEFT_PANELS_LOCATION),
                eq(MapBuilder.<String, Object>build("project", project))))
                .andReturn(leftPanels);
        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(eq(SummaryContextProvider.SUMMARY_RIGHT_PANELS_LOCATION),
                eq(MapBuilder.<String, Object>build("project", project))))
                .andReturn(rightPanels);
        expect(webComponent.renderModule(EasyMock.<User>anyObject(), EasyMock.<HttpServletRequest>anyObject(), eq(webPanelModuleDescriptorA), EasyMock.<Map<String, Object>>anyObject())).andReturn("PANEL_HTML_FIXTURE_A");
        expect(webComponent.renderModule(EasyMock.<User>anyObject(), EasyMock.<HttpServletRequest>anyObject(), eq(webPanelModuleDescriptorB), EasyMock.<Map<String, Object>>anyObject())).andReturn("PANEL_HTML_FIXTURE_B");

        expect(contextProviderUtils.getDefaultContext()).andReturn(defaultContext);

        mockControl.replay();

        final SummaryContextProvider summaryContextProvider = getSummaryContextProviderUnderTest(webInterfaceManager, contextProviderUtils, authenticationContext, webComponent);
        final Map<String, Object> summaryContextMap = summaryContextProvider.getContextMap(testContext);

        final List<SimplePanel> expectedLeftPanels = Lists.newArrayList(expectedPanelFixture("FIXTURE_A"));
        final List<SimplePanel> expectedRightPanels = Lists.newArrayList(expectedPanelFixture("FIXTURE_B"));
        final SimpleProject expectedProject = expectedSimpleProjectFixture();

        final Map<String, Object> expectedContext = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .addAll(defaultContext)
                .add(SummaryContextProvider.CONTEXT_SIMPLE_PROJECT_KEY, expectedProject)
                .add(SummaryContextProvider.CONTEXT_LEFT_COLUMN_KEY, expectedLeftPanels)
                .add(SummaryContextProvider.CONTEXT_RIGHT_COLUMN_KEY, expectedRightPanels)
                .add(SummaryContextProvider.CONTEXT_PROJECT_DESCRIPTION_RENDERER_KEY, projectDescriptionRenderer)
                .toMap();

        assertEquals(summaryContextMap, expectedContext);

        mockControl.verify();
    }

    @Test
    public void testGetContextMapWithNoDisplayablePanels() throws Exception
    {
        final List<WebPanelModuleDescriptor> leftPanels = Lists.newArrayList();
        final List<WebPanelModuleDescriptor> rightPanels = Lists.newArrayList();

        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(eq(SummaryContextProvider.SUMMARY_LEFT_PANELS_LOCATION),
                eq(MapBuilder.<String, Object>build("project", project))))
                .andReturn(leftPanels);
        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(eq(SummaryContextProvider.SUMMARY_RIGHT_PANELS_LOCATION),
                eq(MapBuilder.<String, Object>build("project", project))))
                .andReturn(rightPanels);
        expect(contextProviderUtils.getDefaultContext()).andReturn(defaultContext);

        mockControl.replay();


        final SummaryContextProvider summaryContextProvider = getSummaryContextProviderUnderTest(webInterfaceManager, contextProviderUtils, authenticationContext, webComponent);
        final Map<String, Object> summaryContextMap = summaryContextProvider.getContextMap(testContext);

        final List<SimplePanel> expectedLeftPanels = Collections.emptyList();
        final List<SimplePanel> expectedRightPanels = Collections.emptyList();
        final SimpleProject expectedProject = expectedSimpleProjectFixture();

        final Map<String, Object> expectedContext = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .addAll(defaultContext)
                .add(SummaryContextProvider.CONTEXT_SIMPLE_PROJECT_KEY, expectedProject)
                .add(SummaryContextProvider.CONTEXT_LEFT_COLUMN_KEY, expectedLeftPanels)
                .add(SummaryContextProvider.CONTEXT_RIGHT_COLUMN_KEY, expectedRightPanels)
                .add(SummaryContextProvider.CONTEXT_PROJECT_DESCRIPTION_RENDERER_KEY, projectDescriptionRenderer)
                .toMap();

        assertEquals(summaryContextMap, expectedContext);

        mockControl.verify();

    }

    @Test
    public void testGetContextMapWithNoWebLabel() throws Exception
    {
        final WebPanelModuleDescriptor webPanelModuleDescriptorC = panelFixtureWithNoWebDisplayableName("FIXTURE_C");
        final WebPanelModuleDescriptor webPanelModuleDescriptorD = panelFixtureWithNoWebDisplayableName("FIXTURE_D");

        final List<WebPanelModuleDescriptor> leftPanels = Lists.newArrayList(webPanelModuleDescriptorC);
        final List<WebPanelModuleDescriptor> rightPanels = Lists.newArrayList(webPanelModuleDescriptorD);


        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(eq(SummaryContextProvider.SUMMARY_LEFT_PANELS_LOCATION),
                eq(MapBuilder.<String, Object>build("project", project))))
                .andReturn(leftPanels);
        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(eq(SummaryContextProvider.SUMMARY_RIGHT_PANELS_LOCATION),
                eq(MapBuilder.<String, Object>build("project", project))))
                .andReturn(rightPanels);
        expect(contextProviderUtils.getDefaultContext()).andReturn(defaultContext);
        expect(webComponent.renderModule(EasyMock.<User>anyObject(), EasyMock.<HttpServletRequest>anyObject(), org.easymock.EasyMock.eq(webPanelModuleDescriptorC), EasyMock.<Map<String, Object>>anyObject())).andReturn("PANEL_HTML_FIXTURE_C");
        expect(webComponent.renderModule(EasyMock.<User>anyObject(), EasyMock.<HttpServletRequest>anyObject(), org.easymock.EasyMock.eq(webPanelModuleDescriptorD), EasyMock.<Map<String, Object>>anyObject())).andReturn("PANEL_HTML_FIXTURE_D");

        mockControl.replay();

        final SummaryContextProvider summaryContextProvider = getSummaryContextProviderUnderTest(webInterfaceManager, contextProviderUtils, authenticationContext, webComponent);
        final Map<String, Object> summaryContextMap = summaryContextProvider.getContextMap(testContext);

        final List<SimplePanel> expectedLeftPanels = Lists.newArrayList(expectedPanelFixtureWithNoWebDisplayableName("FIXTURE_C"));
        final List<SimplePanel> expectedRightPanels = Lists.newArrayList(expectedPanelFixtureWithNoWebDisplayableName("FIXTURE_D"));
        final SimpleProject expectedProject = expectedSimpleProjectFixture();

        final Map<String, Object> expectedContext = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .addAll(defaultContext)
                .add(SummaryContextProvider.CONTEXT_SIMPLE_PROJECT_KEY, expectedProject)
                .add(SummaryContextProvider.CONTEXT_LEFT_COLUMN_KEY, expectedLeftPanels)
                .add(SummaryContextProvider.CONTEXT_RIGHT_COLUMN_KEY, expectedRightPanels)
                .add(SummaryContextProvider.CONTEXT_PROJECT_DESCRIPTION_RENDERER_KEY, projectDescriptionRenderer)
                .toMap();

        assertEquals(summaryContextMap, expectedContext);

        mockControl.verify();
    }

    @Test
    public void testGetContextMapWithNoPanelLink() throws Exception
    {
        final WebPanelModuleDescriptor webPanelModuleDescriptorE = panelFixtureWithNoWebPanelLink("FIXTURE_E");
        final WebPanelModuleDescriptor webPanelModuleDescriptorF = panelFixtureWithNoWebPanelLink("FIXTURE_F");

        final List<WebPanelModuleDescriptor> leftPanels = Lists.newArrayList(webPanelModuleDescriptorE);
        final List<WebPanelModuleDescriptor> rightPanels = Lists.newArrayList(webPanelModuleDescriptorF);


        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(eq(SummaryContextProvider.SUMMARY_LEFT_PANELS_LOCATION),
                eq(MapBuilder.<String, Object>build("project", project))))
                .andReturn(leftPanels);
        expect(webInterfaceManager.getDisplayableWebPanelDescriptors(eq(SummaryContextProvider.SUMMARY_RIGHT_PANELS_LOCATION),
                eq(MapBuilder.<String, Object>build("project", project))))
                .andReturn(rightPanels);

        expect(contextProviderUtils.getDefaultContext()).andReturn(defaultContext);
        expect(webComponent.renderModule(EasyMock.<User>anyObject(), EasyMock.<HttpServletRequest>anyObject(), org.easymock.EasyMock.eq(webPanelModuleDescriptorE), EasyMock.<Map<String, Object>>anyObject())).andReturn("PANEL_HTML_FIXTURE_E");
        expect(webComponent.renderModule(EasyMock.<User>anyObject(), EasyMock.<HttpServletRequest>anyObject(), org.easymock.EasyMock.eq(webPanelModuleDescriptorF), EasyMock.<Map<String, Object>>anyObject())).andReturn("PANEL_HTML_FIXTURE_F");

        mockControl.replay();

        final SummaryContextProvider summaryContextProvider = getSummaryContextProviderUnderTest(webInterfaceManager, contextProviderUtils, authenticationContext, webComponent);
        final Map<String, Object> summaryContextMap = summaryContextProvider.getContextMap(testContext);

        final List<SimplePanel> expectedLeftPanels = Lists.newArrayList(expectedPanelFixtureWithNoWebPanelLink("FIXTURE_E"));
        final List<SimplePanel> expectedRightPanels = Lists.newArrayList(expectedPanelFixtureWithNoWebPanelLink("FIXTURE_F"));
        final SimpleProject expectedProject = expectedSimpleProjectFixture();

        final Map<String, Object> expectedContext = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .addAll(defaultContext)
                .add(SummaryContextProvider.CONTEXT_SIMPLE_PROJECT_KEY, expectedProject)
                .add(SummaryContextProvider.CONTEXT_LEFT_COLUMN_KEY, expectedLeftPanels)
                .add(SummaryContextProvider.CONTEXT_RIGHT_COLUMN_KEY, expectedRightPanels)
                .add(SummaryContextProvider.CONTEXT_PROJECT_DESCRIPTION_RENDERER_KEY, projectDescriptionRenderer)
                .toMap();

        assertEquals(summaryContextMap, expectedContext);

        mockControl.verify();
    }

    private SummaryContextProvider getSummaryContextProviderUnderTest(WebInterfaceManager webInterfaceManager,
            ContextProviderUtils contextProviderUtils, JiraAuthenticationContext authenticationContext, ModuleWebComponent webComponent)
    {
        return new SummaryContextProvider(webInterfaceManager, contextProviderUtils, webComponent, authenticationContext, projectDescriptionRenderer);
    }

    private WebPanelModuleDescriptor panelFixtureWithNoWebPanelLink(final String id)
    {
        return MockWebPanelModuleDescriptorBuilder.builder(mockControl, defaultContext)
                .setKey("KEY_" + id)
                .setWebPanelHtml("PANEL_HTML_" + id)
                .setWebLabelDisplayableName("WEBLABEL_" + id)
                .build();
    }

    private SimplePanel expectedPanelFixtureWithNoWebPanelLink(final String id)
    {
        return new SimplePanel("WEBLABEL_" + id, "KEY_" + id, "PANEL_HTML_" + id);
    }

    private WebPanelModuleDescriptor panelFixtureWithNoWebDisplayableName(final String id)
    {
        return MockWebPanelModuleDescriptorBuilder.builder(mockControl, defaultContext)
                .setKey("KEY_" + id)
                .setCompleteKey("COMPLETE_KEY_" + id)
                .setWebPanelHtml("PANEL_HTML_" + id)
                .setNoWebLabelDisplayableName()
                .build();
    }

    private SimplePanel expectedPanelFixtureWithNoWebDisplayableName(final String id)
    {
        return new SimplePanel("COMPLETE_KEY_" + id, "KEY_" + id, "PANEL_HTML_" + id);
    }

    private WebPanelModuleDescriptor panelFixture(final String id)
    {
        return MockWebPanelModuleDescriptorBuilder.builder(mockControl, defaultContext)
                .setKey("KEY_" + id)
                .setWebPanelHtml("PANEL_HTML_" + id)
                .setWebLabelDisplayableName("WEBLABEL_" + id)
                .build();
    }

    private SimplePanel expectedPanelFixture(final String id)
    {
        return new SimplePanel("WEBLABEL_" + id, "KEY_" + id, "PANEL_HTML_" + id);
    }

    private Project projectFixture()
    {
        final MockProject mockProject = new MockProject(1L, "PROJECT_KEY", "PROJECT_NAME");
        mockProject.setUrl("PROJECT_URL");
        mockProject.setDescription("PROJECT_DESCRIPTION");

        return mockProject;
    }

    private SimpleProject expectedSimpleProjectFixture()
    {
        return new SimpleProject(projectFixture());
    }

    /**
     * Builds a mocked out web panel module descriptor, making it easier to specify specific mocked objects it ought to
     * produce.
     */
    private static class MockWebPanelModuleDescriptorBuilder
    {
        private final Map<String, Object> testContext;
        private final WebPanelModuleDescriptor webPanelModuleDescriptor;
        private final WebLabel webLabel;
        private final WebParam webParams;
        private final WebPanel webPanel;
        private String key;

        public MockWebPanelModuleDescriptorBuilder(final IMocksControl mockControl, final Map<String, Object> testContext)
        {
            this.testContext = testContext;
            this.webPanelModuleDescriptor = mockControl.createMock(WebPanelModuleDescriptor.class);
            this.webLabel = mockControl.createMock(WebLabel.class);
            this.webParams = mockControl.createMock(WebParam.class);
            this.webPanel = mockControl.createMock(WebPanel.class);
        }

        public MockWebPanelModuleDescriptorBuilder setKey(final String key)
        {
            expect(webPanelModuleDescriptor.getKey()).andReturn(key).anyTimes();
            this.key = key;
            return this;
        }

        public MockWebPanelModuleDescriptorBuilder setCompleteKey(final String completeKey)
        {
            expect(webPanelModuleDescriptor.getCompleteKey()).andReturn(completeKey).anyTimes();
            return this;
        }

        public MockWebPanelModuleDescriptorBuilder setWebLabelDisplayableName(final String displayableName)
        {
            expect(webPanelModuleDescriptor.getWebLabel()).andReturn(webLabel).anyTimes();
            expect(webLabel.getDisplayableLabel(EasyMock.<HttpServletRequest>isNull(), eq(testContext))).andReturn(displayableName).anyTimes();

            return this;
        }

        public MockWebPanelModuleDescriptorBuilder setNoWebLabelDisplayableName()
        {
            expect(webPanelModuleDescriptor.getWebLabel()).andReturn(null).anyTimes();

            return this;
        }

        public MockWebPanelModuleDescriptorBuilder setRenderedWebParams(final Map<String, String> params)
        {
            expect(webPanelModuleDescriptor.getWebParams()).andReturn(webParams).anyTimes();
            for (final Map.Entry<String, String> entry : params.entrySet())
            {
                expect(webParams.getRenderedParam(eq(entry.getKey()), eq(testContext))).andReturn(entry.getValue()).anyTimes();
            }

            return this;
        }


        public MockWebPanelModuleDescriptorBuilder setWebPanelHtml(final String panelHtml)
        {
            final Map<String, Object> testContextWithDescriptorKey =
                    Maps.newHashMap(testContext);
            testContextWithDescriptorKey.put(SummaryContextProvider.CONTEXT_PANEL_KEY, key);
            expect(webPanelModuleDescriptor.getModule()).andReturn(webPanel).anyTimes();
            expect(webPanel.getHtml(eq(testContextWithDescriptorKey))).andReturn(panelHtml).anyTimes();

            return this;
        }

        public WebPanelModuleDescriptor build()
        {
            return webPanelModuleDescriptor;
        }

        public static MockWebPanelModuleDescriptorBuilder builder(final IMocksControl mockControl, final Map<String, Object> testContext)
        {
            return new MockWebPanelModuleDescriptorBuilder(mockControl, testContext);
        }
    }
}
