package com.atlassian.jira.webtest.selenium.issue.dialogs;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.model.admin.Screen;
import com.atlassian.jira.webtest.framework.page.AdministrationPage;
import com.atlassian.jira.webtest.framework.page.admin.ConfigureScreen;
import com.atlassian.jira.webtest.framework.page.admin.ViewScreens;
import com.atlassian.jira.webtest.selenium.framework.components.GenericMultiSelect;
import com.atlassian.jira.webtest.selenium.framework.components.IssueNavResults;
import com.atlassian.jira.webtest.selenium.framework.components.LabelsPicker;
import com.atlassian.jira.webtest.selenium.framework.components.Pickers;
import com.atlassian.jira.webtest.selenium.framework.dialogs.WorkflowTransitionDialog;
import com.atlassian.jira.webtest.selenium.framework.model.CancelType;
import com.atlassian.jira.webtest.selenium.framework.model.SystemField;
import com.atlassian.jira.webtest.selenium.framework.model.WorkflowTransition;
import com.atlassian.jira.webtest.selenium.framework.pages.IssueNavigator;
import junit.framework.Test;

/**
 * Test workflow transitions of issues using dialogs.
 *
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestWorkflowTransition extends AbstractIssueDialogTest
{
    public static Test suite()
    {
        return suiteFor(TestWorkflowTransition.class);
    }

    private AdministrationPage administration;
    private IssueNavigator issueNavigator;

    private WorkflowTransitionDialog resolveIssueDialog;
    private WorkflowTransitionDialog reopenIssueDialog;

    // TODO this should be really moved into the dialog, but the dialog should just contain contents of a
    // TODO'ResolveIssuePage object, which does not exist yet. Ergo this test must be further refactored as we get
    // TODO actual contents into dialogs.
    private LabelsPicker labelsPickerOnResolveIssue;
    private GenericMultiSelect affectedVersionPickerOnResolveIssue;
    private GenericMultiSelect componentPickerOnResolveIssue;



    @Override
    public void onSetUp()
    {
        super.onSetUp();
        initComponents();
        restoreData("TestWorkflowTransitions.xml");
    }

    private void initComponents()
    {
        this.administration = globalPages().administration();
        this.issueNavigator = new IssueNavigator(context());
        this.resolveIssueDialog = new WorkflowTransitionDialog(context(), WorkflowTransition.RESOLVE);
        this.reopenIssueDialog = new WorkflowTransitionDialog(context(), WorkflowTransition.REOPEN);
        this.labelsPickerOnResolveIssue = LabelsPicker.newSystemLabelsPicker(resolveIssueDialog.locator(), context());
        this.affectedVersionPickerOnResolveIssue = Pickers.newAffectedVersionPicker(resolveIssueDialog.locator(), context());
        this.componentPickerOnResolveIssue = Pickers.newComponentPicker(resolveIssueDialog.locator(), context());
    }

    public void testRedirectOnTransitionViewIssue()
    {
        //test Start progress & stop progress don't show a dialog but simply reload the page
        getNavigator().gotoIssue("HSP-1");

        assertThat.textPresent("This is my first test issue!");
        assertThat.linkPresentWithText("Start Progress");
        assertThat.linkNotPresentWithText("Stop Progress");
        client.click("action_id_4", true);
        assertDialogNotLoaded();
        assertThat.textPresent("This is my first test issue!");
        assertThat.linkNotPresentWithText("Start Progress");
        assertThat.linkPresentWithText("Stop Progress");

        resolveIssueDialog.openFromViewIssue();
//        Timeout.waitFor(20).seconds();
        assertThat.elementPresentByTimeout("jquery=#workflow-transition-5-dialog:contains('Resolve Issue')", DROP_DOWN_WAIT);
        assertThat.elementContainsText("issue-workflow-transition", "Resolving an issue indicates that the developers are satisfied the issue is finished.");
        assertThat.attributeContainsValue("issue-workflow-transition-submit", "value", "Resolve");
        resolveIssueDialog.cancel(CancelType.BY_CLICK);

        assertThat.linkPresentWithText("Resolve Issue");
        assertThat.linkNotPresentWithText("Reopen Issue");

        resolveIssueDialog.openFromViewIssue();
        resolveIssueDialog.submit();

        //make sure we're back on the issue page and that the workflow transition links have changed
        assertThat.elementContainsText("issue_header_summary", "This is my first test issue!");
        assertThat.linkPresentWithText("Reopen Issue");
        assertThat.linkNotPresentWithText("Resolve Issue");

        reopenIssueDialog.openFromViewIssue();
        client.select(reopenIssueDialog.inDialog("#assignee"), "label=Fred Normal");
        reopenIssueDialog.submit();

        //make sure we're back on the issue page and that the workflow transition links have changed and that the issue is assigned to Fred
        assertThat.elementContainsText("assignee-val", "Fred");
        assertThat.linkPresentWithText("Resolve Issue");
        assertThat.linkNotPresentWithText("Reopen Issue");

        //reopen the workflow dialog and assign it back to the administrator by clicking the assign-to-me-trigger link
        resolveIssueDialog.openFromViewIssue();
        client.click(resolveIssueDialog.inDialog("#assign-to-me-trigger"));
        resolveIssueDialog.submit();
        assertThat.elementContainsText("assignee-val", "Administrator");
    }

    public void testRedirectOnTransitionIssueNav()
    {
        //test Start progress & stop progress don't show a dialog but simply reload the page
        getNavigator().findAllIssues();
        getNavigator().gotoFindIssues();

        assertThat.textPresent("This is my first test issue!");
        client.click("actions_10000");
        assertThat.elementPresentByTimeout("actions_10000_drop", DROP_DOWN_WAIT);
        assertThat.linkPresentWithText("Start Progress");
        assertThat.linkNotPresentWithText("Stop Progress");
        client.click("jquery=#actions_10000_drop a[rel='4']", true);
        assertThat.elementPresentByTimeout("affectedIssueMsg");
        assertThat.elementContainsText("affectedIssueMsg", "HSP-1 has been updated.");
        assertThat.elementContainsText("jquery=h1.item-summary", "Issue Navigator");
        assertDialogNotLoaded();
        assertThat.textPresent("This is my first test issue!");
        client.click("actions_10000");
        assertThat.elementPresentByTimeout("actions_10000_drop", DROP_DOWN_WAIT);
        assertThat.linkNotPresentWithText("Start Progress");
        assertThat.linkPresentWithText("Stop Progress");

        resolveIssueDialog.openFromIssueNav(10000);
//        Timeout.waitFor(20).seconds();
        assertThat.elementPresentByTimeout("jquery=#workflow-transition-5-dialog:contains('Resolve Issue')", DROP_DOWN_WAIT);
        assertThat.elementContainsText("issue-workflow-transition", "Resolving an issue indicates that the developers are satisfied the issue is finished.");
        assertThat.attributeContainsValue("issue-workflow-transition-submit", "value", "Resolve");
        resolveIssueDialog.cancel(CancelType.BY_CLICK);
        assertThat.elementContainsText("jquery=h1.item-summary", "Issue Navigator");

        resolveIssueDialog.openFromIssueNav(10000);
        resolveIssueDialog.submit();
        assertThat.elementPresentByTimeout("affectedIssueMsg");
        assertThat.elementContainsText("affectedIssueMsg", "HSP-1 has been updated.");
        assertThat.elementContainsText("jquery=h1.item-summary", "Issue Navigator");


        reopenIssueDialog.openFromIssueNav(10000);
        client.select(reopenIssueDialog.inDialog("#assignee"), "label=Fred Normal");
        reopenIssueDialog.submit();

        //make sure we're back on the issue page and that the workflow transition links have changed and that the issue is assigned to Fred
        assertThat.elementContainsText("assignee_fred", "Fred");
        client.click("actions_10000");
        assertThat.elementPresentByTimeout("actions_10000_drop", DROP_DOWN_WAIT);
        assertThat.linkPresentWithText("Resolve Issue");
        assertThat.linkNotPresentWithText("Reopen Issue");

    }

    public void testWorklogOnResolveScreen()
    {
        getAdministration().activateTimeTracking();
        ConfigureScreen confScreen = administration.goTo().goToPage(ViewScreens.class).goToConfigureScreen(Screen.RESOLVE_ISSUE);
        confScreen.addFieldSection().selectFields().select(SystemField.LOG_WORK.option());
        confScreen.addFieldSection().submitAdd();

        //now open the resolve dialog on an issue and check the log work fields are being displayed!
        getNavigator().gotoIssue("HSP-1");
        resolveIssueDialog.openFromViewIssue();
        assertThat.elementContainsText(resolveIssueDialog.inDialog("#issue-workflow-transition"), "Time Spent");
        assertThat.elementContainsText(resolveIssueDialog.inDialog("#issue-workflow-transition"), "Date Started");
        assertThat.elementContainsText(resolveIssueDialog.inDialog("#issue-workflow-transition"), "Remaining Estimate");
        assertThat.elementContainsText(resolveIssueDialog.inDialog("#issue-workflow-transition"), "Comment");

        //now lets log 4h of work and resolve the issue!
        client.type(resolveIssueDialog.inDialog("#log-work-time-logged"), "4h");
        client.type(resolveIssueDialog.inDialog("#issue-workflow-transition textarea#comment"), "This is my first test comment");
        resolveIssueDialog.submit();

        //check that the issue is resolved and work was logged against it!
        assertThat.elementContainsText("issue_header_summary", "This is my first test issue!");
        assertThat.elementContainsText("action_id_3", "Reopen Issue");
        assertThat.linkNotPresentWithText("Resolve Issue");

        client.click("worklog-tabpanel", true);
        
        assertThat.elementContainsText("id=worklog_details_10000", "4 hours");
        assertThat.elementContainsText("id=worklog_details_10000", "This is my first test comment");
    }

    public void testWorkflowTransitionFromIssueNavContainsPickers()
    {
        ViewScreens viewFieldScreensAdminPage = administration.goTo().goToPage(ViewScreens.class);
        ConfigureScreen confScreen = viewFieldScreensAdminPage.goToConfigureScreen(Screen.RESOLVE_ISSUE);
        confScreen.addFieldSection().selectFields().select(SystemField.LABELS.option(),
                SystemField.AFFECTED_VERSIONS.option(),
                SystemField.COMPONENTS.option());
        confScreen.addFieldSection().submitAdd();
        administration.backToJira();
        issueNavigator.goTo().findAll().results().selectedIssue().executeFromCog(IssueNavResults.IssueNavAction.RESOLVE);
        resolveIssueDialog.assertReady();
        labelsPickerOnResolveIssue.assertReady(500);
        affectedVersionPickerOnResolveIssue.assertReady(500);
        componentPickerOnResolveIssue.assertReady(500);
    }
}
