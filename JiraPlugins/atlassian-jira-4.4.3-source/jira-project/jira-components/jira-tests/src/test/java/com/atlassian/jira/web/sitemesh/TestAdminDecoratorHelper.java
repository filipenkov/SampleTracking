package com.atlassian.jira.web.sitemesh;

import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.component.webfragment.AdminTabsWebComponent;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.model.WebPanel;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class TestAdminDecoratorHelper
{
    private IMocksControl control;
    private WebInterfaceManager webInterfaceManager;
    private ProjectService projectService;
    private JiraAuthenticationContext authenticationContext;
    private ComponentFactory jiraComponentFactory;
    private MockUser user;
    private AdminTabsWebComponent tabsWebComponent;

    @Before
    public void setUp() throws Exception
    {
        control = createControl();
        webInterfaceManager = control.createMock(WebInterfaceManager.class);
        projectService = control.createMock(ProjectService.class);
        user = new MockUser("bbain");
        authenticationContext = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper());
        jiraComponentFactory = control.createMock(ComponentFactory.class);

        tabsWebComponent = control.createMock(AdminTabsWebComponent.class);
        expect(jiraComponentFactory.createObject(AdminTabsWebComponent.class)).andReturn(tabsWebComponent);
    }

    @Test
    public void testGetHeadersNoProjectKey() throws Exception
    {
        String panel1Content = "panel1";
        String panel2Content = "panel2";

        Map<String, Object> expectedContext = MapBuilder.<String, Object>build("admin.active.section", "some.section", "admin.active.tab", "blah");

        WebPanel panel1 = control.createMock(WebPanel.class);
        expect(panel1.getHtml(expectedContext)).andReturn(panel1Content);
        WebPanel panel2 = control.createMock(WebPanel.class);
        expect(panel2.getHtml(expectedContext)).andReturn(panel2Content);

        expect(webInterfaceManager.getDisplayableWebPanels("system.admin.decorator.header", Collections.<String, Object>emptyMap()))
                .andReturn(CollectionBuilder.list(panel1, panel2));

        control.replay();

        AdminDecoratorHelper helper = new AdminDecoratorHelper(webInterfaceManager, projectService,
                authenticationContext, jiraComponentFactory);

        helper.setCurrentSection("some.section");
        helper.setCurrentTab("blah");

        assertFalse(helper.hasKey());
        final List<AdminDecoratorHelper.Header> actualHeaders = helper.getHeaders();
        assertEquals(2, actualHeaders.size());
        assertEquals(panel1Content, actualHeaders.get(0).getHtml());
        assertEquals(panel2Content, actualHeaders.get(1).getHtml());

        control.verify();
    }

    @Test
    public void testGetHeadersWithoutProject() throws Exception
    {
        String key = "ABC";
        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ProjectService.GetProjectResult(error("Not Found")));

        expect(webInterfaceManager.getDisplayableWebPanels("system.admin.decorator.header", Collections.<String, Object>emptyMap()))
                .andReturn(Collections.<WebPanel>emptyList());

        control.replay();

        AdminDecoratorHelper helper = new AdminDecoratorHelper(webInterfaceManager, projectService,
                authenticationContext, jiraComponentFactory);

        helper.setProject(key);
        assertTrue(helper.hasKey());
        assertEquals(Collections.<AdminDecoratorHelper.Header>emptyList(), helper.getHeaders());
        assertEquals(Collections.<AdminDecoratorHelper.Header>emptyList(), helper.getHeaders());

        control.verify();
    }

    @Test
    public void testGetHeadersProject() throws Exception
    {
        String key = "ABC";
        String panel1Content = "panel1";
        String panel2Content = "panel2";

        Project project = new MockProject(101010L, key);
        Map<String, Object> expectedContext = MapBuilder.<String, Object>build("project", project, "admin.active.section", null, "admin.active.tab", "blah");

        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ProjectService.GetProjectResult(ok(), project));

        WebPanel panel1 = control.createMock(WebPanel.class);
        expect(panel1.getHtml(expectedContext)).andReturn(panel1Content);
        WebPanel panel2 = control.createMock(WebPanel.class);
        expect(panel2.getHtml(expectedContext)).andReturn(panel2Content);

        expect(webInterfaceManager.getDisplayableWebPanels("atl.jira.proj.config.header", Collections.<String, Object>emptyMap()))
                .andReturn(CollectionBuilder.list(panel1, panel2));

        control.replay();

        AdminDecoratorHelper helper = new AdminDecoratorHelper(webInterfaceManager, projectService,
                authenticationContext, jiraComponentFactory);

        helper.setProject(key);
        assertTrue(helper.hasKey());

        helper.setCurrentTab("blah");

        List<AdminDecoratorHelper.Header> actualHeaders = helper.getHeaders();

        assertEquals(2, actualHeaders.size());
        assertEquals(panel1Content, actualHeaders.get(0).getHtml());
        assertEquals(panel2Content, actualHeaders.get(1).getHtml());

        //The last result should have been cached.
        assertSame(actualHeaders, helper.getHeaders());
        control.verify();
    }

    @Test
    public void testGetTabHtmlWithProject() throws Exception
    {
        String key = "ABC";
        String panel = "panel";
        String tab1content = "panel1";
        String tab2content = "panel2";

        Project project = new MockProject(101010L, key);

        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ProjectService.GetProjectResult(ok(), project)).anyTimes();

        expect(tabsWebComponent.render(project, null, null)).andReturn(Pair.of(tab1content, 5));
        expect(tabsWebComponent.render(project, null, panel)).andReturn(Pair.of(tab2content, 6));

        control.replay();

        AdminDecoratorHelper helper = new AdminDecoratorHelper(webInterfaceManager, projectService,
                authenticationContext, jiraComponentFactory);

        helper.setProject(key);
        assertTrue(helper.hasKey());

        assertEquals(tab1content, helper.getTabHtml());

        helper.setCurrentTab(panel);
        assertEquals(tab2content, helper.getTabHtml());

        control.verify();
    }

    private ErrorCollection ok()
    {
        return new SimpleErrorCollection();
    }

    private ErrorCollection error(String... msgs)
    {
        SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
        simpleErrorCollection.addErrorMessages(Arrays.asList(msgs));
        return simpleErrorCollection;
    }
}
