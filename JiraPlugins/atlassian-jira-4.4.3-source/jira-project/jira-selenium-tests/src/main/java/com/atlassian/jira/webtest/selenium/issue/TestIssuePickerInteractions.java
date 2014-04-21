package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.components.IssuePicker;
import com.atlassian.jira.webtest.selenium.framework.dialogs.LinkIssueDialog;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import com.atlassian.webtest.ui.keys.SpecialKeys;
import junit.framework.Test;

/**
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //JS Error - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestIssuePickerInteractions extends JiraSeleniumTest
{
    private static final String TEST_XML = "TestIssuePickerInteractions.xml";

    private static final String HSP_PREFIX = "HSP-";
    private static final String HSP_1 = "HSP-1";
    private static final String HSP_2 = "HSP-2";
    private static final String HSP_3 = "HSP-3";
    private static final String HSP_4 = "HSP-4";
    private static final String ISSUE_WITH_T_PRECEDED_BY_PARENTHESIS = HSP_4;
    private static final String ISSUE_WITH_T_IN_SKIPPED_KEYWORDS = HSP_3;
    private static final String MK_1 = "MK-1";


    public static Test suite()
    {
        return suiteFor(TestIssuePickerInteractions.class);
    }

    private LinkIssueDialog linkIssueDialog;

    public void onSetUp()
    {
        super.onSetUp();
        restoreData(TEST_XML);
        this.linkIssueDialog = new LinkIssueDialog(context());
    }

    @Override
    protected void onTearDown() throws Exception
    {
        this.linkIssueDialog = null;
        super.onTearDown();
    }

    public void testInteractions()
    {
        getNavigator().gotoIssue(HSP_1);
        openLinkIssueDialog();
        _testSuggestionsAppear();
        _testSuggestionsCloseWithEsc();
        _testRoofAndFloorWrapping();
    }

    public void testNumericFiltering()
    {
        getNavigator().gotoIssue(HSP_1);
        openLinkIssueDialog();
        linkIssueDialog.issuePicker().insertQuery("1");
        linkIssueDialog.issuePicker().suggestions().assertHistorySearchSuggestionSelected(MK_1);
        linkIssueDialog.issuePicker().confirmInput().assertElementPicked(MK_1);
    }

    private void _testSuggestionsCloseWithEsc()
    {
        linkIssueDialog.issuePicker().suggestions().closeByEscape();
        linkIssueDialog.issuePicker().suggestions().assertClosed();
    }

    private void _testSuggestionsAppear()
    {
        linkIssueDialog.issuePicker().insertQuery("H");
        linkIssueDialog.issuePicker().suggestions().assertHistorySearchContains(HSP_2);
    }

    private void _testRoofAndFloorWrapping()
    {
        IssuePicker picker = linkIssueDialog.issuePicker();
        picker.assertReady(1000);
        picker.clearInputArea().insertQuery(HSP_PREFIX);
        picker.suggestions().assertHistorySearchSuggestionSelected(HSP_PREFIX);
        picker.suggestions().down();
        picker.suggestions().assertHistorySearchSuggestionSelected(HSP_PREFIX);
        picker.suggestions().down();
        picker.suggestions().assertHistorySearchSuggestionSelected(HSP_PREFIX);
        picker.suggestions().down();
        picker.suggestions().assertUserInputSelected("Enter issue key");
        picker.suggestions().down();
        picker.suggestions().assertHistorySearchSuggestionSelected(HSP_PREFIX);
    }

    public void testNonExistantLabelWithLowerCaseKey()
    {
        getNavigator().gotoIssue(HSP_1);
        openLinkIssueDialog();
        linkIssueDialog.issuePicker().insertQuery("nick-1").confirmInput();
        linkIssueDialog.issuePicker().assertElementPicked("NICK-1");
        // TODO move to dialog/picker
        client.click("issue-link-submit");
        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog .error:contains(NICK-1)", DROP_DOWN_WAIT);
    }

    public void testMultiIssueInput()
    {
        getNavigator().gotoIssue(HSP_1);
        openLinkIssueDialog();

        linkIssueDialog.issuePicker().insertQuery("HSP-2 mk-1").awayFromInputArea();
        linkIssueDialog.issuePicker().assertElementPicked(HSP_2);
        linkIssueDialog.issuePicker().assertElementPicked(MK_1); // should be converted to uppercase

        // checking that items do not appear in suggestions
        linkIssueDialog.issuePicker().insertQuery("HSP-2").suggestions().assertDoesNotContain(HSP_2);
        linkIssueDialog.issuePicker().insertQuery("mk-1").suggestions().assertDoesNotContain(MK_1);
    }

    public void testIssuePickerSelectHidden()
    {
        getNavigator().gotoIssue(HSP_1);
        openLinkIssueDialog();
        assertThat.elementPresentByTimeout(linkIssueDialog.issuePicker().selectModelLocator(), 500);
        assertThat.notVisibleByTimeout(linkIssueDialog.issuePicker().selectModelLocator(), 500);
    }
    
    public void testPreviousSuggestionsGetReplacedInsteadOfMerged()
    {
        getNavigator().gotoIssue(HSP_1);
        openLinkIssueDialog();
        linkIssueDialog.issuePicker().insertQuery("t").suggestions().assertDoesNotContain(ISSUE_WITH_T_IN_SKIPPED_KEYWORDS);
        linkIssueDialog.issuePicker().clearInputArea().awayFromInputArea().insertQuery("h").suggestions()
                .assertHistorySearchContains(ISSUE_WITH_T_IN_SKIPPED_KEYWORDS);
        linkIssueDialog.issuePicker().clearInputArea().awayFromInputArea().insertQuery("t").suggestions()
                .assertDoesNotContain(ISSUE_WITH_T_IN_SKIPPED_KEYWORDS);
    }

    public void testIssuesWithMatchingWordsPrecededByParenthesesAreDisplayed()
    {
        getNavigator().gotoIssue(HSP_1);
        linkIssueDialog.openFromViewIssue();
        linkIssueDialog.assertReady(1000);
        linkIssueDialog.issuePicker().insertQuery("t");
        linkIssueDialog.issuePicker().suggestions().assertHistorySearchContains(ISSUE_WITH_T_PRECEDED_BY_PARENTHESIS);
        linkIssueDialog.issuePicker().suggestions().assertHistorySearchDisplayedCountEquals(3); 
        linkIssueDialog.issuePicker().suggestions().assertHistorySearchTotalCountEquals(3);
    }

    public void testDownArrowWithNoQueryShowsHistoryOnly()
    {
        getNavigator().findAllIssues();
        getNavigator().gotoIssue(HSP_1);

        IssuePicker picker = linkIssueDialog.issuePicker();

        linkIssueDialog.openFromViewIssue();
        linkIssueDialog.assertReady(1000);
        picker.assertReady(1000);
        picker.triggerSuggestionsByClick();
        picker.suggestions().assertHistorySearchTotalCountEquals(6);

        String currentSearchGroup = picker.suggestions().groupHeaderLocator("Current Search");

        assertThat.elementNotPresentByTimeout(currentSearchGroup, DROP_DOWN_WAIT);

        picker.insertQuery("h");

        assertThat.elementPresentByTimeout(currentSearchGroup, DROP_DOWN_WAIT);

        picker.inputLocatorObject().element().type(SpecialKeys.BACKSPACE);
        picker.triggerSuggestionsByClick();
        assertThat.elementNotPresentByTimeout(currentSearchGroup, DROP_DOWN_WAIT);
        
        picker.suggestions().assertHistorySearchTotalCountEquals(6);
    }

    private void openLinkIssueDialog()
    {
        linkIssueDialog.openFromViewIssue();
        linkIssueDialog.assertReady();
    }
}
