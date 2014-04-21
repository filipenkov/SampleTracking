package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.component.Input;
import com.atlassian.jira.webtest.framework.core.component.Options;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.SeleniumIssuePickerPopup;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.framework.model.DefaultIssueActions;
import com.atlassian.jira.webtest.framework.model.IssueData;
import com.atlassian.jira.webtest.framework.page.issue.ConvertToSubTaskSelectTypes;
import com.atlassian.jira.webtest.framework.page.issue.ViewIssue;
import com.atlassian.jira.webtest.framework.page.issue.move.MoveSubTaskChooseOperation;
import com.atlassian.jira.webtest.framework.page.issue.move.MoveSubTaskConfirmation;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.keyboard.SeleniumTypeWriter;
import com.atlassian.webtest.ui.keys.TypeMode;

import java.util.EnumSet;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isFalse;
import static com.atlassian.jira.webtest.framework.core.QueryAssertions.isEqual;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup.SearchMode.RECENT_ISSUES;
import static com.atlassian.webtest.ui.keys.Sequences.charsBuilder;

/**
 * Tests for the issue picker popup.
 *
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestIssuePickerPopup extends JiraSeleniumTest
{
    private final static long TIMEOUT = 10000;
    private final static String TIMEOUT_STR = Long.toString(TIMEOUT);
    private static final String ISSUESELECTOR_POPUP = "IssueSelectorPopup";
    private static final String SEARCH_REQUEST_ID = "10000";
    private static final String CSS_LINK_ISSUE_DIALOG_TEXTAREA = "css=#link-issue-dialog #linkKey-textarea";

    private ConvertToSubTaskSelectTypes<ViewIssue> convertToSubtask;
    private MoveSubTaskConfirmation<ViewIssue> moveSubTaskConfirmation;
    private CurrentPage currentPage;
    private IssuePickerPopup currentPicker;

    static interface CurrentPage
    {
        TimedCondition isAt();
        Input parentIssueInput();
        IssuePickerPopup openIssuePicker();
    }

    private class Convert implements CurrentPage
    {
        public TimedCondition isAt() { return convertToSubtask.isAt(); }
        public Input parentIssueInput() { return convertToSubtask.parentIssueInput(); }
        public IssuePickerPopup openIssuePicker() { return convertToSubtask.openParentIssuePicker(); }
    }

    private class Move implements CurrentPage
    {
        public TimedCondition isAt() { return moveSubTaskConfirmation.isAt(); }
        public Input parentIssueInput() { return moveSubTaskConfirmation.parentIssueInput(); }
        public IssuePickerPopup openIssuePicker() { return moveSubTaskConfirmation.openParentIssuePicker(); }
    }

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestIssuePickerPopup.xml");
    }

    /**
     * Tests the issue picker popup on the convert to subtask screen.
     * 
     */
    public void testConvertToSubtask()
    {
        goToConvertToSubtask(Issue.HSP1);
        shouldSelectHsp2AsHsp1Parent();
        goToConvertToSubtask(Issue.HSP2);
        shouldSelectHsp1AsHsp2Parent();
        goToConvertToSubtask(Issue.HSP3);
        shouldSelectViaSearchRequestMode();
        goToConvertToSubtask(Issue.HSP1);
        shouldHandleSwitchingToSearchRequestAndBack();
        goToConvertToSubtask(Issue.HSP1);
        shouldSwitchToRecentIssuesGivenFilterSelectSetToDefaultValue();
    }

    private void shouldSelectHsp2AsHsp1Parent()
    {
        setCurrentPicker();
        assertInRecentIssues();
        assertIssues(EnumSet.of(Issue.HSP2, Issue.HSP3, Issue.HSP4));
        selectIssue(Issue.HSP2);
        assertIssueSelectedInConvertIssue(Issue.HSP2);
    }

    private void shouldSelectHsp1AsHsp2Parent()
    {
        setCurrentPicker();
        assertInRecentIssues();
        assertIssues(EnumSet.of(Issue.HSP1, Issue.HSP3, Issue.HSP4));
        selectIssue(Issue.HSP1);
        assertIssueSelectedInConvertIssue(Issue.HSP1);
    }

    private void shouldSelectViaSearchRequestMode()
    {
        setCurrentPicker();
        assertInRecentIssues();
        assertIssues(EnumSet.of(Issue.HSP2, Issue.HSP1, Issue.HSP4));
        switchToSearchRequest();
        assertInSearchRequest();
        assertIssues(EnumSet.of(Issue.HSP4));
        selectIssue(Issue.HSP4);
        assertIssueSelectedInConvertIssue(Issue.HSP4);
    }

    private void shouldHandleSwitchingToSearchRequestAndBack()
    {
        setCurrentPicker();
        assertInRecentIssues();
        assertIssues(EnumSet.of(Issue.HSP2, Issue.HSP3, Issue.HSP4));
        switchToSearchRequest();
        assertInSearchRequest();
        assertIssues(EnumSet.of(Issue.HSP4, Issue.HSP3));
        switchToRecentIssues();
        assertInRecentIssues();
        assertIssues(EnumSet.of(Issue.HSP2, Issue.HSP3, Issue.HSP4));
        selectIssue(Issue.HSP3);
        assertIssueSelectedInConvertIssue(Issue.HSP3);
    }

    private void shouldSwitchToRecentIssuesGivenFilterSelectSetToDefaultValue()
    {
        setCurrentPicker();
        assertInRecentIssues();
        assertIssues(EnumSet.of(Issue.HSP2, Issue.HSP3, Issue.HSP4));
        switchToSearchRequest();
        assertInSearchRequest();
        assertIssues(EnumSet.of(Issue.HSP4, Issue.HSP3));
        switchToRecentIssuesViaSelect();
        assertInRecentIssues();
        assertIssues(EnumSet.of(Issue.HSP2, Issue.HSP3, Issue.HSP4));
        selectIssue(Issue.HSP3);
        assertIssueSelectedInConvertIssue(Issue.HSP3);
    }

    /**
     * Tests the issue picker on the move to parent screen.
     */
    public void testMoveIssueParent()
    {
        //Select issue 1.
        gotoMoveParent(Issue.HSP5);
        setCurrentPicker();
        assertInRecentIssues();
        assertIssues(EnumSet.of(Issue.HSP1, Issue.HSP2, Issue.HSP3));
        selectIssue(Issue.HSP1);
        assertMoveIssue(Issue.HSP1);

        //Select issue 3
        gotoMoveParent(Issue.HSP5);
        setCurrentPicker();
        assertInRecentIssues();
        assertIssues(EnumSet.of(Issue.HSP1, Issue.HSP2, Issue.HSP3));
        selectIssue(Issue.HSP3);
        assertMoveIssue(Issue.HSP3);

        //Select an issue from the search request view.
        gotoMoveParent(Issue.HSP5);
        setCurrentPicker();
        assertInRecentIssues();
        assertIssues(EnumSet.of(Issue.HSP1, Issue.HSP2, Issue.HSP3));
        switchToSearchRequest();
        assertInSearchRequest();
        assertIssues(EnumSet.of(Issue.HSP3));
        selectIssue(Issue.HSP3);
        assertMoveIssue(Issue.HSP3);

        //Select an issue from the "recent view" are looking at a search request. Just to make sure all the parameters get
        //propagated.
        gotoMoveParent(Issue.HSP5);
        setCurrentPicker();
        assertInRecentIssues();
        assertIssues(EnumSet.of(Issue.HSP1, Issue.HSP2, Issue.HSP3));
        switchToSearchRequest();
        assertInSearchRequest();
        assertIssues(EnumSet.of(Issue.HSP3));
        switchToRecentIssues();
        assertIssues(EnumSet.of(Issue.HSP1, Issue.HSP2, Issue.HSP3));
        assertInRecentIssues();
        selectIssue(Issue.HSP2);
        assertMoveIssue(Issue.HSP2);

        //Select an issue from the "recent view" are looking at a search request. We transition from the search -> recent
        //issues via the select list rather than the link.
        gotoMoveParent(Issue.HSP5);
        setCurrentPicker();
        assertInRecentIssues();
        assertIssues(EnumSet.of(Issue.HSP1, Issue.HSP2, Issue.HSP3));
        switchToSearchRequest();
        assertInSearchRequest();
        assertIssues(EnumSet.of(Issue.HSP3));
        switchToRecentIssuesViaSelect();
        assertIssues(EnumSet.of(Issue.HSP1, Issue.HSP2, Issue.HSP3));
        assertInRecentIssues();
        selectIssue(Issue.HSP2);
        assertMoveIssue(Issue.HSP2);
    }

    public void testLinkIssue() throws InterruptedException
    {
        getNavigator().gotoIssue(HSP_1);
        client.click("link-issue");
        assertThat.elementPresentByTimeout("css=#link-issue-dialog.aui-dialog-content-ready", TIMEOUT);
        openIssuePickerFromMultiSelect();

        // add single booger from issue picker popup
        client.clickLinkWithText("HSP-2", false);
        client.selectWindow(null);
        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog li[title*='HSP-2']", TIMEOUT);

        // does not suggest HSP-2 after add
        final SeleniumTypeWriter writer = new SeleniumTypeWriter(context().client(),
                SeleniumLocators.css("#link-issue-dialog #linkKey-textarea", context()).fullLocator(), TypeMode.TYPE);
        writer.type(charsBuilder("HSP-2").typeMode(TypeMode.INSERT_WITH_EVENT).build());

//        client.typeWithFullKeyEvents(CSS_LINK_ISSUE_DIALOG_TEXTAREA, "HSP-2");
        assertThat.elementNotPresentByTimeout("jquery=#linkKey-suggestions li:contains(HSP-2)", TIMEOUT);

        // after remove, does suggest HSP-2
        client.click("jquery=#link-issue-dialog li[title*='HSP-2'] .item-delete");
        client.typeWithFullKeyEvents(CSS_LINK_ISSUE_DIALOG_TEXTAREA,"HSP-2");
        assertThat.elementPresentByTimeout("jquery=#linkKey-suggestions li:contains(HSP-2)", TIMEOUT);

        // add multiple issues to field
        openIssuePickerFromMultiSelect();
        client.clickLinkWithText("Select multiple issues", true);
        client.click("jquery=input[value=HSP-4]");
        client.click("jquery=input[value=HSP-3]");
        client.click("jquery=input[value=HSP-2]");
        client.clickLinkWithText("Select issues", false);

        // put focus back to main window
        client.selectWindow(null);

        // Check that all boogers added from popup are present
        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog li[title*='HSP-4']", TIMEOUT);
        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog li[title*='HSP-3']", TIMEOUT);
        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog li[title*='HSP-2']", TIMEOUT);

        // Check that partially entered value before adding from popup can still be boogerised
        client.typeWithFullKeyEvents(CSS_LINK_ISSUE_DIALOG_TEXTAREA,"a");
        assertThat.elementPresentByTimeout("jquery=#linkKey-multi-select[data-query='a']");
        client.click("jquery=#linkKey-suggestions li.active a");
        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog li[title='a']", TIMEOUT);

        // resetting state
        getNavigator().gotoIssue("HSP-1");
        client.click("link-issue");
        assertThat.elementPresentByTimeout("css=#link-issue-dialog.aui-dialog-content-ready", TIMEOUT);

        // Testing issue picker does not override already boogerised issue
        client.typeWithFullKeyEvents(CSS_LINK_ISSUE_DIALOG_TEXTAREA, "HSP-2");
        assertThat.elementPresentByTimeout("jquery=#linkKey-multi-select[data-query='HSP-2']");
        client.click("jquery=#linkKey-suggestions li.active a");
        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog li[title*='HSP-2']", TIMEOUT);
        openIssuePickerFromMultiSelect();
        client.clickLinkWithText("HSP-2", false);
        client.selectWindow(null);

        // checking there is only one booger
        if (!client.getEval("window.jQuery(\"#link-issue-dialog li[title*='HSP-2']\").length").equals("1"))
        {
            throw new AssertionError("There should be only one booger!");
        }
    }

//    public void testLinkIssue2()
//    {
//        IssuePicker issuePicker = globalPages().viewIssueFor(Issue.HSP1).openDialog(IssuePicker.class);
//        client.click("link-issue");
//        assertThat.elementPresentByTimeout("css=#link-issue-dialog.aui-dialog-content-ready", TIMEOUT);
//        openIssuePickerFromMultiSelect();
//
//        // add single booger from issue picker popup
//        client.clickLinkWithText("HSP-2", false);
//        client.selectWindow(null);
//        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog li[title*='HSP-2']", TIMEOUT);
//
//        // does not suggest HSP-2 after add
//        client.typeWithFullKeyEvents(CSS_LINK_ISSUE_DIALOG_TEXTAREA, "HSP-2");
//        assertThat.elementNotPresentByTimeout("jquery=#linkKey-suggestions li:contains(HSP-2)", TIMEOUT);
//
//        // after remove, does suggest HSP-2
//        client.click("jquery=#link-issue-dialog li[title*='HSP-2'] .item-delete");
//        client.typeWithFullKeyEvents(CSS_LINK_ISSUE_DIALOG_TEXTAREA,"HSP-2");
//        assertThat.elementPresentByTimeout("jquery=#linkKey-suggestions li:contains(HSP-2)", TIMEOUT);
//
//        // add multiple issues to field
//        openIssuePickerFromMultiSelect();
//        client.clickLinkWithText("Select multiple issues", true);
//        client.click("jquery=input[value=HSP-4]");
//        client.click("jquery=input[value=HSP-3]");
//        client.click("jquery=input[value=HSP-2]");
//        client.clickLinkWithText("Select issues", false);
//
//        // put focus back to main window
//        client.selectWindow(null);
//
//        // Check that all boogers added from popup are present
//        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog li[title*='HSP-4']", TIMEOUT);
//        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog li[title*='HSP-3']", TIMEOUT);
//        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog li[title*='HSP-2']", TIMEOUT);
//
//        // Check that partially entered value before adding from popup can still be boogerised
//        client.typeWithFullKeyEvents(CSS_LINK_ISSUE_DIALOG_TEXTAREA,"a");
//        client.click("jquery=#linkKey-suggestions li.active a");
//        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog li[title='a']", TIMEOUT);
//
//        // resetting state
//        getNavigator().gotoIssue("HSP-1");
//        client.click("link-issue");
//        assertThat.elementPresentByTimeout("css=#link-issue-dialog.aui-dialog-content-ready", TIMEOUT);
//
//        // Testing issue picker does not override already boogerised issue
//        client.typeWithFullKeyEvents(CSS_LINK_ISSUE_DIALOG_TEXTAREA, "HSP-2");
//        client.click("jquery=#linkKey-suggestions li.active a");
//        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog li[title*='HSP-2']", TIMEOUT);
//        openIssuePickerFromMultiSelect();
//        client.clickLinkWithText("HSP-2", false);
//        client.selectWindow(null);
//
//        // checking there is only one booger
//        if (!client.getEval("window.jQuery(\"#link-issue-dialog li[title*='HSP-2']\").length").equals("1"))
//        {
//            throw new AssertionError("There should be only one booger!");
//        }
//    }

    public void testShouldDisplayAndSelectSubTasksWhenInvokedFromLinkIssue()
    {

        // open the link issue dialog for HSP-1
        getNavigator().gotoIssue(HSP_1);
        client.click("link-issue");
        assertThat.elementPresentByTimeout("css=#link-issue-dialog.aui-dialog-content-ready", TIMEOUT);

        // Open the issue picker pop-up, select a sub-task issue (HSP-5) and close the pop-up
        openIssuePickerFromMultiSelect();
        client.clickLinkWithText("HSP-5", false);
        client.selectWindow(null);

        // Confirm that the sub-task has been added as a 'booger' in the dialog.
        assertThat.elementPresentByTimeout("jquery=#link-issue-dialog li[title*='HSP-5']", TIMEOUT);
    }

    private void setCurrentPicker()
    {
        currentPicker = currentPage.openIssuePicker();
    }

    private void goToConvertToSubtask(Issue issue)
    {
        convertToSubtask = goToConvertThroughViewIssue(issue);
        assertThat(convertToSubtask.isAt(), byDefaultTimeout());
        currentPage = new Convert();
    }


    private void gotoMoveParent(Issue issue)
    {
        moveSubTaskConfirmation = goToMoveThroughViewIssue(issue);
        assertThat(moveSubTaskConfirmation.isAt(), byDefaultTimeout());
        currentPage = new Move();
    }

    @SuppressWarnings ({ "unchecked" })
    private ConvertToSubTaskSelectTypes<ViewIssue> goToConvertThroughViewIssue(IssueData issue)
    {
        return globalPages().goToViewIssueFor(issue).menu().invoke(DefaultIssueActions.CONVERT_TO_SUBTASK)
                .getChild(ConvertToSubTaskSelectTypes.class);
    }

    @SuppressWarnings ({ "unchecked" })
    private MoveSubTaskConfirmation<ViewIssue> goToMoveThroughViewIssue(IssueData issue)
    {
        ViewIssue viewIssue = globalPages().goToViewIssueFor(issue);
        MoveSubTaskChooseOperation<ViewIssue> moveStep1 = viewIssue.menu().invoke(DefaultIssueActions.MOVE)
                .getChild(MoveSubTaskChooseOperation.class);
        assertThat(moveStep1.isAt(), byDefaultTimeout());
        assertThat(moveStep1.isSelectable(MoveSubTaskChooseOperation.FlowType.CHANGE_PARENT), byDefaultTimeout());
        moveStep1.selectFlowType(MoveSubTaskChooseOperation.FlowType.CHANGE_PARENT).next();
        return viewIssue.getChild(MoveSubTaskConfirmation.class);
    }


    private void assertInSearchRequest()
    {
        assertThat(currentPicker.isOpen(), byDefaultTimeout());
        assertThat(currentPicker.isInMode(IssuePickerPopup.SearchMode.FILTER), byDefaultTimeout());
    }

    private void assertInRecentIssues()
    {
        assertThat(currentPicker.isOpen(), byDefaultTimeout());
        assertThat(currentPicker.isInMode(RECENT_ISSUES), byDefaultTimeout());
        assertThat(currentPicker.hasAnyIssues(IssuePickerPopup.ResultSection.CURRENT_ISSUES), isFalse().byDefaultTimeout());
    }


    private void assertIssues(final EnumSet<Issue> presentIssues)
    {
        assertIssues(presentIssues, EnumSet.complementOf(presentIssues));
    }

    private void assertIssues(EnumSet<Issue> presentIssues, EnumSet<Issue> hiddenIssues)
    {
        assertThat(currentPicker.isOpen(), byDefaultTimeout());
        //Make sure the present links are there.
        for (Issue presentIssue : presentIssues)
        {
            assertThat(currentPicker.hasIssue(presentIssue), byDefaultTimeout());
        }
        //Make sure issues that are not present are not there.
        for (Issue hiddenIssue : hiddenIssues)
        {
            assertThat(currentPicker.hasIssue(hiddenIssue), isFalse().byDefaultTimeout());
        }
    }

    private void switchToSearchRequest()
    {
        currentPicker.switchToFilter(Options.value(SEARCH_REQUEST_ID));
    }

    private void switchToRecentIssues()
    {
        currentPicker.switchToRecentIssues();
    }

    private void switchToRecentIssuesViaSelect()
    {
        currentPicker.filterSelect().selectDefault();
    }

    private void selectIssue(Issue issue)
    {
        currentPicker.close().bySelectingIssue(issue);
    }

    private void openIssuePickerFromMultiSelect()
    {
        client.clickLinkWithText("[Select Issue]", false);
        client.waitForPopUp(ISSUESELECTOR_POPUP, TIMEOUT_STR);
        client.selectPopUp(ISSUESELECTOR_POPUP);
    }

    private void openIssuePicker(ViewIssue vi)
    {
        currentPicker = new SeleniumIssuePickerPopup(vi, SeleniumLocators.css("a.popup-trigger", context()),
                context()).open();
    }

    private void assertMoveIssue(Issue issue)
    {
        assertThat.formElementEquals("id=parentIssue", issue.key());
    }

    private void assertIssueSelectedInConvertIssue(Issue issue)
    {
        assertThat(currentPage.isAt(), byDefaultTimeout());
        assertThat(currentPage.parentIssueInput().value(), isEqual(issue.key()).byDefaultTimeout());
    }

    private static enum Issue implements IssueData
    {
        HSP1("HSP-1", 10000),
        HSP2("HSP-2", 10001),
        HSP3("HSP-3", 10002),
        HSP4("HSP-4", 10003),
        HSP5("HSP-5", 10010, HSP4),
        MKY1("MKY-1", 10020);

        private final String key;
        private final long id;
        private final Issue parent;

        private Issue(final String key, final long id)
        {
            this(key, id, null);
        }

        private Issue(final String key, final long id, Issue parent)
        {
            this.key = key;
            this.id = id;
            this.parent = parent;
        }

        public String key()
        {
            return key;
        }

        public long id()
        {
            return id;
        }

    }
}
