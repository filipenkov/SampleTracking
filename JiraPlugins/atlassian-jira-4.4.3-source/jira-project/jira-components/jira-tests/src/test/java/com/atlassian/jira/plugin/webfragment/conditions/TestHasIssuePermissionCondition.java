package com.atlassian.jira.plugin.webfragment.conditions;

import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.jira.local.ListeningTestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
import org.easymock.IMocksControl;

import java.util.HashMap;

public class TestHasIssuePermissionCondition extends ListeningTestCase
{
    @Test
    public void testNoPermissionExists()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final PermissionManager permissionManager = mocksControl.createMock(PermissionManager.class);

        final HasIssuePermissionCondition condition = new HasIssuePermissionCondition(permissionManager);

        final HashMap initParams = new HashMap();

        mocksControl.replay();
        try
        {
            condition.init(initParams);
            fail("Should have failed with permission specified");
        }
        catch (PluginParseException e)
        {
            // good case
        }
        mocksControl.verify();


    }

    @Test
    public void testIncorrectPermission()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final PermissionManager permissionManager = mocksControl.createMock(PermissionManager.class);

        final HasIssuePermissionCondition condition = new HasIssuePermissionCondition(permissionManager);

        final HashMap initParams = new HashMap();

        initParams.put("permission", "Be Crazy");
        mocksControl.replay();
        try
        {
            condition.init(initParams);
            fail("Should have failed with permission specified");
        }
        catch (PluginParseException e)
        {
            // good case
        }
        mocksControl.verify();
    }

    @Test
    public void testNoIssue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final HashMap<String, Object> params = new HashMap<String, Object>();
        final JiraHelper jiraHelper = new JiraHelper(null, null, params);

        final PermissionManager permissionManager = mocksControl.createMock(PermissionManager.class);

        final HasIssuePermissionCondition condition = new HasIssuePermissionCondition(permissionManager);

        final HashMap initParams = new HashMap();

        initParams.put("permission", "create");
        condition.init(initParams);

        mocksControl.replay();

        assertFalse(condition.shouldDisplay(null, jiraHelper));

        mocksControl.verify();

    }
    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final HashMap<String, Object> params = new HashMap<String, Object>();
         params.put("issue", issue);

         final JiraHelper jiraHelper = new JiraHelper(null, null, params);

        final PermissionManager permissionManager = mocksControl.createMock(PermissionManager.class);

        final HasIssuePermissionCondition condition = new HasIssuePermissionCondition(permissionManager);

        final HashMap initParams = new HashMap();

        initParams.put("permission", "create");
        condition.init(initParams);

        expect(permissionManager.hasPermission(11, issue, null)).andReturn(true);

        mocksControl.replay();

        assertTrue(condition.shouldDisplay(null, jiraHelper));

        mocksControl.verify();

    }

    @Test
    public void testFalse()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final HashMap<String, Object> params = new HashMap<String, Object>();
         params.put("issue", issue);

         final JiraHelper jiraHelper = new JiraHelper(null, null, params);

        final PermissionManager permissionManager = mocksControl.createMock(PermissionManager.class);

        final HasIssuePermissionCondition condition = new HasIssuePermissionCondition(permissionManager);

        final HashMap initParams = new HashMap();

        initParams.put("permission", "browse");
        condition.init(initParams);

        expect(permissionManager.hasPermission(10, issue, null)).andReturn(false);

        mocksControl.replay();

        assertFalse(condition.shouldDisplay(null, jiraHelper));

        mocksControl.verify();

    }
}
