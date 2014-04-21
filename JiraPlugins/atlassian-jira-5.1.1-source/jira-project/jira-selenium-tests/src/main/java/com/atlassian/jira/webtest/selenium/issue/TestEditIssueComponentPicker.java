package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.components.GenericMultiSelect;
import com.atlassian.jira.webtest.selenium.framework.components.IssueNavResults;
import com.atlassian.jira.webtest.selenium.framework.components.Pickers;
import com.atlassian.jira.webtest.selenium.framework.dialogs.QuickEditDialog;
import com.atlassian.jira.webtest.selenium.framework.model.Locators;
import com.atlassian.jira.webtest.selenium.framework.pages.IssueNavigator;
import junit.framework.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for AUI component picker (version, component etc.) in the edit issue, create issue, and bulk edit pages.
 *
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestEditIssueComponentPicker extends JiraSeleniumTest
{
    private IssueNavigator issueNavigator;

    private GenericMultiSelect componentPicker;
    private QuickEditDialog quickEditDialog;


    public static Test suite()
    {
        return suiteFor(TestEditIssueComponentPicker.class);
    }

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        initPages();
        initPickers();
        restoreData("TestEditIssueVersion.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    private void initPages()
    {
        this.issueNavigator = new IssueNavigator(context());
        this.quickEditDialog = new QuickEditDialog(context());
    }

    private void initPickers()
    {
        this.componentPicker = Pickers.newComponentPicker(Locators.JQUERY.addPrefix("form#issue-edit"), context());
    }

    private List<GenericMultiSelect> allPickers()
    {
        return Arrays.asList(componentPicker);
    }

    public void testPickersInteractionsOnEditFromViewIssue()
    {
        getNavigator().editIssue(HSP_1);
        verifyPickersOnEditIssue(Pickers.newComponentPicker(Locators.JQUERY.addPrefix("form#issue-edit"), context()));
    }


    public void testPickersOnEditFromIssueNav()
    {
        IssueNavResults searchResults = issueNavigator.goTo().findAll().results();
        searchResults.selectIssue(HSP_1).selectedIssue().executeFromCog(IssueNavResults.IssueNavAction.EDIT);
        quickEditDialog.waitUntilOpen();
        verifyPickersOnEditIssue(Pickers.newComponentPicker(Locators.JQUERY.addPrefix("#edit-issue-dialog form.aui"), context()));
    }

    // TODO in bulk edit issue?!?

    private void verifyPickersOnEditIssue(GenericMultiSelect picker)
    {
        assertThat.elementPresentByTimeout(picker.locator());
        shouldNotShowUnknownVersionStringGivenNoQuery(picker);
        shouldContainNoMatchesForNonExistingQuery(picker);
        shouldMatchExistingQuery(picker);
    }
    
    private void shouldContainNoMatchesForNonExistingQuery(GenericMultiSelect picker)
    {
        picker.insertQuery("blah").suggestions().assertOpen();
        picker.suggestions().assertNoMatches();
        picker.clearInputArea().suggestions().closeByEscape();
    }

    private void shouldMatchExistingQuery(GenericMultiSelect picker)
    {
        picker.insertQuery("1");
        picker.suggestions().assertOpen();
        picker.suggestions().assertContains("New Component 1");
        picker.suggestions().assertDoesNotContain("New Component 2");
        picker.suggestions().assertDoesNotContain("New Component 3");
        picker.clearInputArea().suggestions().closeByEscape();
    }

    private void shouldNotShowUnknownVersionStringGivenNoQuery(GenericMultiSelect picker)
    {
        picker.clearInputArea().triggerSuggestionsByArrowDown().assertOpen();
        picker.suggestions().assertContains("New Component 1");
        picker.suggestions().assertContains("New Component 2");
        picker.suggestions().assertContains("New Component 3");
        assertHasNoUnknownSuggestion(picker);
    }

    private void assertHasNoUnknownSuggestion(GenericMultiSelect picker)
    {
        picker.suggestions().assertOpen();
        assertThat.elementNotPresentByTimeout(picker.suggestionsLocator() + " li.unknown");
    }
}
