package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.components.fields.AssigneeField;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.dialogs.quickedit.EditIssueDialog;
import com.atlassian.jira.pageobjects.elements.GlobalMessage;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.navigator.IssueNavigatorResults;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.SearchClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.SearchRequest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.SearchResult;
import com.atlassian.pageobjects.elements.PageElementFinder;
import junit.framework.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.Map;

import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.ASSIGNEE;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.COMMENT;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.COMPONENTS;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.ENVIRONMENT;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.FIX_VERSIONS;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.LABELS;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.PRIORITY;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.SUMMARY;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.TIMETRACKING;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.WORKLOG;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Tests for editing an issue using quick edit
 *
 * @since v5.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@Restore ("xml/TestQuickEditIssue.xml")
public class TestEditIssue extends BaseJiraWebTest
{
    public static final String CUSTOMFIELD = "customfield_10000";

    @Inject
    private PageElementFinder finder;

    public static final String UNASSIGNED_ASSIGNEE = "Unassigned";

    @Test
    public void testTogglingModesRetainsValues()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        viewIssuePage.execKeyboardShortcut("e");

        EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class)
                .switchToFullMode()
                .fill(SUMMARY, "test");

        editIssueDialog = editIssueDialog.switchToCustomMode();
        assertEquals("test", editIssueDialog.getFieldValue(SUMMARY));
        editIssueDialog.fill(COMMENT, "test another switch");
        editIssueDialog = editIssueDialog.switchToFullMode();
        assertEquals("test another switch", editIssueDialog.getFieldValue(COMMENT));
        assertEquals("test", editIssueDialog.getFieldValue(SUMMARY));

    }

    @Test
    public void testCustomFieldJavaScriptExecutes()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        viewIssuePage.execKeyboardShortcut("e");

        pageBinder.bind(EditIssueDialog.class).switchToFullMode();
        Assert.assertTrue(finder.find(By.id(CUSTOMFIELD)).hasClass("custom-field-js-applied"));
    }

    @Test
    public void testCustomFieldIsDisplayedAndJavascriptExecutesWhenChecked()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        viewIssuePage.execKeyboardShortcut("e");

        EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class)
                .switchToCustomMode();
        editIssueDialog.addFields(CUSTOMFIELD);
        Assert.assertTrue(finder.find(By.id(CUSTOMFIELD)).hasClass("custom-field-js-applied"));
    }

    @Test
    public void testEditingSimpleIssueInCustomMode()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1").execKeyboardShortcut("e");

        EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class);

        editIssueDialog.switchToCustomMode().addFields(SUMMARY);
        editIssueDialog.fill(SUMMARY, "Scott's Issue");

        ViewIssuePage issuePage = editIssueDialog.submitExpectingViewIssue("HSP-1");
        assertEquals("Scott's Issue", issuePage.getSummary());
    }

    @Test
    public void testEditingSimpleIssueInFullMode()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1").execKeyboardShortcut("e");
        EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class);

        editIssueDialog.switchToFullMode();
        editIssueDialog.fill(SUMMARY, "Scott's Issue");

        ViewIssuePage issuePage = editIssueDialog.submitExpectingViewIssue("HSP-1");
        assertEquals("Scott's Issue", issuePage.getSummary());
    }

    @Test
    public void testValidationErrorsInCustomMode()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1").execKeyboardShortcut("e");
        EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class);
        editIssueDialog.switchToCustomMode().addFields(SUMMARY);
        editIssueDialog.fill(SUMMARY, "");
        final Map<String, String> formErrors = editIssueDialog.submit(EditIssueDialog.class).waitForFormErrors().getFormErrors();
        assertTrue("Expected inline errror for summary as it is a required field and has no value", formErrors.containsKey(SUMMARY));
    }

    @Test
    public void testValidationErrorsInFullMode()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1").execKeyboardShortcut("e");
        EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class);
        editIssueDialog.switchToFullMode();
        editIssueDialog.fill(SUMMARY, "");
        final Map<String, String> formErrors = editIssueDialog.submit(EditIssueDialog.class).waitForFormErrors().getFormErrors();
        assertTrue("Expected inline errror for summary as it is a required field and has no value", formErrors.containsKey(SUMMARY));
    }

    @Test
    public void testAddingAndRemovingFields()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1").execKeyboardShortcut("e");
        EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class);
        editIssueDialog.switchToCustomMode();

        editIssueDialog.removeFields(PRIORITY, COMPONENTS, FIX_VERSIONS, LABELS, COMMENT).addFields(SUMMARY, ENVIRONMENT);
        editIssueDialog.close();
        jira.goToViewIssue("HSP-2").execKeyboardShortcut("e");
        editIssueDialog = pageBinder.bind(EditIssueDialog.class);
        assertEquals(editIssueDialog.getVisibleFields(), asList(SUMMARY, ASSIGNEE, ENVIRONMENT));
    }

    @Test
    public void testEditOnIssueNavigator()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.visit(AdvancedSearch.class).enterQuery("").submit();
        IssueNavigatorResults issueNavigatorResults = pageBinder.bind(IssueNavigatorResults.class);
        EditIssueDialog editIssueDialog = issueNavigatorResults.focus().getSelectedIssue().editViaShortcut();
        assertTrue("Expected Edit Issue Dialog for [MKY-1]", editIssueDialog.getTitle().contains("MKY-1"));
        editIssueDialog.close();
        editIssueDialog = issueNavigatorResults.nextIssue().getSelectedIssue().editViaShortcut();
        assertTrue("Expected Edit Issue Dialog for [HSP-5]", editIssueDialog.getTitle().contains("HSP-5"));
    }

    @Test
    @Restore("xml/TestLogWorkInlineErrors.xml")
    public void testLogworkInlineErrors()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1").execKeyboardShortcut("e");
        EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class)
                .submit(EditIssueDialog.class);

        // Test that the time tracking fields are added to the dialog and errors displayed beneath them
        assertEquals(asList("You must indicate the time spent working.", "Original Estimate is required."),
                editIssueDialog.getFormErrorList());
    }

    @Test
    public void testEditAssignee()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        backdoor.generalConfiguration().allowUnassignedIssues();

        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.fill("summary", "Issue created from testEditAssignee");
        final String assignee = pageBinder.bind(AssigneeField.class).getAssignee();
        assertEquals("Automatic", assignee);

        createIssueDialog.submit(GlobalMessage.class);

        final String issueKey = getNewIssueKey();

        ViewIssuePage viewIssuePage = jira.goToViewIssue(issueKey);
        assertEquals("Administrator", viewIssuePage.getPeopleSection().getAssignee());

        viewIssuePage.execKeyboardShortcut("e");
        EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class).setAssignee(UNASSIGNED_ASSIGNEE);

        viewIssuePage = editIssueDialog.submitExpectingViewIssue(issueKey);
        assertEquals(UNASSIGNED_ASSIGNEE, viewIssuePage.getPeopleSection().getAssignee());

        viewIssuePage.execKeyboardShortcut("e");
        viewIssuePage = pageBinder.bind(EditIssueDialog.class).submitExpectingViewIssue(issueKey);
        assertEquals(UNASSIGNED_ASSIGNEE, viewIssuePage.getPeopleSection().getAssignee());
    }

    @Test
    @Restore("xml/TestLogWorkInlineErrors.xml")
    public void testEditOriginalEstimate()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1").execKeyboardShortcut("e");
        final EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class).switchToCustomMode()
                .addFields(TIMETRACKING, WORKLOG)
                .setOriginalEstimate("5d").setTimeSpent("1h");
        editIssueDialog.submit(ViewIssuePage.class, "HSP-1"); // this will fail if there are some problems with validation
    }

    private String getNewIssueKey()
    {
        final SearchClient searchClient = new SearchClient(jira.environmentData());
        final SearchResult result = searchClient.postSearch(new SearchRequest().jql("summary ~ \"\\\"Issue created from testEditAssignee\\\"\" "));
        return result.issues.get(0).key;
    }

}

