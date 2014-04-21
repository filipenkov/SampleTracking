package com.atlassian.jira.upgrade;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.config.util.TestAbstractJiraHome;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build27;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build56;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.jira.workflow.WorkflowActionsBean;

import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.User;
import com.opensymphony.workflow.basic.BasicWorkflow;
import com.opensymphony.workflow.spi.Step;

import java.util.List;

public class TestUpgradeTask_Build56 extends AbstractUsersIndexingTestCase
{
    private UpgradeTask_Build56 upgradeTask_build56;
    private GenericValue project;
    private User u;
    private GenericValue issueType;

    public TestUpgradeTask_Build56(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        upgradeTask_build56 = new UpgradeTask_Build56(new ApplicationPropertiesImpl(
                new ApplicationPropertiesStore(PropertiesManager.getInstance(), new TestAbstractJiraHome.FixedHome())),
                ManagerFactory.getIndexManager(), new DefaultOfBizDelegator(
            CoreFactory.getGenericDelegator()));

        u = UtilsForTests.getTestUser("Issue User");

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "counter", new Long(1)));
        issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Test Type"));

        final Mock permissionManager = new Mock(PermissionManager.class);
        permissionManager.setStrict(true);
        permissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.CREATE_ISSUE)), new IsAnything(),
            new IsEqual(u)), Boolean.TRUE);

        ManagerFactory.addService(PermissionManager.class, (PermissionManager) permissionManager.proxy());
    }

    public void testGetBuildNumber()
    {
        assertEquals("56", upgradeTask_build56.getBuildNumber());
    }

    public void testGetShortDescription()
    {
        assertEquals("Rename unassigned to open, move assigned issues to open and remove assigned status", upgradeTask_build56.getShortDescription());
    }

    public void testDoUpgrade() throws Exception
    {
        // Create statuses with upgrade task 27
        final UpgradeTask_Build27 upgradeTask_build27 = new UpgradeTask_Build27();
        upgradeTask_build27.doUpgrade(false);

        _testUpgradeWorked();
    }

    public void testDoUpgradeWithNonDefaultWorkflow1() throws Exception
    {
        // Create statuses with upgrade task 27
        final UpgradeTask_Build27 upgradeTask_build27 = new UpgradeTask_Build27();
        upgradeTask_build27.doUpgrade(false);

        final GenericValue unassigned = ManagerFactory.getConstantsManager().getStatus("1");
        unassigned.set("name", "Owen");
        unassigned.store();

        try
        {
            upgradeTask_build56.doUpgrade(false);
            fail("Exception should have been thrown");
        }
        catch (final Exception expectedException)
        {}
    }

    public void testDoUpgradeWithNonDefaultWorkflow2() throws Exception
    {
        // Create statuses with upgrade task 27
        final UpgradeTask_Build27 upgradeTask_build27 = new UpgradeTask_Build27();
        upgradeTask_build27.doUpgrade(false);

        final GenericValue unassigned = ManagerFactory.getConstantsManager().getStatus("2");
        unassigned.set("name", "Owen");
        unassigned.store();

        try
        {
            upgradeTask_build56.doUpgrade(false);
            fail("Exception should have been thrown");
        }
        catch (final Exception e)
        {
            assertEquals(
                "Could not upgrade due to non default statuses ('Unassigned' instead of 'Unassigned'; 'Owen' instead of 'Assigned'). If these are logically equivalent you may force the upgrade by restarting JIRA with -Djira.upgrade.build56.override=true. Otherwise please contact " + ExternalLinkUtilImpl.getInstance().getProperty(
                    "external.link.jira.support.mail.to") + " for more information.", e.getMessage());
        }
    }

    public void testDoUpgradeWithNonDefaultWorkflowOverride() throws Exception
    {
        // Create statuses with upgrade task 27
        final UpgradeTask_Build27 upgradeTask_build27 = new UpgradeTask_Build27();
        upgradeTask_build27.doUpgrade(false);

        final GenericValue unassigned = ManagerFactory.getConstantsManager().getStatus("2");
        unassigned.set("name", "Owen");
        unassigned.store();

        //Set override system property
        System.setProperty("jira.upgrade.build56.override", "true");

        try
        {
            _testUpgradeWorked();
        }
        catch (final Exception e)
        {
            fail("Exception should NOT have been thrown");
        }
    }

    private void _testUpgradeWorked() throws Exception
    {
        // Create some issues that need to be updated.
        final GenericValue issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build(IssueFieldConstants.SUMMARY, "Issue 1",
            IssueFieldConstants.STATUS, "1"));
        final GenericValue issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build(IssueFieldConstants.SUMMARY, "Issue 2",
            IssueFieldConstants.STATUS, "2"));
        final GenericValue issue3 = UtilsForTests.getTestEntity("Issue", EasyMap.build(IssueFieldConstants.SUMMARY, "Issue 3",
            IssueFieldConstants.STATUS, "3"));
        final GenericValue issue4 = UtilsForTests.getTestEntity("Issue", EasyMap.build(IssueFieldConstants.SUMMARY, "Issue 4",
            IssueFieldConstants.STATUS, "4"));
        final GenericValue issue5 = UtilsForTests.getTestEntity("Issue", EasyMap.build(IssueFieldConstants.SUMMARY, "Issue 5",
            IssueFieldConstants.STATUS, "5"));
        final GenericValue issue6 = UtilsForTests.getTestEntity("Issue", EasyMap.build(IssueFieldConstants.SUMMARY, "Issue 6",
            IssueFieldConstants.STATUS, "6"));

        upgradeTask_build56.doUpgrade(false);

        // Retrieve the issues and make sure they have or have not been updated
        final IssueManager issueManager = ManagerFactory.getIssueManager();
        GenericValue issue = issueManager.getIssue(issue1.getLong("id"));
        assertEquals("1", issue.getString("status"));
        issue = issueManager.getIssue(issue2.getLong("id"));
        assertEquals("1", issue.getString("status"));
        issue = issueManager.getIssue(issue3.getLong("id"));
        assertEquals("3", issue.getString("status"));
        issue = issueManager.getIssue(issue4.getLong("id"));
        assertEquals("4", issue.getString("status"));
        issue = issueManager.getIssue(issue5.getLong("id"));
        assertEquals("5", issue.getString("status"));
        issue = issueManager.getIssue(issue6.getLong("id"));
        assertEquals("6", issue.getString("status"));

        // Retrieve unassigned status and check it has new name, description and iconurl and that status has been removed
        final GenericValue unassigned = ManagerFactory.getConstantsManager().getStatus("1");
        assertEquals("Open", unassigned.getString("name"));
        assertEquals("The issue is open and ready for the assignee to start work on it.", unassigned.getString("description"));
        assertEquals("/images/icons/status_open.gif", unassigned.getString("iconurl"));

        final GenericValue assigned = ManagerFactory.getConstantsManager().getStatus("2");
        assertNull(assigned);
    }

    public void testUpgradedWorkedOnWorkflow() throws Exception
    {
        // Create statuses with upgrade task 27
        final UpgradeTask_Build27 upgradeTask_build27 = new UpgradeTask_Build27();
        upgradeTask_build27.doUpgrade(false);

        // Completely mock out the permission manager (i.e. do not care about which permission is passed)
        // This is so as workflow (due to its auto-firing ability for actions, which was introduced in version 2.6) will check
        // for available actions once the issue is created. Hence it will call permission manager a few times with various permissions.
        final Mock permissionManager = new Mock(PermissionManager.class);
        permissionManager.setStrict(true);
        permissionManager.expectAndReturn("hasPermission", P.args(new IsAnything(), new IsAnything(), new IsEqual(u)), Boolean.TRUE);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) permissionManager.proxy());

        final IssueManager issueManager = ManagerFactory.getIssueManager();

        UtilsForTests.getTestEntity("FieldScreen", EasyMap.build("id", WorkflowActionsBean.VIEW_COMMENTASSIGN_ID, "name", "Test Screen 1"));
        UtilsForTests.getTestEntity("FieldScreen", EasyMap.build("id", WorkflowActionsBean.VIEW_RESOLVE_ID, "name", "Test Screen 2"));
        (ComponentManager.getComponentInstanceOfType(FieldScreenManager.class)).refresh();

        MutableIssue issueObject = IssueImpl.getIssueObject(null);
        issueObject.setProject(project);
        issueObject.setIssueType(issueType);
        issueObject.setSummary("Issue 1");
        final GenericValue issue1 = issueManager.createIssue(u, EasyMap.build("issue", issueObject));

        issue1.set(IssueFieldConstants.STATUS, "2");
        issue1.store();

        GenericValue workflowEntry = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("OSWorkflowEntry",
            EasyMap.build("id", issue1.getLong("workflowId"))));
        GenericValue currentStep = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("OSCurrentStep",
            EasyMap.build("entryId", workflowEntry.getLong("id"))));
        currentStep.set("stepId", new Integer(2));
        currentStep.store();

        issueObject = IssueImpl.getIssueObject(null);
        issueObject.setProject(project);
        issueObject.setIssueType(issueType);
        issueObject.setSummary("Issue 2");

        final GenericValue issue2 = issueManager.createIssue(u, EasyMap.build("issue", issueObject));
        issue2.set(IssueFieldConstants.STATUS, "3");
        issue2.store();
        workflowEntry = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("OSWorkflowEntry",
            EasyMap.build("id", issue2.getLong("workflowId"))));
        currentStep = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("OSCurrentStep",
            EasyMap.build("entryId", workflowEntry.getLong("id"))));
        currentStep.set("stepId", new Integer(3));
        currentStep.store();

        upgradeTask_build56.doUpgrade(false);

        GenericValue issue = issueManager.getIssue(issue1.getLong("id"));
        assertEquals("1", issue.getString("status"));

        BasicWorkflow basicWorkflow = (BasicWorkflow) ComponentAccessor.getWorkflowManager().makeWorkflow(u.getName());
        List currentSteps = basicWorkflow.getCurrentSteps(issue.getLong("id").longValue());
        assertEquals(1, currentSteps.size());

        Step currentWorkflowStep = (Step) currentSteps.get(0);
        assertEquals(1, currentWorkflowStep.getStepId());

        issue = issueManager.getIssue(issue2.getLong("id"));
        assertEquals("3", issue.getString("status"));
        basicWorkflow = (BasicWorkflow) ComponentAccessor.getWorkflowManager().makeWorkflow(u.getName());
        currentSteps = basicWorkflow.getCurrentSteps(issue.getLong("id").longValue());
        assertEquals(1, currentSteps.size());
        currentWorkflowStep = (Step) currentSteps.get(0);
        assertEquals(3, currentWorkflowStep.getStepId());
    }
}
