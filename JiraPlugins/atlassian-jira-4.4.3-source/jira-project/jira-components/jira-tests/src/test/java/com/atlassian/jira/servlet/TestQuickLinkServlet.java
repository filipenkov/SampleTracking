package com.atlassian.jira.servlet;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.TestJiraKeyUtils;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpServletResponse;
import com.mockobjects.servlet.MockRequestDispatcher;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.atlassian.jira.easymock.EasyMockMatcherUtils.any;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;


public class TestQuickLinkServlet extends ListeningTestCase
{
    private GenericValue project = new MockGenericValue("Project", EasyMap.build("key", "JRA", "id", new Long(1)));
    private GenericValue projectWithTrailingSlashAtEnd = new MockGenericValue("Trailing Slash Project", EasyMap.build("key", "TSL/", "id", new Long(2)));
    private GenericValue projectWithTrailingSlash = new MockGenericValue("middle slash project", EasyMap.build("key", "JRA/ARJ", "id", new Long(3)));
    MockRequest mockRequest = new MockRequest();
    MockHttpServletResponse mockResponse = new MockHttpServletResponse();
    MockProjectManager projectManager = new MockProjectManager();
    MockIssue issue = new MockIssue();
    MockIssueManager issueManager = new MockIssueManager()
    {
        public MutableIssue getIssueObject(String key) throws DataAccessException
        {
            if ("JRA-1".equals(key))
            {
                issue.setKey("JRA-1");
                return issue;
            }
            return null;
        }
    };


    ChangeHistoryManager changeHistoryManager;
    QuickLinkServlet quickLinkServlet;

    @Before
    public void setUp() throws Exception
    {
        changeHistoryManager = EasyMock.createNiceMock(ChangeHistoryManager.class);
        replay(changeHistoryManager);
        projectManager.addProject(projectWithTrailingSlashAtEnd);
        projectManager.addProject(project);
        projectManager.addProject(projectWithTrailingSlash);
        quickLinkServlet = buildQuickLinkServlet(projectManager, issueManager, changeHistoryManager);
        TestJiraKeyUtils.setKeyMatcher(new TestJiraKeyUtils.MockKeyMatcher(""));
    }

    @Test
    public void testProjectForward() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/JRA");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/BrowseProject.jspa?id=" + project.getLong("id"), mockRequest.pathRequested);
    }

    @Test
    public void testVersionForward() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/JRA/fixforversion/10000");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/BrowseVersion.jspa?id=" + project.getLong("id") + "&versionId=10000", mockRequest.pathRequested);
    }

    @Test
    public void testComponentForward() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/JRA/component/10000");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/BrowseComponent.jspa?id=" + project.getLong("id") + "&componentId=10000", mockRequest.pathRequested);
    }

    @Test
    public void testVersionForwardWithSlashAtEnd() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/TSL//fixforversion/10000");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/BrowseVersion.jspa?id=" + projectWithTrailingSlashAtEnd.getLong("id") + "&versionId=10000", mockRequest.pathRequested);
    }

    @Test
    public void testVersionForwardWithSlash() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/JRA/ARJ/fixforversion/10000");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/BrowseVersion.jspa?id=" + projectWithTrailingSlash.getLong("id") + "&versionId=10000", mockRequest.pathRequested);
    }

    @Test
    public void testComponentForwardWithSlashAtEnd() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/TSL//component/10000");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/BrowseComponent.jspa?id=" + projectWithTrailingSlashAtEnd.getLong("id") + "&componentId=10000", mockRequest.pathRequested);
    }

    @Test
    public void testComponentForwardWithSlash() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/JRA/ARJ/component/10000");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/BrowseComponent.jspa?id=" + projectWithTrailingSlash.getLong("id") + "&componentId=10000", mockRequest.pathRequested);
    }


    @Test
    public void testProjectForwardLowercase() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/jra");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/BrowseProject.jspa?id=" + project.getLong("id"), mockRequest.pathRequested);
    }

    @Test
    public void testProjectForwardTrailingSlashNotPartOfProjectName() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/JRA/");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/BrowseProject.jspa?id=" + project.getLong("id"), mockRequest.pathRequested);
    }

    @Test
    public void testProjectForwardTrailingSlashPartOfProjectName() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/TSL/");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/BrowseProject.jspa?id=" + projectWithTrailingSlashAtEnd.getLong("id"), mockRequest.pathRequested);
    }

    @Test
    public void testBrowseRedirect() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/BrowseProject.jspa", mockRequest.pathRequested);
    }

    @Test
    public void testInvalidProject() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/ZZZ");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/views/projectnotfound.jsp", mockRequest.pathRequested);
    }

    @Test
    public void testInvalidProjectMissingTrailingSlash() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/TSL");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/views/projectnotfound.jsp", mockRequest.pathRequested);
    }

    @Test
    public void testBrowseRedirect2() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("");
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/BrowseProject.jspa", mockRequest.pathRequested);
    }

    @Test
    public void testIssueForward() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/JRA-1");
        String queryString = mockRequest.getQueryString() != null ? "&" + mockRequest.getQueryString() : "";
        quickLinkServlet.service(mockRequest, mockResponse);

        assertEquals("/secure/ViewIssue.jspa?key=" + issue.getKey() + queryString, mockRequest.pathRequested);
    }

    @Test
    public void testForwardToMovedIssue() throws Exception
    {
        IssueManager mockIssueManager = new MockIssueManager()
        {
            public MutableIssue getIssueObject(String key) throws DataAccessException
            {
                return null;
            }
        };

        ChangeHistoryManager mockChangeHistoryManager = mockChangeHistoryManagerForMovedIssue("MOVED-1");

        QuickLinkServlet quickLinkServlet = buildQuickLinkServlet(null, mockIssueManager, mockChangeHistoryManager);

        Mock mockRequest = new Mock(HttpServletRequest.class);
        mockRequest.expectAndReturn("getPathInfo", "/JRA-1");
        mockRequest.expectAndReturn("getContextPath", "/test-context-path");
        mockRequest.expectAndReturn("getQueryString", "key=value");

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expectVoid("sendRedirect", "/test-context-path/browse/MOVED-1?key=value");

        quickLinkServlet.service((HttpServletRequest) mockRequest.proxy(), (HttpServletResponse) mockResponse.proxy());

        mockRequest.verify();
        mockResponse.verify();
    }

    @Test
    public void testForwardToMovedIssueNullContext() throws Exception
    {
        IssueManager mockIssueManager = new MockIssueManager()
        {
            public MutableIssue getIssueObject(String key) throws DataAccessException
            {
                return null;
            }
        };

        ChangeHistoryManager mockChangeHistoryManager = mockChangeHistoryManagerForMovedIssue("MOVED-1");
        QuickLinkServlet quickLinkServlet = buildQuickLinkServlet(null, mockIssueManager, mockChangeHistoryManager);

        Mock mockRequest = new Mock(HttpServletRequest.class);
        mockRequest.expectAndReturn("getPathInfo", "/JRA-1");
        mockRequest.expectAndReturn("getContextPath", null);
        mockRequest.expectAndReturn("getQueryString", "key=value"); //getQueryString does not have a leading question mark automatically appended

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expectVoid("sendRedirect", "/browse/MOVED-1?key=value");

        quickLinkServlet.service((HttpServletRequest) mockRequest.proxy(), (HttpServletResponse) mockResponse.proxy());

        mockRequest.verify();
        mockResponse.verify();
    }

    /**
     * This test now tests that even for null issues, the filter simply forwards on the request (JRA-8961)
     *
     * @throws IOException
     * @throws ServletException
     */
    @Test
    public void testNoIssueExists() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/NULL-1");
        String queryString = mockRequest.getQueryString() != null ? "&" + mockRequest.getQueryString() : "";
        quickLinkServlet.service(mockRequest, mockResponse);
        assertEquals("/secure/ViewIssue.jspa?key=NULL-1" + queryString, mockRequest.pathRequested);
    }

    @Test
    public void testNoProjectExists() throws IOException, ServletException
    {
        mockRequest.setupPathInfo("/NULL");
        mockResponse.setExpectedSetStatusCalls(1);
        quickLinkServlet.service(mockRequest, mockResponse);
        mockResponse.verify();
        // fixme: would check for a 404 response code if mockResponse had the method
    }

    @Test
    public void testLowerCaseIssue() throws IOException, ServletException
    {
        MockRequest mockRequest = new MockRequest();
        mockRequest.setupPathInfo("/jra-1");

        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        String queryString = mockRequest.getQueryString() != null ? "&" + mockRequest.getQueryString() : "";

        quickLinkServlet.service(mockRequest, mockHttpServletResponse);

        assertEquals("/secure/ViewIssue.jspa?key=" + issue.getKey() + queryString, mockRequest.pathRequested);
    }

    private ChangeHistoryManager mockChangeHistoryManagerForMovedIssue(final String movedIssueKey)
            throws GenericEntityException
    {
        ChangeHistoryManager mockChangeHistoryManager = createNiceMock(ChangeHistoryManager.class);
        expect(mockChangeHistoryManager.findMovedIssue(any(String.class))).andReturn(new MockIssue()
        {
            public String getKey()
            {
                return movedIssueKey;
            }
        });
        replay(mockChangeHistoryManager);
        return mockChangeHistoryManager;
    }

    private QuickLinkServlet buildQuickLinkServlet(final ProjectManager projectManager, final IssueManager issueManager, final ChangeHistoryManager changeHistoryManager)
    {
        return new QuickLinkServlet()
        {
            ProjectManager getProjectManager()
            {
                return projectManager;
            }

            IssueManager getIssueManager()
            {
                return issueManager;
            }

            ChangeHistoryManager getChangeHistoryManager()
            {
                return changeHistoryManager;
            }
        };
    }

    private class MockRequest extends MockHttpServletRequest
    {
        public String pathRequested = "";
        public String contextPath = "";

        public RequestDispatcher getRequestDispatcher(String path)
        {
            pathRequested = path;
            return new MockRequestDispatcher();
        }

        public String getQueryString()
        {
            return null;
        }

        public String getContextPath()
        {
            return contextPath;
        }

        public int getRemotePort()
        {
            return 0;
        }

        public String getLocalName()
        {
            return null;
        }

        public String getLocalAddr()
        {
            return null;
        }

        public int getLocalPort()
        {
            return 0;
        }
    }

}
