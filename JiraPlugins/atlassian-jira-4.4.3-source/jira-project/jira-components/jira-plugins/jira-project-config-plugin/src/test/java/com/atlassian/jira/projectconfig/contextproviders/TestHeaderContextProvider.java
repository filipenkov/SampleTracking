package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLabel;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLink;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.beans.SimpleProject;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.collect.Lists;
import com.opensymphony.user.User;
import mock.user.MockOSUser;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestHeaderContextProvider
{
    private static final String DEFAULT_KEY = "default";

    private IMocksControl mockControl;
    private ContextProviderUtils contextProviderUtils;
    private WebInterfaceManager webInterfaceManager;
    private Project project;
    private Map<String, Object> testContext;
    private Map<String, Object> defaultContext;
    private Map<String, Object> webItemContext;
    private JiraAuthenticationContext authenticationContext;
    private User user;
    private JiraHelper helper;

    @Before
    public void setUp()
    {
        mockControl = EasyMock.createControl();

        webInterfaceManager = mockControl.createMock(WebInterfaceManager.class);
        contextProviderUtils = mockControl.createMock(ContextProviderUtils.class);
        user = new MockOSUser("admin", "administrator", "admin@admin.com");
        authenticationContext = new MockAuthenticationContext(user, null, null);

        testContext = MapBuilder.newBuilder(
                "a", "aValue",
                "b", "bValue",
                "c", new Object()
        ).toHashMap();

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
    public void testPluggableProjectOperations() throws Exception
    {
        final JiraWebItemModuleDescriptor webItemModuleDescriptor = mockControl.createMock(JiraWebItemModuleDescriptor.class);
        final JiraWebLink webLink = mockControl.createMock(JiraWebLink.class);
        final JiraWebLabel webLabel = mockControl.createMock(JiraWebLabel.class);

        final String linkId = "linkId";
        final String renderedUrl = "renderedUrl";
        final String displayableLabel = "displayableLabel";

        expect(webItemModuleDescriptor.getCompleteKey()).andReturn("jira.webfragments.view.project.operations:edit_project").andReturn("blah").anyTimes();

        expect(webItemModuleDescriptor.getLink()).andReturn(webLink).anyTimes();
        expect(webItemModuleDescriptor.getLabel()).andReturn(webLabel).anyTimes();
        expect(webLink.getId()).andReturn(linkId).anyTimes();
        expect(webLink.getRenderedUrl(eq(user), eq(helper))).andReturn(renderedUrl).anyTimes();
        expect(webLabel.getDisplayableLabel(eq(user), eq(helper))).andReturn(displayableLabel).anyTimes();

        expect(webInterfaceManager.getDisplayableItems("system.view.project.operations", webItemContext))
                .andReturn(Lists.<WebItemModuleDescriptor>newArrayList(
                        webItemModuleDescriptor,
                        webItemModuleDescriptor
                )).anyTimes();


        testContext.put(HeaderContextProvider.CURRENT_TAB, "view_project_summary");


        expect(contextProviderUtils.getDefaultContext()).andReturn(defaultContext).anyTimes();

        mockControl.replay();

        final HeaderContextProvider headerContextProvider = getHeaderContextProviderUnderTest(webInterfaceManager, contextProviderUtils, authenticationContext);
        Map<String, Object> headerContextMap = headerContextProvider.getContextMap(testContext);

        final SimpleProject expectedProject = expectedSimpleProjectFixture();
        final HeaderContextProvider.SimpleViewableProjectOperation expectedViewableProjectOperation =
                new HeaderContextProvider.SimpleViewableProjectOperation(linkId, urlEncode(renderedUrl), displayableLabel);

        Map<String, Object> expectedContext = MapBuilder.<String, Object>newBuilder()
                .addAll(testContext)
                .addAll(defaultContext)
                .add(HeaderContextProvider.CONTEXT_SIMPLE_PROJECT_KEY, expectedProject)
                .add(HeaderContextProvider.CONTEXT_VIEW_PROJECT_OPERATIONS_KEY,
                        Lists.<Object>newArrayList(expectedViewableProjectOperation))
                .add(HeaderContextProvider.CONTEXT_VIEW_PROJECT_EDIT_OPERATION_KEY, expectedViewableProjectOperation)
                .add(HeaderContextProvider.SHOW_ACTIONS_MENU, true)
                .add(DEFAULT_KEY, true)
                .toMap();

        assertEquals(headerContextMap, expectedContext);


        mockControl.verify();

        testContext.remove(HeaderContextProvider.CURRENT_TAB);
        testContext.put(HeaderContextProvider.CURRENT_TAB, "blah");

        headerContextMap = headerContextProvider.getContextMap(testContext);


        assertEquals("Expected show action menu flag to be false if not on summary panel",
                false, headerContextMap.get(HeaderContextProvider.SHOW_ACTIONS_MENU));
    }


    @Test
    public void testGetUrlEncodedRenderedUrl()
    {
        final String renderedUrl = "renderedUrl";

        expect(contextProviderUtils.createUrlBuilder(eq(renderedUrl)))
                .andReturn(new UrlBuilder("urlEncoder", "UTF-8", false));

        mockControl.replay();

        final HeaderContextProvider headerContextProvider = new HeaderContextProvider(contextProviderUtils, authenticationContext, webInterfaceManager);
        final String urlEncodedRenderedUrl = headerContextProvider.getUrlEncodedRenderedUrl(renderedUrl);

        assertEquals("urlEncoder", urlEncodedRenderedUrl);

        mockControl.verify();
    }


    private HeaderContextProvider getHeaderContextProviderUnderTest(WebInterfaceManager webInterfaceManager,
            ContextProviderUtils contextProviderUtils, JiraAuthenticationContext authenticationContext)
    {
        return new HeaderContextProvider(contextProviderUtils, authenticationContext, webInterfaceManager)
        {
            @Override
            JiraHelper getJiraHelper(Project project)
            {
                return helper;
            }

            @Override
            String getUrlEncodedRenderedUrl(String renderedUrl)
            {
                return urlEncode(renderedUrl);
            }

            @Override
            Map<String, Object> createContext(Map<String, Object> params)
            {
                return MapBuilder.newBuilder(params).add(DEFAULT_KEY, true).toMap();
            }
        };
    }

    private static String urlEncode(final String renderedUrl)
    {
        return "urlEncoded:" + renderedUrl;
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
}
