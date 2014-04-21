package com.sysbliss.jira.plugins.workflow.servlet;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugins.workflow.MockStatusConstantsManager;
import com.atlassian.jira.plugins.workflow.SimpleConfigurableJiraWorkflow;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.AbstractJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;
import com.sysbliss.jira.plugins.workflow.manager.*;
import com.sysbliss.jira.plugins.workflow.util.WorkflowDesignerPropertySet;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WorkflowThumbnailServletTest
{
    public static final String BASE_SERVLET_URL = "http://servletrunner.atlassian.com/thumbnailServlet";

    private WorkflowImageManager workflowImageManager;
    private WorkflowService workflowService;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private PermissionManager permissionManager;

    private User user;
    private ProjectService mockProjectService;
    private WorkflowSchemeManager mockWorkflowSchemeManager;

    @Before
    public void setup() throws IOException, SAXException, InvalidWorkflowDescriptorException, ServletException
    {
        //load the default workflow
        InputStream workflowXml = this.getClass().getClassLoader().getResourceAsStream("default-jira-workflow.xml");
        final WorkflowDescriptor workflowDescriptor = WorkflowLoader.load(workflowXml, true);
        final WorkflowManager workflowManager = mock(WorkflowManager.class);

        //mock the jiraHome
        final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
        final File baseCacheDir = new File(sysTempDir, "caches");

        if (!baseCacheDir.exists())
        {
            baseCacheDir.mkdirs();
        }

        final JiraHome jiraHome = mock(JiraHome.class);
        when(jiraHome.getCachesDirectory()).thenReturn(baseCacheDir);

        //clean the cache
        File thumbsDir = new File(jiraHome.getCachesDirectory(), CachingWorkflowImageManagerImpl.BASE_CACHE_FOLDER);
        if (thumbsDir.exists())
        {
            FileUtils.deleteDirectory(thumbsDir);
        }

        ConstantsManager constantsManager = new MockStatusConstantsManager();
        WorkflowLayoutManager layoutManager = new WorkflowLayoutManagerImpl(constantsManager, new MockVWDPropertySet());
        workflowImageManager = new CachingWorkflowImageManagerImpl(new WorkflowImageManagerImpl(constantsManager, layoutManager), jiraHome);

        //setup the services to inject into the servlet
        user = mock(User.class);
        when(user.getName()).thenReturn("Ricky Bobby");
        when(user.getDirectoryId()).thenReturn(1L);
        when(user.getDisplayName()).thenReturn("Ricky Bobby");
        when(user.isActive()).thenReturn(true);
        when(user.getEmailAddress()).thenReturn("ricky@bobby.com");

        jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(user);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -5);
        workflowDescriptor.getMetaAttributes().put(AbstractJiraWorkflow.JIRA_META_UPDATED_DATE, Long.toString(cal.getTime().getTime()));

        JiraWorkflow workflow = new SimpleConfigurableJiraWorkflow("jira", false, workflowDescriptor, workflowManager, true);
        workflowService = mock(WorkflowService.class);
        when(workflowService.getWorkflow(Matchers.<JiraServiceContext>anyObject(), Matchers.startsWith("jira"))).thenReturn(workflow);

        final IssueType mockIssueType = mock(IssueType.class);
        when(mockIssueType.getId()).thenReturn("1");

        final Project mockProject = mock(Project.class);
        when(mockProject.getIssueTypes()).thenReturn(Lists.<IssueType>newArrayList(mockIssueType));

        mockProjectService = mock(ProjectService.class);
        mockWorkflowSchemeManager = mock(WorkflowSchemeManager.class);

        when(mockProjectService.getAllProjectsForAction(user, ProjectAction.VIEW_PROJECT)).thenReturn(
                new ServiceOutcomeImpl<List<Project>>(new SimpleErrorCollection(), Lists.newArrayList(mockProject)));

        when(mockWorkflowSchemeManager.getWorkflowMap(mockProject)).thenReturn(Collections.<String, String>emptyMap());

        permissionManager = mock(PermissionManager.class);
        when(permissionManager.hasPermission(Permissions.VIEW_WORKFLOW_READONLY, mockProject, user)).thenReturn(true);


    }

    @Test
    public void simpleWebRequestReturnsImage() throws IOException, SAXException, ServletException
    {
        final HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getParameter("workflowName")).thenReturn("jira");

        final HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        MyServletUnitOutputStream outputStream = new MyServletUnitOutputStream(new ByteArrayOutputStream());
        when(mockResponse.getOutputStream()).thenReturn(outputStream);
        WorkflowThumbnailServlet servlet = new WorkflowThumbnailServlet(workflowService, jiraAuthenticationContext, workflowImageManager, permissionManager, mockWorkflowSchemeManager, mockProjectService);
        servlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).setContentType(WorkflowThumbnailServlet.CONTENTTYPE_PNG);
        verify(mockResponse).setStatus(200);
        assertTrue(outputStream.getOutputStream().size() > 1);
    }

    @Test
    public void noPermissionToViewWorkflow() throws IOException, SAXException, ServletException
    {
        final Project mockProject = mock(Project.class);

        when(mockProjectService.getAllProjectsForAction(user, ProjectAction.VIEW_PROJECT)).thenReturn(
                new ServiceOutcomeImpl<List<Project>>(new SimpleErrorCollection(), Lists.newArrayList(mockProject)));

        when(mockWorkflowSchemeManager.getWorkflowMap(mockProject)).thenReturn(Collections.<String, String>emptyMap());


        permissionManager = mock(PermissionManager.class);
        when(permissionManager.hasPermission(Permissions.VIEW_WORKFLOW_READONLY, mockProject, user)).thenReturn(false);

        final HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getParameter("workflowName")).thenReturn("jira");

        final HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        MyServletUnitOutputStream outputStream = new MyServletUnitOutputStream(new ByteArrayOutputStream());
        when(mockResponse.getOutputStream()).thenReturn(outputStream);
        WorkflowThumbnailServlet servlet = new WorkflowThumbnailServlet(workflowService, jiraAuthenticationContext, workflowImageManager, permissionManager, mockWorkflowSchemeManager, mockProjectService);
        servlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).setContentType(WorkflowThumbnailServlet.CONTENTTYPE_PNG);
        verify(mockResponse).setStatus(404);
        assertTrue(outputStream.getOutputStream().size() < 1);
    }

    @Test
    public void unknowWorkflowReturnsNotFound() throws IOException, SAXException, ServletException
    {
        final HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getParameter("workflowName")).thenReturn("does not exist");

        final HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        MyServletUnitOutputStream outputStream = new MyServletUnitOutputStream(new ByteArrayOutputStream());
        when(mockResponse.getOutputStream()).thenReturn(outputStream);
        WorkflowThumbnailServlet servlet = new WorkflowThumbnailServlet(workflowService, jiraAuthenticationContext, workflowImageManager, permissionManager, mockWorkflowSchemeManager, mockProjectService);
        servlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).setContentType(WorkflowThumbnailServlet.CONTENTTYPE_PNG);
        verify(mockResponse).setStatus(404);
        assertTrue(outputStream.getOutputStream().size() < 1);
    }

    static class MyServletUnitOutputStream extends ServletOutputStream
    {
        private final ByteArrayOutputStream outputStream;

        MyServletUnitOutputStream(ByteArrayOutputStream outputStream)
        {
            this.outputStream = outputStream;
        }

        @Override
        public void write(final int i) throws IOException
        {
            outputStream.write(i);
        }

        public ByteArrayOutputStream getOutputStream()
        {
            return outputStream;
        }
    }

    private class MockVWDPropertySet implements WorkflowDesignerPropertySet {

        public void setProperty(String key, String value) {
            //do nothing
        }

        public String getProperty(String key) {
            return "";
        }

        public void removeProperty(String key) {
            //do nothing;
        }

        public boolean hasProperty(String key) {
            return true;
        }
    }
}
