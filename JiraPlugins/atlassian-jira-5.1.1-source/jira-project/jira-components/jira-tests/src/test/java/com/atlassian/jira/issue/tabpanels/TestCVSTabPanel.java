package com.atlassian.jira.issue.tabpanels;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.vcs.RepositoryException;
import com.atlassian.jira.vcs.RepositoryManager;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;

public class TestCVSTabPanel extends AbstractUsersTestCase
{
    CVSTabPanel CVSTabPanel;
    private User user;
    private Issue issueObject;
    private GenericValue project;

    public TestCVSTabPanel(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "test project"));
        GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "project", project.getLong("id"), "key", "TST-1"));
        issueObject = IssueImpl.getIssueObject(issue);
        user = new MockUser("Owen");
    }

    public void testGetCommitsNullIssue() throws GenericEntityException, RepositoryException
    {
        try
        {
            CVSTabPanel = new CVSTabPanel(null, null);
            CVSTabPanel.getActions(null, user);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Issue cannot be null.", e.getMessage());
        }
    }

    public void testShowPanelValid()
    {
        _testShowPanel(true, true);
    }

    public void testShowPanelNoRepository()
    {
        _testShowPanel(true, false);
    }

    public void testShowPanelNoPermission()
    {
        _testShowPanel(false, true);
    }

    public void testShowPanelNoPermissionAndNoRepository()
    {
        _testShowPanel(false, false);
    }

    public void _testShowPanel(boolean hasPermission, boolean hasRepositories)
    {
        JiraTestUtil.loginUser(user);

        Mock mockRepositoryManager = new Mock(RepositoryManager.class);
        mockRepositoryManager.setStrict(true);
        mockRepositoryManager.expectAndReturn("getRepositoriesForProject", P.args(new IsEqual(project)), hasRepositories ? EasyList.build(new Object()) : Collections.EMPTY_LIST);

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.VIEW_VERSION_CONTROL)), new IsEqual(issueObject), new IsEqual(user)), Boolean.valueOf(hasPermission));

        CVSTabPanel vi = new CVSTabPanel((RepositoryManager) mockRepositoryManager.proxy(), (PermissionManager) mockPermissionManager.proxy());
        assertEquals(hasPermission && hasRepositories, vi.showPanel(issueObject, user));

        if (hasPermission) mockRepositoryManager.verify(); //repository is checked only if the permission is valid
        mockPermissionManager.verify();
    }
}
