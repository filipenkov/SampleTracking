package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.bulkedit.operation.BulkDeleteOperation;
import com.atlassian.jira.bulkedit.operation.BulkOperation;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.setup.AbstractSetupAction;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanImpl;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.Action;
import webwork.action.ActionContext;

import java.util.List;

public class TestBulkDelete extends AbstractUsersTestCase
{
    private User testUser;

    public TestBulkDelete(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        testUser = createMockUser("TestBulkDelete");
        JiraTestUtil.loginUser(testUser);
    }

    public void testDoPerformValidationCannotPerform()
    {
        // Setup BulkEditBean
        BulkEditBean bulkEditBean = setupBulkEditBean();

        // Setup mock for Permissions Manager
        Mock permissionManagerMock = new Mock(PermissionManager.class);
        permissionManagerMock.setStrict(true);
        permissionManagerMock.expectAndReturn("hasPermission", P.args(new IsEqual(Permissions.BULK_CHANGE), new IsEqual(testUser)), Boolean.TRUE);

        // Create mock for the BulkDelete operation
        Mock bulkDeleteOperation = new Mock(BulkOperation.class);
        bulkDeleteOperation.setStrict(true);
        bulkDeleteOperation.expectAndReturn("canPerform", P.args(new IsEqual(bulkEditBean), new IsEqual(testUser)), Boolean.FALSE);

        // Setup mock for BulkOperationsManager
        Mock bulkOperationsManager = new Mock(BulkOperationManager.class);
        bulkOperationsManager.expectAndReturn("getOperation", P.args(new IsEqual(BulkDeleteOperation.NAME_KEY)), bulkDeleteOperation.proxy());

        BulkDelete bulkDelete = new BulkDelete(null, (BulkOperationManager) bulkOperationsManager.proxy(), (PermissionManager) permissionManagerMock.proxy());


        bulkDelete.doPerformValidation();
        checkSingleElementCollection(bulkDelete.getErrorMessages(), "You do not have permission to delete the selected 3 issues.");
        bulkOperationsManager.verify();
        permissionManagerMock.verify();
        bulkDeleteOperation.verify();
    }

    public void testDoPerformValidationException()
    {
        // Setup BulkEditBean
        BulkEditBean bulkEditBean = setupBulkEditBean();

        // Setup mock for Permissions Manager
        Mock permissionManagerMock = new Mock(PermissionManager.class);
        permissionManagerMock.setStrict(true);
        permissionManagerMock.expectAndReturn("hasPermission", P.args(new IsEqual(Permissions.BULK_CHANGE), new IsEqual(testUser)), Boolean.TRUE);

        // Create mock for the BulkDelete operation
        Mock bulkDeleteOperation = new Mock(BulkOperation.class);
        bulkDeleteOperation.setStrict(true);
        bulkDeleteOperation.expectAndThrow("canPerform", P.args(new IsEqual(bulkEditBean), new IsEqual(testUser)), new Exception());

        // Setup mock for BulkOperationsManager
        Mock bulkOperationsManager = new Mock(BulkOperationManager.class);
        bulkOperationsManager.expectAndReturn("getOperation", P.args(new IsEqual(BulkDeleteOperation.NAME_KEY)), bulkDeleteOperation.proxy());

        BulkDelete bulkDelete = new BulkDelete(null, (BulkOperationManager) bulkOperationsManager.proxy(), (PermissionManager) permissionManagerMock.proxy());

        bulkDelete.doPerformValidation();
        checkSingleElementCollection(bulkDelete.getErrorMessages(), "Error while validating input. Please refer to the log for more details.");
        bulkOperationsManager.verify();
        permissionManagerMock.verify();
        bulkDeleteOperation.verify();
    }

    public void testDoPerformValidation()
    {
        // Setup BulkEditBean
        BulkEditBean bulkEditBean = setupBulkEditBean();

        // Setup mock for Permissions Manager
        Mock permissionManagerMock = new Mock(PermissionManager.class);
        permissionManagerMock.setStrict(true);
        permissionManagerMock.expectAndReturn("hasPermission", P.args(new IsEqual(Permissions.BULK_CHANGE), new IsEqual(testUser)), Boolean.TRUE);

        // Create mock for the BulkDelete operation
        Mock bulkDeleteOperation = new Mock(BulkOperation.class);
        bulkDeleteOperation.setStrict(true);
        bulkDeleteOperation.expectAndReturn("canPerform", P.args(new IsEqual(bulkEditBean), new IsEqual(testUser)), Boolean.TRUE);

        // Setup mock for BulkOperationsManager
        Mock bulkOperationsManager = new Mock(BulkOperationManager.class);
        bulkOperationsManager.expectAndReturn("getOperation", P.args(new IsEqual(BulkDeleteOperation.NAME_KEY)), bulkDeleteOperation.proxy());

        BulkDelete bulkDelete = new BulkDelete(null, (BulkOperationManager) bulkOperationsManager.proxy(), (PermissionManager) permissionManagerMock.proxy());

        bulkDelete.doPerformValidation();
        assertTrue(bulkDelete.getErrorMessages().isEmpty());
        assertTrue(bulkDelete.getErrors().isEmpty());
        bulkOperationsManager.verify();
        permissionManagerMock.verify();
        bulkDeleteOperation.verify();
    }

    public void testDoDetails() throws Exception
    {
        BulkEditBean bulkEditBean = setupBulkEditBean();
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("BulkDeleteDetailsValidation.jspa");

        BulkDelete bulkDelete = new BulkDelete(null, (BulkOperationManager) new Mock(BulkOperationManager.class).proxy(), null);

        Group group = createMockGroup(AbstractSetupAction.DEFAULT_GROUP_ADMINS);
        addUserToGroup(testUser, group);
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, group.getName());

        bulkDelete.doDetails();
        response.verify();
    }

    public void testDoDetailsValidation() throws Exception
    {
        BulkEditBean bulkEditBean = setupBulkEditBean();

        BulkDelete bulkDelete = new BulkDelete(null, (BulkOperationManager) new Mock(BulkOperationManager.class).proxy(), null);

        String result = bulkDelete.doDetailsValidation();
        assertEquals(Action.INPUT, result);
        assertEquals(4, bulkEditBean.getCurrentStep());
        assertTrue(bulkEditBean.isAvailablePreviousStep(2));
    }

    public void testDoPerformError() throws Exception
    {
        // Setup BulkEditBean
        BulkEditBean bulkEditBean = setupBulkEditBean();

        // Setup mock for Permissions Manager
        Mock permissionManagerMock = new Mock(PermissionManager.class);
        permissionManagerMock.setStrict(true);
        permissionManagerMock.expectAndReturn("hasPermission", P.args(new IsEqual(Permissions.BULK_CHANGE), new IsEqual(testUser)), Boolean.TRUE);

        // Create mock for the BulkDelete operation
        Mock bulkDeleteOperation = new Mock(BulkOperation.class);
        bulkDeleteOperation.setStrict(true);
        bulkDeleteOperation.expectAndReturn("canPerform", P.args(new IsEqual(bulkEditBean), new IsEqual(testUser)), Boolean.FALSE);

        // Setup mock for BulkOperationsManager
        Mock bulkOperationsManager = new Mock(BulkOperationManager.class);
        bulkOperationsManager.expectAndReturn("getOperation", P.args(new IsEqual(BulkDeleteOperation.NAME_KEY)), bulkDeleteOperation.proxy());

        BulkDelete bulkDelete = new BulkDelete(null, (BulkOperationManager) bulkOperationsManager.proxy(), (PermissionManager) permissionManagerMock.proxy());

        String result = bulkDelete.doPerform();
        assertEquals(Action.ERROR, result);
        bulkOperationsManager.verify();
        bulkDeleteOperation.verify();
        permissionManagerMock.verify();
    }

    public void testDoPerformErrorNoBulkChangePermission() throws Exception
    {
        // Setup BulkEditBean
        BulkEditBean bulkEditBean = setupBulkEditBean();

        // Setup mock for Permissions Manager
        Mock permissionManagerMock = new Mock(PermissionManager.class);
        permissionManagerMock.setStrict(true);
        permissionManagerMock.expectAndReturn("hasPermission", P.args(new IsEqual(Permissions.BULK_CHANGE), new IsEqual(testUser)), Boolean.FALSE);

        // Create mock for the BulkDelete operation
        Mock bulkDeleteOperation = new Mock(BulkOperation.class);
        bulkDeleteOperation.setStrict(true);
        bulkDeleteOperation.expectAndReturn("canPerform", P.args(new IsEqual(bulkEditBean), new IsEqual(testUser)), Boolean.TRUE);

        // Setup mock for BulkOperationsManager
        Mock bulkOperationsManager = new Mock(BulkOperationManager.class);
        bulkOperationsManager.expectAndReturn("getOperation", P.args(new IsEqual(BulkDeleteOperation.NAME_KEY)), bulkDeleteOperation.proxy());

        BulkDelete bulkDelete = new BulkDelete(null, (BulkOperationManager) bulkOperationsManager.proxy(), (PermissionManager) permissionManagerMock.proxy());

        String result = bulkDelete.doPerform();
        assertEquals(Action.ERROR, result);
        bulkOperationsManager.verify();
        bulkDeleteOperation.verify();
        permissionManagerMock.verify();
    }

    public void testDoPerformException() throws Exception
    {
        // Setup BulkEditBean
        BulkEditBean bulkEditBean = setupBulkEditBean();

        // Setup mock for Permissions Manager
        Mock permissionManagerMock = new Mock(PermissionManager.class);
        permissionManagerMock.setStrict(true);
        permissionManagerMock.expectAndReturn("hasPermission", P.args(new IsEqual(Permissions.BULK_CHANGE), new IsEqual(testUser)), Boolean.TRUE);

        // Create mock for the BulkDelete operation
        Mock bulkDeleteOperation = new Mock(BulkOperation.class);
        bulkDeleteOperation.setStrict(true);
        bulkDeleteOperation.expectAndReturn("canPerform", P.args(new IsEqual(bulkEditBean), new IsEqual(testUser)), Boolean.TRUE);
        bulkDeleteOperation.expectAndThrow("perform", P.args(new IsEqual(bulkEditBean), new IsEqual(testUser)), new Exception());

        // Setup mock for BulkOperationsManager
        Mock bulkOperationsManager = new Mock(BulkOperationManager.class);
        bulkOperationsManager.expectAndReturn("getOperation", P.args(new IsEqual(BulkDeleteOperation.NAME_KEY)), bulkDeleteOperation.proxy());

        BulkDelete bulkDelete = new BulkDelete(null, (BulkOperationManager) bulkOperationsManager.proxy(), (PermissionManager) permissionManagerMock.proxy());

        String result = bulkDelete.doPerform();
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(bulkDelete.getErrorMessages(), "Error while deleting selected issues. Please refer to the log for more details.");
        bulkOperationsManager.verify();
        bulkDeleteOperation.verify();
        permissionManagerMock.verify();
    }

    public void testDoPerform() throws Exception
    {
        // Setup BulkEditBean
        BulkEditBean bulkEditBean = setupBulkEditBean();

        Mock permissionManagerMock = new Mock(PermissionManager.class);
        permissionManagerMock.setStrict(true);
        permissionManagerMock.expectAndReturn("hasPermission", P.args(new IsEqual(Permissions.BULK_CHANGE), new IsEqual(testUser)), Boolean.TRUE);

        // Create mock for the BulkDelete operation
        Mock bulkDeleteOperation = new Mock(BulkOperation.class);
        bulkDeleteOperation.setStrict(true);
        bulkDeleteOperation.expectAndReturn("canPerform", P.args(new IsEqual(bulkEditBean), new IsEqual(testUser)), Boolean.TRUE);
        bulkDeleteOperation.expectVoid("perform", P.args(new IsEqual(bulkEditBean), new IsEqual(testUser)));

        // Setup mock for BulkOperationsManager
        Mock bulkOperationsManager = new Mock(BulkOperationManager.class);
        bulkOperationsManager.expectAndReturn("getOperation", P.args(new IsEqual(BulkDeleteOperation.NAME_KEY)), bulkDeleteOperation.proxy());

        BulkDelete bulkDelete = new BulkDelete(null, (BulkOperationManager) bulkOperationsManager.proxy(), (PermissionManager) permissionManagerMock.proxy());

        // Setup response
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("/secure/IssueNavigator.jspa");
        String result = bulkDelete.doPerform();
        assertEquals(Action.NONE, result);

        /// Ensure no errors
        assertTrue(bulkDelete.getErrorMessages().isEmpty());
        assertTrue(bulkDelete.getErrors().isEmpty());

        // Ensure the BulkEditBean has been removed from session
        assertFalse(ActionContext.getSession().containsKey(SessionKeys.BULKEDITBEAN));
        bulkOperationsManager.verify();
        bulkDeleteOperation.verify();
        permissionManagerMock.verify();
        response.verify();
    }

    public void testDoDetailsValidationNoBean() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("SessionTimeoutMessage.jspa");

        BulkDelete bulkDelete = new BulkDelete(null, (BulkOperationManager) new Mock(BulkOperationManager.class).proxy(), null);

        String result = bulkDelete.doDetailsValidation();
        assertEquals(Action.NONE, result);
        response.verify();
    }

    private BulkEditBean setupBulkEditBean()
    {
        BulkEditBeanImpl bulkEditBean = new BulkEditBeanImpl(null);
        List issues = EasyList.build(UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test issue 1", "project", 101L, "type", "Bug")),
                UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test issue 2", "project", 101L, "type", "Bug")),
                UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test issue 3", "project", 101L, "type", "Bug")));
        UtilsForTests.getTestEntity("Project", EasyMap.build("id", (101L)));

        bulkEditBean._setSelectedIssueGVsForTesting(issues);
        ActionContext.getSession().put(SessionKeys.BULKEDITBEAN, bulkEditBean);

        return bulkEditBean;
    }

    protected void tearDown() throws Exception
    {
        ActionContext.getSession().remove(SessionKeys.BULKEDITBEAN);
        JiraTestUtil.resetRequestAndResponse();
        super.tearDown();
    }
}
