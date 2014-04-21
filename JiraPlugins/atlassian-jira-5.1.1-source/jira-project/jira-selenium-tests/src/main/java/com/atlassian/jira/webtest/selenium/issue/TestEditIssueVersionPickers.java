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
 * Tests for AUI pickers (version, component etc.) in the edit issue, create issue, and bulk edit pages.
 *
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestEditIssueVersionPickers extends JiraSeleniumTest
{
    private static final String EDIT_PAGE_LOCATOR = Locators.JQUERY.addPrefix("form#issue-edit");
    private static final String EDIT_FORM_ISSUE_NAV_LOCATOR = Locators.JQUERY.addPrefix("#edit-issue-dialog form.aui");

    private static final int CUSTOM_FIELD_ID = 10020;

    private IssueNavigator issueNavigator;

    private GenericMultiSelect affectedVersionPicker;
    private GenericMultiSelect fixVersionPicker;
    private GenericMultiSelect customFieldVersionPicker;
    private QuickEditDialog quickEditDialog;

    public static Test suite()
    {
        return suiteFor(TestEditIssueVersionPickers.class);
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
        this.affectedVersionPicker = Pickers.newAffectedVersionPicker(EDIT_PAGE_LOCATOR, context());
        this.fixVersionPicker = Pickers.newFixVersionPicker(EDIT_PAGE_LOCATOR, context());
        this.customFieldVersionPicker = Pickers.newCustomFieldVersionPicker(EDIT_PAGE_LOCATOR, CUSTOM_FIELD_ID,
                context());
    }

    private List<GenericMultiSelect> allPickers()
    {
        return Arrays.asList(affectedVersionPicker, fixVersionPicker, customFieldVersionPicker);
    }

    private List<GenericMultiSelect> issueNavPickers()
    {
        return Arrays.asList(
                Pickers.newAffectedVersionPicker(EDIT_FORM_ISSUE_NAV_LOCATOR, context()),
                Pickers.newFixVersionPicker(EDIT_FORM_ISSUE_NAV_LOCATOR, context()),
                Pickers.newCustomFieldVersionPicker(EDIT_FORM_ISSUE_NAV_LOCATOR, CUSTOM_FIELD_ID, context()));
    }

    public void testVersionPickerInteractionsOnEditFromViewIssue()
    {
        getNavigator().editIssue(HSP_1);
        verifyPickersOnEditIssue(allPickers());
    }
    public void testVersionPickerTitle()
    {
        getNavigator().editIssue(HSP_1);
        fixVersionPicker.triggerSuggestionsByClick();
        fixVersionPicker.confirmInput();
        assertThat.attributeContainsValue("jquery=form#issue-edit div#fixVersions-multi-select ul.items li.item-row", "title", "New Version 1");
        assertThat.attributeDoesntContainValue("jquery=form#issue-edit div#fixVersions-multi-select ul.items li.item-row", "title", " New Version 1");
        assertThat.attributeDoesntContainValue("jquery=form#issue-edit div#fixVersions-multi-select ul.items li.item-row", "title", "New Version 1 ");


    }

    // JRADEV-2773. The SingleVersionPicker custom field should NOT be a Frother Control since it doesn't enforce
    // "only choose" one.
    public void testSingleVersionPickerIsntFrother() throws Exception
    {
        getNavigator().editIssue(HSP_1);
        assertThat.attributeContainsValue("//select[@id='customfield_10030']", "class", "select");
        assertThat.attributeDoesntContainValue("//select[@id='customfield_10030']", "class", "hidden");

        assertThat.attributeDoesntContainValue("//select[@id='customfield_10030']/..", "class", "aui-field-versionspicker");
    }

    public void testVersionPickerInteractionsOnEditFromIssueNav()
    {
        IssueNavResults searchResults = issueNavigator.goTo().findAll().results();
        searchResults.selectIssue(HSP_1).selectedIssue().executeFromCog(IssueNavResults.IssueNavAction.EDIT);

        quickEditDialog.waitUntilOpen();

        verifyPickersOnEditIssue(issueNavPickers());
    }

    // TODO in bulk edit issue?

    private void verifyPickersOnEditIssue(List<GenericMultiSelect> pickers)
    {
        for (GenericMultiSelect picker : pickers)
        {
            assertThat.elementPresentByTimeout(picker.locator());
            shouldNotShowUnknownVersionStringGivenNoVersionQuery(picker);
            shouldContainNoMatchesForNonExistingVersionQuery(picker);
            shouldMatchExistingVersionQuery(picker);
        }
    }
    
    private void shouldContainNoMatchesForNonExistingVersionQuery(final GenericMultiSelect picker)
    {
        picker.insertQuery("blah");
        picker.suggestions().assertOpen();
        picker.suggestions().assertNoMatches();
        picker.clearInputArea().suggestions().closeByEscape();
    }

    private void shouldMatchExistingVersionQuery(final GenericMultiSelect picker)
    {
        picker.insertQuery("1");
        picker.suggestions().assertOpen();
        picker.suggestions().assertContains("Unreleased Versions", "New Version 1");
        picker.suggestions().assertDoesNotContain("New Version 4");
        picker.suggestions().assertDoesNotContain("New Version 5");
        picker.clearInputArea().suggestions().closeByEscape();
    }

    private void shouldNotShowUnknownVersionStringGivenNoVersionQuery(final GenericMultiSelect picker)
    {
        picker.clearInputArea().triggerSuggestionsByArrowDown().assertOpen();
        picker.suggestions().assertContainsGroup("Unreleased Versions");
        assertHasNoUnknownSuggestion(picker);
    }

    private void assertHasNoUnknownSuggestion(final GenericMultiSelect picker)
    {
        picker.suggestions().assertOpen();
        assertThat.elementNotPresentByTimeout(picker.suggestionsLocator() + " li.unknown");
    }
}
