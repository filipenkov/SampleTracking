package com.atlassian.jira.bulkedit.operation;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.action.ActionDispatcher;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.bean.BulkEditBean;

import com.atlassian.jira.web.bean.BulkEditBeanImpl;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;
import webwork.action.ActionSupport;
import webwork.dispatcher.ActionResult;

import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TestBulkDeleteOperation extends AbstractUsersTestCase
{
    private static final String ERROR_MESSAGE = "Test Error Message";
    User testUser;

    public TestBulkDeleteOperation(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        testUser = UtilsForTests.getTestUser("testuser");
    }

    public void testCanPerformNoPermission() throws GenericEntityException
    {
        final BulkEditBean bulkEditBean = setupBulkEditBean();
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.DELETE_ISSUE)), new IsAnything(),
            new IsEqual(testUser)), Boolean.FALSE);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        final BulkDeleteOperation bulkDeleteOperation = new BulkDeleteOperation();
        final boolean result = bulkDeleteOperation.canPerform(bulkEditBean, testUser);
        mockPermissionManager.verify();
        assertFalse(result);
    }

    public void testCanPerform() throws GenericEntityException
    {
        final BulkEditBean bulkEditBean = setupBulkEditBean();
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.DELETE_ISSUE)), new IsAnything(),
            new IsEqual(testUser)), Boolean.TRUE);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        final BulkDeleteOperation bulkDeleteOperation = new BulkDeleteOperation();
        final boolean result = bulkDeleteOperation.canPerform(bulkEditBean, testUser);
        mockPermissionManager.verify();
        assertTrue(result);
    }

    public void testPerform() throws Exception
    {
        final BulkEditBean bulkEditBean = setupBulkEditBean();

        // Setup mock for IssueDelete backend action
        final Mock mockActionDispatcher = new Mock(ActionDispatcher.class);

        mockActionDispatcher.expectAndReturn("execute", P.args(new IsEqual(ActionNames.ISSUE_DELETE), new IsAnything()), new ActionResult(
            Action.SUCCESS, new Object(), Collections.EMPTY_LIST, null));
        mockActionDispatcher.setStrict(true);
        CoreFactory.setActionDispatcher((ActionDispatcher) mockActionDispatcher.proxy());

        final BulkDeleteOperation bulkDeleteOperation = new BulkDeleteOperation();
        bulkDeleteOperation.perform(bulkEditBean, testUser);
        mockActionDispatcher.verify();
    }

    public void testPerformNoIssue() throws Exception
    {
        // Test that bulk delete does not delete an issue that does not exist in the database
        // This is required as if a user is bulk deleteing issues and its sub-tasks the sub-tasks will be deleted when
        // the issue is deleted, by the time the bulk-delete gets around to deleting the sub-task, they have already been removed
        final BulkEditBeanImpl bulkEditBean = new BulkEditBeanImpl(null);
        final GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test issue 1"));
        final List issues = EasyList.build(issue);
        bulkEditBean._setSelectedIssueGVsForTesting(issues);

        // Setup mock for IssueDelete backend action
        final Mock mockActionDispatcher = new Mock(ActionDispatcher.class);
        mockActionDispatcher.setStrict(true);
        CoreFactory.setActionDispatcher((ActionDispatcher) mockActionDispatcher.proxy());

        final Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.setStrict(true);
        mockIssueManager.expectAndReturn("getIssue", P.args(new IsEqual(issue.getLong("id"))), null);
        ManagerFactory.addService(IssueManager.class, (IssueManager) mockIssueManager.proxy());

        final BulkDeleteOperation bulkDeleteOperation = new BulkDeleteOperation();
        bulkDeleteOperation.perform(bulkEditBean, testUser);
        mockIssueManager.verify();
        mockActionDispatcher.verify();
    }

    public void testPerformWithError() throws Exception
    {
        final BulkEditBean bulkEditBean = setupBulkEditBean();

        // Setup mock for IssueDelete backend action
        final Mock mockActionDispatcher = new Mock(ActionDispatcher.class);
        final MockAction mockAction = new MockAction();
        final ActionResult actionResult = new ActionResult(Action.ERROR, new Object(), EasyList.build(mockAction), null);
        mockActionDispatcher.expectAndReturn("execute", P.args(new IsEqual(ActionNames.ISSUE_DELETE), new IsAnything()), actionResult);
        mockActionDispatcher.setStrict(true);
        CoreFactory.setActionDispatcher((ActionDispatcher) mockActionDispatcher.proxy());

        final BulkDeleteOperation bulkDeleteOperation = new BulkDeleteOperation();
        try
        {
            bulkDeleteOperation.perform(bulkEditBean, testUser);
            fail("AtlassianCoreException should have been thrown.");
        }
        catch (final AtlassianCoreException e)
        {
            assertFalse(e.getMessage().indexOf(ERROR_MESSAGE) == -1);
        }
        mockActionDispatcher.verify();
    }

    private BulkEditBean setupBulkEditBean()
    {
        final BulkEditBeanImpl bulkEditBean = new BulkEditBeanImpl(null);
        final List issues = EasyList.build(UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test issue 1")),
            UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test issue 2")), UtilsForTests.getTestEntity("Issue", EasyMap.build(
                "summary", "test issue 3")));
        bulkEditBean._setSelectedIssueGVsForTesting(issues);
        return bulkEditBean;
    }

    class MockAction extends ActionSupport
    {
        @Override
        public boolean getHasErrorMessages()
        {
            return true;
        }

        @Override
        public Collection getErrorMessages()
        {
            return EasyList.build(ERROR_MESSAGE);
        }
    }
}
