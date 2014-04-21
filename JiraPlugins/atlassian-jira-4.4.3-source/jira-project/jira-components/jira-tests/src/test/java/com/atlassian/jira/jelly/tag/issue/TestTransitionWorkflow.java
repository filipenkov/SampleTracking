package com.atlassian.jira.jelly.tag.issue;


import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.jelly.service.JellyServiceException;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.UpgradeTask;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build101;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build11;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build27;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build83;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import electric.xml.Document;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: amazkovoi
 * Date: 7/09/2004
 * Time: 11:51:59
 */
public class TestTransitionWorkflow extends AbstractJellyTestCase
{

    private User user;
    private User secondaryUser;
    private Group g;
    private GenericValue sr;

    public TestTransitionWorkflow(String s)
    {
        super(s);
    }


    protected void setUp() throws Exception
    {
        super.setUp();

        user = UtilsForTests.getTestUser("logged-in-user");
        secondaryUser = UtilsForTests.getTestUser("testuser");
        g = UtilsForTests.getTestGroup("jira-user");
        g.addUser(user);
        g.addUser(secondaryUser);
        JiraTestUtil.loginUser(user);
        ManagerFactory.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, null);

        SchemeManager permManager = ManagerFactory.getPermissionSchemeManager();
        GenericValue scheme = permManager.createDefaultScheme();
        PermissionManager pm = ManagerFactory.getPermissionManager();
        pm.addPermission(Permissions.CREATE_ISSUE, scheme, "jira-user", "group");
        pm.addPermission(Permissions.BROWSE, scheme, "jira-user", "group");
        pm.addPermission(Permissions.RESOLVE_ISSUE, scheme, "jira-user", "group");
        pm.addPermission(Permissions.ASSIGN_ISSUE, scheme, "jira-user", "group");
        pm.addPermission(Permissions.CLOSE_ISSUE, scheme, "jira-user", "group");
        pm.addPermission(Permissions.WORK_ISSUE, scheme, "jira-user", "group");
        pm.addPermission(Permissions.ASSIGNABLE_USER, scheme, "jira-user", "group");

        // Build IssueConstants
        UpgradeTask_Build11 upgradeTask_build11 = new UpgradeTask_Build11(ComponentAccessor.getConstantsManager());
        upgradeTask_build11.doUpgrade(false);

        // Build the default statuses
        UpgradeTask_Build27 upgradeTask_build27 = new UpgradeTask_Build27();
        upgradeTask_build27.doUpgrade(false);

        // Create field screens
        UpgradeTask upgradeTask = (UpgradeTask) JiraUtils.loadComponent(UpgradeTask_Build83.class);
        upgradeTask.doUpgrade(false);

        // Build default IssueTypeScheme
        UpgradeTask upgradeTask101 = (UpgradeTask) JiraUtils.loadComponent(UpgradeTask_Build101.class);
        upgradeTask101.doUpgrade(false);

        JiraAuthenticationContext authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        // Log in user
        authenticationContext.setLoggedInUser(user);
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "name", "A Project", "lead", user.getName(), "counter", new Long(1)));
        ComponentManager.getInstance().getIssueTypeScreenSchemeManager().associateWithDefaultScheme(project);
        ManagerFactory.getProjectManager().refresh();

        final PermissionSchemeManager permissionSchemeManager = ManagerFactory.getPermissionSchemeManager();
        permissionSchemeManager.addSchemeToProject(project, scheme);

        // Create a REAL issue (with valid transitions and everything) with key ABC-2
        final MutableIssue issue1 = ComponentAccessor.getIssueFactory().getIssue();
        issue1.setProject(project);
        issue1.setAssignee(user);
        GenericValue issueTypeGV = ComponentAccessor.getConstantsManager().getIssueType("1");
        issue1.setIssueType(issueTypeGV);
        ComponentAccessor.getIssueManager().createIssue(user, issue1);

        // Create a second REAL issue with key ABC-3
        final MutableIssue issue2 = ComponentAccessor.getIssueFactory().getIssue();
        issue2.setProject(project);
        issue2.setAssignee(secondaryUser);
        issue2.setIssueType(issueTypeGV);
        ComponentAccessor.getIssueManager().createIssue(secondaryUser, issue2);

        // Create a SearchRequest to return the two issues
        sr = UtilsForTests.getTestEntity("SearchRequest", EasyMap.build("id", new Long(10241), "name", "test", "author", "logged-in-user", "user", "logged-in-user", "group", "jira-user", "project", project.get("id"), "request", "project = "+project.get("id")));
    }

    protected void tearDown() throws Exception
    {
        user = null;
        secondaryUser = null;
        sr = null;

        ActionContext.getSession().clear();
        ComponentAccessor.getJiraAuthenticationContext().setLoggedInUser(null);
        PermissionManager pm = ManagerFactory.getPermissionManager();
        pm.removeGroupPermissions("jira-user");
        ImportUtils.setSubvertSecurityScheme(false);
        super.tearDown();
    }

    // Test that the tag handles the issue not existing
    public void testDoTagIssueDoesNotExist() throws Exception
    {
        final String scriptFilename = "transition-workflow.test.no-issue.jelly";

        try
        {
            Document document = runScript(scriptFilename);
            fail("JellyTagException should have been thrown.");
        }
        catch(JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf("Cannot retrieve issue with key 'ABC-1'") >= 0);
        }
    }

    // Test that the action does not exist
    public void testDoTagInvaldActionId() throws Exception
    {
        final String scriptFilename = "transition-workflow.test.invalid-action.jelly";

        try
        {
            Document document = runScript(scriptFilename);
            fail("JellyTagException should have been thrown.");
        }
        catch(JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf("Invalid action id '-7'.") >= 0);
        }
    }

    // Test that we found the transition but that it is not a valid transition for the issues current state.
    public void testDoTagInvaldActionNameBecauseOfIssueStatus() throws Exception
    {
        final String scriptFilename = "transition-workflow.test.invalid.action.name.for.state-action.jelly";

        try
        {
            Document document = runScript(scriptFilename);
            fail("JellyTagException should have been thrown.");
        }
        catch(JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf("Found workflow transition with name/id 'Reopen Issue' but that is not a valid workflow transition for the current state of issue 'ABC-2'") >= 0);
        }
    }

    // Test that we found the transition but that it is not a valid transition for the issues current state.
    public void testDoTagInvaldActionIdBecauseOfIssueStatus() throws Exception
    {
        final String scriptFilename = "transition-workflow.test.invalid.action.id.for.state-action.jelly";

        try
        {
            Document document = runScript(scriptFilename);
            fail("JellyTagException should have been thrown.");
        }
        catch(JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf("Found workflow transition with name/id '3' but that is not a valid workflow transition for the current state of issue 'ABC-2'") >= 0);
        }
    }

    // JRA-13412 - make sure it throws an exception if the assignee is present
    public void testDoTagInvaldActionBecauseOfMissingScreen() throws Exception
    {
        final String fileAssignee = "transition-workflow.test.invalid.action.missing-screen-assignee.jelly";
        final String fileResolution = "transition-workflow.test.invalid.action.missing-screen-resolution.jelly";
        final String fileOK = "transition-workflow.test.valid.action.missing-screen.jelly";

        try
        {
            runScript(fileAssignee);
            fail("JellyTagException should have been thrown.");
        }
        catch(JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf(" Field 'assignee' can not be set on action with no screen") >= 0);
        }
        try
        {
            runScript(fileResolution);
            fail("JellyTagException should have been thrown.");
        }
        catch(JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf(" Field 'resolution' can not be set on action with no screen") >= 0);
        }

        // OK it shoul work if we transition to a workflow with no screens when we have no extra fields
        try
        {
            Document doc = runScript(fileOK);
            assertNotNull(doc);
        }
        catch(JellyServiceException e)
        {
            fail("JellyTagException should NOT have been thrown.");
        }
    }

    // JRA-16112 and JRA-16915
    public void testDoTagValidActionNameNoResolutionSpecified() throws Exception
    {

        final String scriptFilename = "transition-workflow.test.missing.resolution.jelly";
        try
        {
            runScript(scriptFilename);
            fail("JellyTagException should have been thrown.");
        }
        catch(JellyServiceException e)
        {
            //expected: Resolution is required, it throws an error if not specified
        }
    }

    // This is the positive test that shows the tag actually works
    public void testDoTagValidActionName() throws Exception
    {

        final String scriptFilename = "transition-workflow.test.valid.action.name-action.jelly";

        try
        {
            runScript(scriptFilename);
        }
        catch(JellyServiceException e)
        {
            fail("JellyTagException should not have been thrown.");
        }

        MutableIssue issue1 = ComponentAccessor.getIssueManager().getIssueObject("ABC-2");
        assertEquals(user, issue1.getAssignee());
        assertAuthor(user, issue1);
    }

    //JRA-19763
    public void testDoTagNotLoggedInUser() throws Exception
    {
        final String scriptFilename = "transition-workflow.test.valid.action.name-action-differentuser.jelly";
        runScript(scriptFilename);

        MutableIssue issue1 = ComponentAccessor.getIssueManager().getIssueObject("ABC-2");
        assertEquals(user, issue1.getAssignee());
        assertAuthor(secondaryUser, issue1);
    }

    private void assertAuthor(User expectedUser, Issue issue)
    {
        List<ChangeHistory> changeHistory = ComponentAccessor.getChangeHistoryManager().getChangeHistoriesForUser(issue, expectedUser);
        assertNotNull(changeHistory);
        assertEquals(1, changeHistory.size());
        assertEquals(expectedUser.getName(), changeHistory.get(0).getUsername());
    }

    // JRA-14164: ensure that when processing multiple issues with the one tag, that the assignee is not overwritten accidentally
    public void testDoTagValidFilterId() throws Exception
    {
        final String scriptFilename = "transition-workflow.test.valid.filter.id-action.jelly";
        

        runScript(scriptFilename);

        MutableIssue issue1 = ComponentAccessor.getIssueManager().getIssueObject("ABC-2");
        MutableIssue issue2 = ComponentAccessor.getIssueManager().getIssueObject("ABC-3");

        assertEquals(user, issue1.getAssignee());
        assertEquals(secondaryUser, issue2.getAssignee());

    }

    protected String getRelativePath()
    {
        return "tag" + FS + "issue" + FS;
    }
}
