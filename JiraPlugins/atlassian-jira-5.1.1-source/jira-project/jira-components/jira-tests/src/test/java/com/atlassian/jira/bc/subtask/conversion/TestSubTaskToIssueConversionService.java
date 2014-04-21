package com.atlassian.jira.bc.subtask.conversion;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.AbstractPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.SimpleErrorCollection;

public class TestSubTaskToIssueConversionService extends LegacyJiraMockTestCase
{
    private final Issue subTask = new MockIssue()
    {
        public boolean isSubTask()
        {
            return true;
        }
    };

    private final Issue normalIssue = new MockIssue();

    private final PermissionManager positivePermMgr = new MockPermissionManager(true);

    private final PermissionManager negativePermMgr = new MockPermissionManager(false);

    private User testUser;
    JiraServiceContext context;

    protected void setUp() throws Exception
    {
        super.setUp();
        testUser = createMockUser("TestSubTaskToIssueConversionService");
        context = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testCanConvertIssueWithPermission()
    {
        SubTaskToIssueConversionService convService = getService(positivePermMgr);
        assertFalse(convService.canConvertIssue(context, normalIssue));
    }

    public void testCanConvertIssueWithoutPermission()
    {
        SubTaskToIssueConversionService convService = getService(negativePermMgr);
        assertFalse(convService.canConvertIssue(context, normalIssue));
    }

    public void testCanConvertSubTaskWithPermission()
    {
        SubTaskToIssueConversionService convService = getService(positivePermMgr);
        assertTrue(convService.canConvertIssue(context, subTask));
    }

    public void testCanConvertSubTaskWithoutPermission()
    {
        SubTaskToIssueConversionService convService = getService(negativePermMgr);
        assertFalse(convService.canConvertIssue(context, subTask));
    }

    public void testCanConvertNullIssue()
    {
        SubTaskToIssueConversionService convService = getService(negativePermMgr);
        try
        {
            convService.canConvertIssue(null, null);
            fail("Null Issue is not allowed");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            convService.canConvertIssue(context, null);
            fail("Null Issue is not allowed");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    private SubTaskToIssueConversionService getService(PermissionManager permManager)
    {
        return new DefaultSubTaskToIssueConversionService(permManager, null, null, null, null, null, null, null)
        {

            protected String getText(String key)
            {
                return key;
            }

            protected String getText(String key, Object param)
            {
               return key + param;
            }

            protected String getText(String key, String param0, String param1)
            {
                return key+ param0 + param1;
            }
        };
    }

    private class MockPermissionManager extends AbstractPermissionManager
    {
        private final boolean hasPermission;

        public MockPermissionManager(boolean hasPermission)
        {
            this.hasPermission = hasPermission;
        }

        public boolean hasPermission(int permissionsId, Issue issue, com.atlassian.crowd.embedded.api.User u)
        {
            return hasPermission;
        }
    }

}
