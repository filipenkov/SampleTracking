/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.text.SimpleDateFormat;
import java.util.Date;

@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestCustomWorkflow extends JIRAWebTest
{
    public TestCustomWorkflow(String name)
    {
        super(name);
    }

    /**
     * Test that the workflow conditions use the unmodified issue. This is because:
     * To figure out what workflow operations are available we call to OSWorkflow passing it an issue.
     * When the workflow action is executed, we gather the information from the user for issue update and then
     * execute the transition. When the transition is executed OSWorkflow checks teh conditions again. However, the
     * issue has been modified by this stage. If the modified issue causes a workflow condition to fail, the user gets
     * an error.
     * We need to ensure that we do the condition checks against an unmodified issue.
     */
    public void testConditionsUseUnmodifiedIssue()
    {
        restoreBlankInstance();

        addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        addUserToGroup(BOB_USERNAME, Groups.DEVELOPERS);

        // Copy default JIRA workflow
        copyWorkFlow("jira", WORKFLOW_COPIED, "Workflow copied from JIRA default");
        assertTextPresent(WORKFLOW_COPIED);
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yy");
        assertTextPresent("Last modified on " + format.format(new Date()) + " by " + ADMIN_FULLNAME + ".");

        editTransitionScreen(WORKFLOW_COPIED, TRANSIION_NAME_START_PROGRESS, ASSIGN_FIELD_SCREEN_NAME);

        addWorkflowPostfunction(WORKFLOW_COPIED, STATUS_IN_PROGRESS, "Stop Progress", "com.atlassian.jira.plugin.system.workflow:assigntolead-function");

        enableWorkflow();

        // Create an issue for testing
        String key = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "Test Issue");

        clickLinkWithText(TRANSIION_NAME_START_PROGRESS);

        selectOption(FIELD_ASSIGNEE, BOB_FULLNAME);

        submit();

        // Ensure we are on the View Issue Screen
        assertNotNull(getDialog().getResponse().getURL());
        assertTrue(getDialog().getResponse().getURL().getPath().endsWith("browse/" + key));

        // Ensure the status got set
        text.assertTextPresent(new IdLocator(tester, "status-val"), STATUS_IN_PROGRESS);
        // Ensure the issue was assigned correctly
        text.assertTextPresent(new IdLocator(tester, "assignee-val"), BOB_FULLNAME);

        // Login as bob
        logout();
        login(BOB_USERNAME, BOB_PASSWORD);

        // Navigate to the issue
        gotoIssue(key);

        // Now stop progress on the issue
        clickLinkWithText(TRANSIION_NAME_STOP_PROGRESS);

        // Ensure the status got set
        text.assertTextPresent(new IdLocator(tester, "status-val"), STATUS_OPEN);
        // Ensure the issue was assigned correctly
        text.assertTextPresent(new IdLocator(tester, "assignee-val"), ADMIN_FULLNAME);

        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testLastModifiedByAnonymousUser()
    {
        restoreBlankInstance();

        grantGlobalPermission(GLOBAL_ADMIN, "Anyone");

        logout();

        // Copy default JIRA workflow
        copyWorkFlow("jira", WORKFLOW_COPIED, "Workflow copied from JIRA default");
        assertTextPresent(WORKFLOW_COPIED);
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yy");
        assertTextPresent("Last modified on " + format.format(new Date()) + " by an anonymous user.");
    }

    public void testLastModifiedWithFunnyCharacters()
    {
        restoreBlankInstance();

        addUser("</meta>user", "meta", "</meta> user lastname", "meta@example.com");
        addUserToGroup("</meta>user", "jira-administrators");

        logout();

        login("</meta>user", "meta");

        // Copy default JIRA workflow
        copyWorkFlow("jira", WORKFLOW_COPIED, "Workflow copied from JIRA default");
        assertTextPresent(WORKFLOW_COPIED);
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yy");
        assertTextPresent("Last modified on " + format.format(new Date()) + " by &lt;/meta&gt; user lastname.");
    }

    private void enableWorkflow()
    {
        // Associate the project with the new workflow
        addWorkFlowScheme(WORKFLOW_SCHEME, "Test workflow scheme.");
        assignWorkflowScheme(WORKFLOW_SCHEME, "Bug", WORKFLOW_COPIED);
        associateWorkFlowSchemeToProject(PROJECT_HOMOSAP, WORKFLOW_SCHEME);
        waitForSuccessfulWorkflowSchemeMigration(PROJECT_HOMOSAP, WORKFLOW_SCHEME);
    }
}
