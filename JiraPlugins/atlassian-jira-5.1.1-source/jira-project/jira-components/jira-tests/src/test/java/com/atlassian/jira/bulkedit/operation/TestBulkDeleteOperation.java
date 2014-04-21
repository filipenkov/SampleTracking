package com.atlassian.jira.bulkedit.operation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanImpl;
import com.google.common.collect.Lists;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import junit.framework.TestCase;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.List;

public class TestBulkDeleteOperation extends TestCase
{
    User testUser;
    private MockComponentWorker mockComponentWorker;
    private MockIssueManager issueManager;
    final MockIssue issue1 = new MockIssue(1);
    final MockIssue issue2 = new MockIssue(2);
    final MockIssue issue3 = new MockIssue(3);

    public TestBulkDeleteOperation(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        mockComponentWorker = new MockComponentWorker();
        ComponentAccessor.initialiseWorker(mockComponentWorker);
        issueManager = new MockIssueManager();
        mockComponentWorker.registerMock(IssueManager.class, issueManager);

        issueManager.addIssue(issue1);
        issueManager.addIssue(issue2);
        issueManager.addIssue(issue3);
        testUser = new MockUser("testuser");
    }

    public void testCanPerformNoPermission() throws GenericEntityException
    {
        final BulkEditBean bulkEditBean = setupBulkEditBean();
        MockPermissionManager mockPermissionManager = new MockPermissionManager(false);

        mockComponentWorker.registerMock(PermissionManager.class, mockPermissionManager);

        final BulkDeleteOperation bulkDeleteOperation = new BulkDeleteOperation();
        final boolean result = bulkDeleteOperation.canPerform(bulkEditBean, testUser);
        assertFalse(result);
    }

    public void testCanPerform() throws GenericEntityException
    {
        final BulkEditBean bulkEditBean = setupBulkEditBean();

        MockPermissionManager mockPermissionManager = new MockPermissionManager(true);
        mockComponentWorker.registerMock(PermissionManager.class, mockPermissionManager);

        final BulkDeleteOperation bulkDeleteOperation = new BulkDeleteOperation();
        final boolean result = bulkDeleteOperation.canPerform(bulkEditBean, testUser);
        assertTrue(result);
    }

    public void testPerform() throws Exception
    {
        final BulkEditBean bulkEditBean = setupBulkEditBean();

        // Setup mock for IssueDelete backend action
        final BulkDeleteOperation bulkDeleteOperation = new BulkDeleteOperation();
        assertNotNull(issueManager.getIssue(1L));
        assertNotNull(issueManager.getIssue(2L));
        assertNotNull(issueManager.getIssue(3L));
        bulkDeleteOperation.perform(bulkEditBean, new MockUser("fred"));
        assertNull(issueManager.getIssue(1L));
        assertNull(issueManager.getIssue(2L));
        assertNotNull(issueManager.getIssue(3L));
    }

    public void testPerformNoIssue() throws Exception
    {
        // Test that bulk delete does not delete an issue that does not exist in the database
        // This is required as if a user is bulk deleteing issues and its sub-tasks the sub-tasks will be deleted when
        // the issue is deleted, by the time the bulk-delete gets around to deleting the sub-task, they have already been removed
        final BulkEditBeanImpl bulkEditBean = new BulkEditBeanImpl(issueManager);
        final List<Issue> issues = Lists.<Issue>newArrayList(issue1);
        bulkEditBean.initSelectedIssues(issues);

        final Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.setStrict(true);
        mockIssueManager.expectAndReturn("getIssueObject", P.args(new IsEqual(issue1.getId())), null);
        mockComponentWorker.registerMock(IssueManager.class, (IssueManager) mockIssueManager.proxy());

        final BulkDeleteOperation bulkDeleteOperation = new BulkDeleteOperation();
        bulkDeleteOperation.perform(bulkEditBean, testUser);
        mockIssueManager.verify();
    }

    private BulkEditBean setupBulkEditBean()
    {
        final BulkEditBeanImpl bulkEditBean = new BulkEditBeanImpl(issueManager);
        bulkEditBean.initSelectedIssues(Lists.<Issue>newArrayList(issue1, issue2));
        return bulkEditBean;
    }

}
