package com.atlassian.jira.web.action;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.project.VersionProxy;
import com.atlassian.jira.web.SessionKeys;
import com.opensymphony.user.EntityNotFoundException;
import mock.servlet.MockHttpServletRequest;
import mock.servlet.MockHttpSession;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.net.URLEncoder;
import java.util.List;

public class TestIssueActionSupport extends AbstractUsersTestCase
{
    private MockHttpSession session;

    public TestIssueActionSupport(String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        session = new MockHttpSession();
        final MockHttpServletRequest servletRequest = new MockHttpServletRequest(session);
        ActionContext.setRequest(servletRequest);
    }

    public void testGetManagers()
    {
        MockIssueManager im = new MockIssueManager();
        ManagerFactory.addService(IssueManager.class, im);

        IssueActionSupport ias = new IssueActionSupport();

        assertEquals(im, ias.getIssueManager());
    }

    public void testGetSearchRequest()
    {
        SearchRequest sr = new SearchRequest();
        session.setAttribute(SessionKeys.SEARCH_REQUEST, sr);

        IssueActionSupport ias = new IssueActionSupport();
        assertEquals(sr, ias.getSearchRequest());
    }

    public void testGetSearchRequestNull()
    {
        IssueActionSupport ias = new IssueActionSupport();
        assertNull(ias.getSearchRequest());
    }

    public void testSetSearchRequest() throws EntityNotFoundException
    {
        SearchRequest sr = new SearchRequest();

        IssueActionSupport ias = new IssueActionSupport();
        ias.setSearchRequest(sr);

        assertEquals(sr, session.getAttribute(SessionKeys.SEARCH_REQUEST));
        assertEquals(sr, ias.getSearchRequest());

    }

    public void testGetURLEncoded()
    {
        IssueActionSupport ias = new IssueActionSupport();
        assertEquals(URLEncoder.encode("foobar baz"), ias.getUrlEncoded("foobar baz"));
    }

    public void testGetPossibleVersions() throws Exception
    {
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1)));
        UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(1), "project", new Long(1), "name", "foo", "released", null, "sequence", new Long(1)));
        UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(2), "project", new Long(1), "name", "bar", "released", null, "sequence", new Long(2)));
        UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(3), "project", new Long(1), "name", "baz", "released", "true", "sequence", new Long(3)));
        UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(4), "project", new Long(1), "name", "bat", "released", "true", "sequence", new Long(4)));

        IssueActionSupport ias = new IssueActionSupport();
        List versions = ias.getPossibleVersions(project);
        assertEquals(6, versions.size());

        VersionProxy proxy = (VersionProxy) versions.get(0);
        assertEquals(-2, proxy.getKey());
        assertEquals("Unreleased Versions", proxy.getValue());

        proxy = (VersionProxy) versions.get(1);
        assertEquals(1, proxy.getKey());
        assertEquals("foo", proxy.getValue());

        proxy = (VersionProxy) versions.get(2);
        assertEquals(2, proxy.getKey());
        assertEquals("bar", proxy.getValue());

        proxy = (VersionProxy) versions.get(3);
        assertEquals(-3, proxy.getKey());
        assertEquals("Released Versions", proxy.getValue());

        proxy = (VersionProxy) versions.get(4);
        assertEquals(4, proxy.getKey());
        assertEquals("bat", proxy.getValue());

        proxy = (VersionProxy) versions.get(5);
        assertEquals(3, proxy.getKey());
        assertEquals("baz", proxy.getValue());
    }


}
