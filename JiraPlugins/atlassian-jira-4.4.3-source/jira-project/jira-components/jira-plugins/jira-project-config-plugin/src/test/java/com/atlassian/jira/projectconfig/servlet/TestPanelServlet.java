package com.atlassian.jira.projectconfig.servlet;

import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.tab.ProjectConfigTab;
import com.atlassian.jira.projectconfig.tab.ProjectConfigTabManager;
import com.atlassian.jira.projectconfig.util.MockProjectConfigRequestCache;
import com.atlassian.jira.projectconfig.util.ProjectConfigRequestCache;
import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createControl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class TestPanelServlet
{
    private IMocksControl control;
    private MockSimpleAuthenticationContext authCtx;
    private ProjectConfigTabManager tabManager;
    private ProjectService projectService;
    private TemplateRenderer templateRenderer;
    private WebResourceManager webResourceManager;
    private UserProjectHistoryManager userProjectHistoryManager;
    private ApplicationProperties applicationProperties;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private VelocityRequestContext velocityRequestContext;
    private VelocityRequestSession velocityRequestSession;
    private VelocityContextFactory contextFactory;
    private HttpServletResponse response;
    private HttpServletRequest request;
    private ProjectConfigRequestCache cache;

    @Before
    public void setUp() throws Exception
    {
        control = createControl();
        authCtx = new MockSimpleAuthenticationContext(new MockUser("bbain"), Locale.ENGLISH, new NoopI18nHelper());
        tabManager = control.createMock(ProjectConfigTabManager.class);
        projectService = control.createMock(ProjectService.class);
        templateRenderer = control.createMock(TemplateRenderer.class);
        webResourceManager = control.createMock(WebResourceManager.class);
        userProjectHistoryManager = control.createMock(UserProjectHistoryManager.class);
        applicationProperties = control.createMock(ApplicationProperties.class);
        contextFactory = control.createMock(VelocityContextFactory.class);
        velocityRequestContextFactory = control.createMock(VelocityRequestContextFactory.class);
        velocityRequestContext = control.createMock(VelocityRequestContext.class);
        velocityRequestSession = control.createMock(VelocityRequestSession.class);
        response = control.createMock(HttpServletResponse.class);
        request = control.createMock(HttpServletRequest.class);
        cache = new MockProjectConfigRequestCache();
    }

    @Test
    public void testPathMatching()
    {
        assertMatchesPatten("/project-config/B", "B", null, null);
        assertMatchesPatten("//project-config/B/", "B", null, null);
        assertMatchesPatten("/project-config/B///","B", null, null);
        assertMatchesPatten("project-config////B///", "B", null, null);

        assertMatchesPatten("/project-config/B/aaaa", "B", "aaaa", null);
        assertMatchesPatten("//project-config/B/aaa//", "B", "aaa", null);
        assertMatchesPatten("/project-config/B///aaaaa","B", "aaaaa", null);
        assertMatchesPatten("project-config////B///aaaa///", "B", "aaaa", null);

        assertMatchesPatten("/project-config/B/aaaa/bbb", "B", "aaaa", "bbb");
        assertMatchesPatten("//project-config/B/aaa//bbbb", "B", "aaa", "bbbb");
        assertMatchesPatten("/project-config/B///aaaaa/bbbbb////","B", "aaaaa", "bbbbb////");
        assertMatchesPatten("project-config////BJ///aaaa///bbbbbbb//////", "BJ", "aaaa", "bbbbbbb//////");

        assertNotMatchesPatten("qwerty");
        assertNotMatchesPatten("project-config/");
        assertNotMatchesPatten("project-config/////");
    }

    @Test
    public void testDoGetNoProjectSpecified() throws Exception
    {
        expect(request.getPathInfo()).andReturn("/project-config////");

        final AtomicBoolean called = new AtomicBoolean(false);
        PanelServlet servlet = new PanelServlet(authCtx, tabManager, projectService, templateRenderer, webResourceManager, applicationProperties, contextFactory, userProjectHistoryManager, null, cache)
        {
            @Override
            void outputError(HttpServletResponse reponse, Collection<String> errorMessage, String title)
                    throws IOException
            {
                assertEquals(Collections.singleton(NoopI18nHelper.makeTranslation("admin.project.servlet.no.project")), errorMessage);
                assertEquals(NoopI18nHelper.makeTranslation("common.words.error"), title);
                called.set(true);
            }
        };

        control.replay();

        servlet.doGet(request, response);
        assertTrue(called.get());

        control.verify();
    }

    @Test
    public void testDoGetProjectDoesNotExist() throws Exception
    {
        final String error = "my error";

        expect(request.getPathInfo()).andReturn("/project-config/abc");
        expect(projectService.getProjectByKeyForAction(authCtx.getLoggedInUser(), "abc", ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ProjectService.GetProjectResult(errors(error)));

        final AtomicBoolean called = new AtomicBoolean(false);
        PanelServlet servlet = new PanelServlet(authCtx, tabManager, projectService, templateRenderer, webResourceManager, applicationProperties, contextFactory, userProjectHistoryManager, null, cache)
        {
            @Override
            void outputError(HttpServletResponse reponse, Collection<String> errorMessage, String title)
                    throws IOException
            {
                assertEquals(Collections.singleton(error), errorMessage);
                assertEquals(NoopI18nHelper.makeTranslation("common.words.error"), title);
                called.set(true);
            }
        };

        control.replay();

        servlet.doGet(request, response);
        assertTrue(called.get());

        control.verify();
    }

    @Test
    public void testDoGetProjectDoesNotExistAnonymous() throws Exception
    {
        final String error = "my error";

        authCtx.setLoggedInUser(null);

        expect(request.getPathInfo()).andReturn("/project-config/abc");
        expect(projectService.getProjectByKeyForAction(authCtx.getLoggedInUser(), "abc", ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ProjectService.GetProjectResult(errors(error)));

        final AtomicBoolean called = new AtomicBoolean(false);
        PanelServlet servlet = new PanelServlet(authCtx, tabManager, projectService, templateRenderer, webResourceManager, applicationProperties, contextFactory, userProjectHistoryManager, null, cache)
        {
            @Override
            void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
            {
                called.set(true);
            }
        };

        control.replay();

        servlet.doGet(request, response);
        assertTrue(called.get());

        control.verify();
    }

    @Test
    public void testDoGetTabDoesNotExist() throws Exception
    {
        final Project project = new MockProject(6373L);

        expect(request.getPathInfo()).andReturn("/project-config/abc/dontexist");
        expect(projectService.getProjectByKeyForAction(authCtx.getLoggedInUser(), "abc", ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ProjectService.GetProjectResult(errors(), project));
        expect(tabManager.getTabForId("dontexist")).andReturn(null);

        final AtomicBoolean called = new AtomicBoolean(false);
        PanelServlet servlet = new PanelServlet(authCtx, tabManager, projectService, templateRenderer, webResourceManager, applicationProperties, contextFactory, userProjectHistoryManager, null, cache)
        {
            @Override
            void outputError(HttpServletResponse reponse, Collection<String> errorMessage, String title)
                    throws IOException
            {
                assertEquals(Collections.singleton(NoopI18nHelper.makeTranslation("admin.project.servlet.no.tab", "dontexist")), errorMessage);
                assertEquals(NoopI18nHelper.makeTranslation("common.words.error"), title);
                called.set(true);
            }
        };

        control.replay();

        servlet.doGet(request, response);
        assertTrue(called.get());

        control.verify();
    }

    @Test
    public void testDoGetHappyPath() throws Exception
    {
        final Project project = new MockProject(6373L, "PROJKEY");

        final ProjectConfigTab tabPanel = control.createMock(ProjectConfigTab.class);
        expect(tabPanel.getId()).andReturn("someId");
        final ProjectService.GetProjectResult result =  new ProjectService.GetProjectResult(errors(), project);

        expect(request.getPathInfo()).andReturn("/project-config/abc/dontexist/extra");
        expect(projectService.getProjectByKeyForAction(authCtx.getLoggedInUser(), "abc", ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(result);
        expect(tabManager.getTabForId("dontexist")).andReturn(tabPanel);
        userProjectHistoryManager.addProjectToHistory(authCtx.getLoggedInUser(),result.getProject());

        expect(velocityRequestContextFactory.getJiraVelocityRequestContext()).andReturn(velocityRequestContext);
        expect(velocityRequestContext.getSession()).andReturn(velocityRequestSession);
        velocityRequestSession.setAttribute(SessionKeys.CURRENT_ADMIN_PROJECT, "PROJKEY");
        velocityRequestSession.setAttribute(SessionKeys.CURRENT_ADMIN_PROJECT_TAB, "someId");

        final AtomicBoolean called = new AtomicBoolean(false);
        PanelServlet servlet = new PanelServlet(authCtx, tabManager, projectService, templateRenderer, webResourceManager, applicationProperties, contextFactory, userProjectHistoryManager, velocityRequestContextFactory, cache)
        {
            @Override
            void outputTab(HttpServletRequest actualRequest, HttpServletResponse actualResponse, Project actualProject, ProjectConfigTab actualTab, String actualExtra)
                    throws IOException
            {
                assertSame(request, actualRequest);
                assertSame(response, actualResponse);
                assertSame(project, actualProject);
                assertSame(tabPanel, actualTab);
                assertEquals("extra", actualExtra);

                called.set(true);
            }
        };

        control.replay();

        servlet.doGet(request, response);
        assertTrue(called.get());

        control.verify();
    }

    private void assertMatchesPatten(String url, String project, String pannel, String extra)
    {
        Matcher matcher = PanelServlet.PATTERN.matcher(url);
        assertTrue(matcher.matches());
        assertEquals(project, matcher.group(1));
        assertEquals(pannel, matcher.group(2));
        assertEquals(extra, matcher.group(3));
    }

    private void assertNotMatchesPatten(String url)
    {
        assertFalse(PanelServlet.PATTERN.matcher(url).matches());
    }

    private ErrorCollection errors(String...errors)
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessages(Arrays.asList(errors));
        return errorCollection;
    }
}
