package com.atlassian.jira.projectconfig.servlet;

import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.MockFieldScreen;
import com.atlassian.jira.issue.fields.screen.MockFieldScreenTab;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.projectconfig.order.MockComparatorFactory;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.easymock.classextension.EasyMock.createNiceControl;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @since v4.4
 */
public class TestInlineDialogServlet
{
    private IMocksControl control;
    private MockComparatorFactory comparatorFactory;
    private FieldScreenManager fieldScreenManager;
    private TemplateRenderer templateRenderer;
    private MockSimpleAuthenticationContext authContext;
    private ProjectService projectService;
    private MockVelocityRequestFactory contextFactory;
    private HttpServletResponse response;
    private HttpServletRequest request;
    private MockUser user;

    @Before
    public void setUp() throws Exception
    {
        control = createNiceControl();
        comparatorFactory = new MockComparatorFactory();
        fieldScreenManager = control.createMock(FieldScreenManager.class);
        templateRenderer = control.createMock(TemplateRenderer.class);
        user = new MockUser("bbain");
        authContext = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper());
        projectService = control.createMock(ProjectService.class);
        contextFactory = new MockVelocityRequestFactory();
        response = control.createMock(HttpServletResponse.class);
        request = control.createMock(HttpServletRequest.class);
    }

    @Test
    public void testPermissionNoProjectPassed() throws Exception
    {
        InlineDialogServlet dialogServlet = new InlineDialogServlet(comparatorFactory, fieldScreenManager, templateRenderer, authContext, projectService, contextFactory);

        PrintWriter writer = new PrintWriter(new StringWriter());
        expect(request.getParameter("projectKey")).andReturn(null);
        expect(response.getWriter()).andReturn(writer);
        
        Map<String, Object> expectedContext = ImmutableMap.<String, Object>of("message",
                NoopI18nHelper.makeTranslation("admin.project.fields.screens.perm.error"));
        
        templateRenderer.render("screens/warningpanel.vm", expectedContext, writer);

        control.replay();
        dialogServlet.doGet(request, response);
        control.verify();
    }

    @Test
    public void testPermissionNoProjectWithError() throws Exception
    {
        InlineDialogServlet dialogServlet = new InlineDialogServlet(comparatorFactory, fieldScreenManager, templateRenderer, authContext, projectService, contextFactory);

        PrintWriter writer = new PrintWriter(new StringWriter());
        String key = "SOMETHING";
        expect(request.getParameter("projectKey")).andReturn(key);
        expect(response.getWriter()).andReturn(writer);

        Map<String, Object> expectedContext = ImmutableMap.<String, Object>of("message",
                NoopI18nHelper.makeTranslation("admin.project.fields.screens.perm.error"));

        templateRenderer.render("screens/warningpanel.vm", expectedContext, writer);
        
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("ImAnError");
        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
            .andReturn(new ProjectService.GetProjectResult(errorCollection));

        control.replay();
        dialogServlet.doGet(request, response);
        control.verify();
    }

    @Test
    public void testPermissionNoProject() throws Exception
    {
        InlineDialogServlet dialogServlet = new InlineDialogServlet(comparatorFactory, fieldScreenManager, templateRenderer, authContext, projectService, contextFactory);

        String key = "SOMETHING";
        expect(request.getParameter("projectKey")).andReturn(key);

        PrintWriter writer = new PrintWriter(new StringWriter());
        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
            .andReturn(new ProjectService.GetProjectResult(new SimpleErrorCollection()));
        expect(response.getWriter()).andReturn(writer);

        Map<String, Object> expectedContext = ImmutableMap.<String, Object>of("message",
                NoopI18nHelper.makeTranslation("admin.project.fields.screens.perm.error"));

        templateRenderer.render("screens/warningpanel.vm", expectedContext, writer);

        control.replay();
        dialogServlet.doGet(request, response);
        control.verify();
    }

    @Test
    public void testNoFieldId() throws Exception
    {
        MockProject project = new MockProject();
        InlineDialogServlet dialogServlet = new InlineDialogServlet(comparatorFactory, fieldScreenManager, templateRenderer, authContext, projectService, contextFactory);

        String key = "SOMETHING";
        expect(request.getParameter("projectKey")).andReturn(key);
        expect(request.getParameter("fieldId")).andReturn(null);        

        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
            .andReturn(new ProjectService.GetProjectResult(new SimpleErrorCollection(), project));

        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No fieldId specified");

        control.replay();
        dialogServlet.doGet(request, response);
        control.verify();
    }

    @Test
    public void testHappyPath() throws Exception
    {
        MockProject project = new MockProject();

        MockFieldScreen fieldScreen1 = new MockFieldScreen();
        fieldScreen1.setId(4848L);
        fieldScreen1.setName("ZZZ");

        MockFieldScreen fieldScreen2 = new MockFieldScreen();
        fieldScreen2.setId(48482L);
        fieldScreen2.setName("AAA");

        MockFieldScreenTab tab1 = new MockFieldScreenTab();
        tab1.setName("ZZZZZ");
        tab1.setId(5l);
        tab1.setFieldScreen(fieldScreen1);
        MockFieldScreenTab tab2 = new MockFieldScreenTab();
        tab2.setName("AAAAA");
        tab2.setId(6l);
        tab2.setFieldScreen(fieldScreen2);

        String key = "SOMETHING";
        String field = "version";
        expect(request.getParameter("projectKey")).andReturn(key);
        expect(request.getParameter("fieldId")).andReturn(field);
        PrintWriter printWriter = new PrintWriter(new StringWriter());
        expect(response.getWriter()).andReturn(printWriter);

        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ProjectService.GetProjectResult(new SimpleErrorCollection(), project));
        expect(fieldScreenManager.getFieldScreenTabs(field)).andReturn(Lists.<FieldScreenTab>newArrayList(tab1, tab2));

        templateRenderer.render("screens/screensdialog.vm", ImmutableMap.<String, Object>of("screens", newArrayList(fieldScreen2, fieldScreen1)), printWriter);

        InlineDialogServlet dialogServlet = new InlineDialogServlet(comparatorFactory, fieldScreenManager, templateRenderer, authContext, projectService, contextFactory);
        control.replay();
        dialogServlet.doGet(request, response);
        control.verify();
    }
}
