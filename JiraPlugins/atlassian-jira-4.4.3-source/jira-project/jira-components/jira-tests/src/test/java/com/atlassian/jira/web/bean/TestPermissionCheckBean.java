package com.atlassian.jira.web.bean;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.core.util.collection.EasyList;
import com.opensymphony.user.ProviderAccessor;
import com.opensymphony.user.User;

public class TestPermissionCheckBean extends ListeningTestCase
{

    private JiraAuthenticationContext nullUserAuthContext;
    private JiraAuthenticationContext privilegedAuthContext;
    private JiraAuthenticationContext unprivilegedAuthContext;
    private PermissionManager permissionManager;
    private User privilegedUser;
    private User unprivilegedUser;

    @Before
    public void setUp() throws Exception
    {
        final ProviderAccessor providerAccessorProxy = new MockProviderAccessor();
        privilegedUser = new User("test", providerAccessorProxy, new MockCrowdService());;
        unprivilegedUser = new User("nong", providerAccessorProxy, new MockCrowdService());;

        nullUserAuthContext = (JiraAuthenticationContext) DuckTypeProxy.getProxy(JiraAuthenticationContext.class, EasyList.build(new Object()), DuckTypeProxy.RETURN_NULL);

        Object duckAuthContext = new Object()
        {
            public User getUser()
            {
                return privilegedUser;
            }
        };
        privilegedAuthContext = (JiraAuthenticationContext) DuckTypeProxy.getProxy(JiraAuthenticationContext.class, EasyList.build(duckAuthContext), DuckTypeProxy.RETURN_NULL);

        duckAuthContext = new Object()
        {
            public User getUser()
            {
                return unprivilegedUser;
            }
        };
        unprivilegedAuthContext = (JiraAuthenticationContext) DuckTypeProxy.getProxy(JiraAuthenticationContext.class, EasyList.build(duckAuthContext), DuckTypeProxy.RETURN_NULL);

        Object duckPM = new Object()
        {
            public boolean hasPermission(int permissionsId, Issue entity, com.atlassian.crowd.embedded.api.User u)
            {
                return u == privilegedUser;
            }
            public boolean hasPermission(int permissionsId, Issue entity, User u)
            {
                return hasPermission(permissionsId, entity, (com.atlassian.crowd.embedded.api.User) u);
            }
        };
        permissionManager = (PermissionManager) DuckTypeProxy.getProxy(PermissionManager.class, EasyList.build(duckPM), DuckTypeProxy.RETURN_NULL);

    }

    @Test
    public void testBean()
    {
        Issue issue = getIssue(false);
        Issue subtaskIssue = getIssue(true);

        PermissionCheckBean permissionCheck;

        permissionCheck = new PermissionCheckBean(nullUserAuthContext, permissionManager);
        assertFalse(permissionCheck.isIssueVisible(issue));
        
         Object duckPM = new Object()
        {
            public boolean hasPermission(int permissionsId, Issue entity, com.atlassian.crowd.embedded.api.User u)
            {
                return u == null || u == privilegedUser;
            }
            public boolean hasPermission(int permissionsId, Issue entity, User u)
            {
                return hasPermission(permissionsId, entity, (com.atlassian.crowd.embedded.api.User) u);
            }
        };
        PermissionManager nullUserHasBrowsePermissionManager =  (PermissionManager) DuckTypeProxy.getProxy(PermissionManager.class, EasyList.build(duckPM), DuckTypeProxy.RETURN_NULL);
        permissionCheck = new PermissionCheckBean(nullUserAuthContext, nullUserHasBrowsePermissionManager);
        assertTrue(permissionCheck.isIssueVisible(issue));

        permissionCheck = new PermissionCheckBean(unprivilegedAuthContext, permissionManager);
        assertFalse(permissionCheck.isIssueVisible(issue));
        assertFalse(permissionCheck.isIssueVisible(subtaskIssue));
        assertFalse(permissionCheck.isIssueVisible(subtaskIssue.getParentObject()));

        permissionCheck = new PermissionCheckBean(privilegedAuthContext, permissionManager);
        assertTrue(permissionCheck.isIssueVisible(issue));
        assertTrue(permissionCheck.isIssueVisible(subtaskIssue));
        assertTrue(permissionCheck.isIssueVisible(subtaskIssue.getParentObject()));

        try
        {
            permissionCheck = new PermissionCheckBean(null, permissionManager);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {

        }
        try
        {
            permissionCheck = new PermissionCheckBean(nullUserAuthContext, null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {

        }

        try
        {
            permissionCheck = new PermissionCheckBean(nullUserAuthContext, permissionManager);
            permissionCheck.isIssueVisible(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {

        }

    }

    private Issue getIssue(boolean subtask)
    {
        if (subtask)
        {
            final Issue parentIssue = getIssue(false);
            Issue issue = new MockIssue()
            {

                public boolean isSubTask()
                {
                    return true;
                }

                public Issue getParentObject()
                {
                    return parentIssue;
                }
            };
            return issue;
        }
        else
        {
            return new MockIssue();
        }
    }
}
