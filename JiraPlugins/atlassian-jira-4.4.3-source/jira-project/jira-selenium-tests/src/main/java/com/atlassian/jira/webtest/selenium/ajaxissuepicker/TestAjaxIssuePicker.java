package com.atlassian.jira.webtest.selenium.ajaxissuepicker;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.jira.webtest.selenium.harness.util.FormCleaner;
import junit.framework.Test;

@WebTest({Category.SELENIUM_TEST })
@Quarantine
public class TestAjaxIssuePicker extends JiraSeleniumTest
{
    private static final String HISTORY_SEARCH = "History Search";
    private static final String CURRENT_SEARCH = "Current Search";
    private static final String HSP_1 = "HSP-1";
    private static final String HSP_2 = "HSP-2";
    private static final String MKY_2 = "MKY-2";
    private static final String MKY_1 = "MKY-1";
    private static final String HSP = "hsp";
    private static final String MKY = "mk";
    private static final String _ = "_";
    private static final String PARENT_ISSUE_KEY_SECTION = "id=parentIssueKey_s_";
    private static final String PARENT_ISSUE_KEY_ITEM = "id=parentIssueKey_i_";
    private static final String PARENT_ISSUE_SECTION = "id=parentIssue_s_";
    private static final String PARENT_ISSUE_ITEM = "id=parentIssue_i_";
    private static final String NO_KEY = "_n";
    private static final String LINK_KEY_INPUT = "jquery=#linkKey-multi-select textarea";
    private static final String HSP_11 = "HSP-11";
    private static final String HSP_3 = "HSP-3";
    private static final String HSP_8 = "HSP-8";

    public static Test suite()
    {
        return suiteFor(TestAjaxIssuePicker.class);
    }

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestAjaxIssuePicker.xml");
        getNavigator().logout(getXsrfToken());
    }

    @Override
    protected void onTearDown() throws Exception
    {
        new FormCleaner(context()).cleanUpPage();
        super.onTearDown();
    }

    public void testNoHistory()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        testNoSection(HISTORY_SEARCH);
    }

    public void testNoCurrentSearch()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoIssue(HSP_2);
        testNoSection(CURRENT_SEARCH);
    }

    private void testNoSection(String section)
    {
        getNavigator().gotoIssue(HSP_1);
        openLinkDialogAndFocusPicker();

        client.typeWithFullKeyEvents(LINK_KEY_INPUT, HSP);

        assertIssueNotInSection(section, HSP_1);
    }

    private void openLinkDialogAndFocusPicker()
    {
        client.click("link-issue");
        assertThat.elementPresentByTimeout(LINK_KEY_INPUT, 5000);
        client.focus(LINK_KEY_INPUT);
    }

    private String getSectionId(final String section)
    {
        return section.replaceAll(" ", "-").toLowerCase();
    }

    private String getLegacySectionId (final String section) {
        return section.replaceAll("(\\w).*\\s(\\w).*", "$1$2").toLowerCase();
    }


    public void testOneHistory()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoIssue(HSP_1);
        testPositiveItems(HISTORY_SEARCH);
    }

    public void testAllCurrentSearch()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().findAllIssues();
        testPositiveItems(CURRENT_SEARCH);
//        assertThat.textPresent("Showing 17 of 18 matching issues");
    }

    private void testPositiveItems(String section)
    {
        getNavigator().gotoIssue(HSP_2);
        openLinkDialogAndFocusPicker();
        client.typeWithFullKeyEvents(LINK_KEY_INPUT, HSP);
        assertIssueInSection(section, HSP_1);
        assertIssueNotInSection(section, HSP_2);
    }

    public void testHistoryProjSub()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoIssue(HSP_1);
        getNavigator().gotoIssue(HSP_2);

        getNavigator().gotoIssue(MKY_2);
        testProjSub(HISTORY_SEARCH);
    }

    public void testCurrentProjSub()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().findAllIssues();
        testProjSub(CURRENT_SEARCH);
    }

    private void testProjSub(String section)
    {
        getNavigator().gotoIssue(MKY_1);

        client.click("issue-to-subtask");
        client.waitForPageToLoad(PAGE_LOAD_WAIT);

        // Different Project
        client.typeWithFullKeyEvents("parentIssueKey", HSP);
        assertThat.elementPresentByTimeout(PARENT_ISSUE_KEY_SECTION + getLegacySectionId(section), DROP_DOWN_WAIT);
        assertThat.elementPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + NO_KEY);
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + HSP_1);
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + HSP_2);
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + MKY_2);

        // Same project
        client.typeWithFullKeyEvents("parentIssueKey", MKY);
        assertThat.visibleByTimeout(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + MKY_2, DROP_DOWN_WAIT);
        assertThat.elementPresent(PARENT_ISSUE_KEY_SECTION + getLegacySectionId(section));
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + NO_KEY);
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + HSP_1);
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + HSP_2);
        assertThat.elementPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + MKY_2);

    }

    public void testHistoryProjLink()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoIssue(HSP_1);
        getNavigator().gotoIssue(MKY_1);
        testProjLink(HISTORY_SEARCH);
    }

    public void testCurrentProjLink()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().findAllIssues();
        testProjLink(CURRENT_SEARCH);
    }

    private void testProjLink(String section)
    {
        getNavigator().gotoIssue(HSP_2);
        openLinkDialogAndFocusPicker();

        client.typeWithFullKeyEvents(LINK_KEY_INPUT, HSP);

        assertIssueInSection(section, HSP_1);
        assertIssueNotInSection(section, MKY_1);
        assertIssueNotInSection(section, HSP_2);

        client.typeWithFullKeyEvents(LINK_KEY_INPUT, MKY);

        assertIssueNotInSection(section, HSP_1);
        assertIssueNotInSection(section, HSP_2);
        assertIssueInSection(section, MKY_1);

        client.typeWithFullKeyEvents(LINK_KEY_INPUT, "1 ");

        assertIssueNotInSection(section, HSP_2);
        assertIssueInSection(section, HSP_1);
        assertIssueInSection(section, MKY_1);

        client.click("id=issue-link-cancel", false);
        assertThat.notVisibleByTimeout("id=link-issue-dialog", DROP_DOWN_WAIT);
    }

    public void testHistoryParentIssue()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoIssue(HSP_1);
        getNavigator().gotoIssue(MKY_1);
        getNavigator().gotoIssue(HSP_2);
        testParentIssue(HISTORY_SEARCH);
    }

    public void testCurrentParentIssue()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().findAllIssues();
        testParentIssue(CURRENT_SEARCH);
    }

    private void testParentIssue(String section)
    {
        getNavigator().gotoIssue(HSP_11);

        openLinkDialogAndFocusPicker();

        client.typeWithFullKeyEvents(LINK_KEY_INPUT, HSP);

        assertIssueInSection(section, HSP_1);
        assertIssueInSection(section, HSP_1);
        assertIssueNotInSection(section, MKY_1);
        assertIssueNotInSection(section, HSP_11);

        //this is a bit of a hack since chooseOkOnNextConfirmation() doesn't work FF2
//        client.chooseOkOnNextConfirmation();
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");

        getNavigator().gotoIssue(HSP_11);
        client.click("move-issue", true);
        client.click("move.subtask.parent.operation.name_id");
        client.click("next_submit", true);

        client.typeWithFullKeyEvents("parentIssue", MKY);


        getLegacySectionId(section);

        waitFor(DROP_DOWN_WAIT);
        assertThat.elementPresent(PARENT_ISSUE_SECTION + getLegacySectionId(section));
        assertThat.elementPresent(PARENT_ISSUE_ITEM + getLegacySectionId(section) + NO_KEY);
        assertThat.elementNotPresent(PARENT_ISSUE_ITEM + getLegacySectionId(section) + _ + HSP_1);
        assertThat.elementNotPresent(PARENT_ISSUE_ITEM + getLegacySectionId(section) + _ + MKY_1);
        assertThat.elementNotPresent(PARENT_ISSUE_ITEM + getLegacySectionId(section) + _ + HSP_2);
        assertThat.elementNotPresent(PARENT_ISSUE_ITEM + getLegacySectionId(section) + _ + HSP_11);

        client.typeWithFullKeyEvents("parentIssue", HSP);
        assertThat.visibleByTimeout(PARENT_ISSUE_ITEM + getLegacySectionId(section) + _ + HSP_1, DROP_DOWN_WAIT);
        assertThat.elementPresent(PARENT_ISSUE_SECTION + getLegacySectionId(section));
        assertThat.elementNotPresent(PARENT_ISSUE_ITEM + getLegacySectionId(section) + NO_KEY);
        assertThat.elementPresent(PARENT_ISSUE_ITEM + getLegacySectionId(section) + _ + HSP_1);
        assertThat.elementNotPresent(PARENT_ISSUE_ITEM + getLegacySectionId(section) + _ + MKY_1);
        assertThat.elementNotPresent(PARENT_ISSUE_ITEM + getLegacySectionId(section) + _ + HSP_2);
        assertThat.elementNotPresent(PARENT_ISSUE_ITEM + getLegacySectionId(section) + _ + HSP_11);

        client.click("id=reparent_submit", true);
        
    }

    public void testHistorySubTasks()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoIssue(HSP_1);
        getNavigator().gotoIssue(MKY_1);
        getNavigator().gotoIssue(HSP_11);
        testSubTasks(HISTORY_SEARCH);
    }

    public void testCurrentSubTasks()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().findAllIssues();
        testSubTasks(CURRENT_SEARCH);
    }

    private void testSubTasks(String section)
    {
        getNavigator().gotoIssue(HSP_3);

        openLinkDialogAndFocusPicker();

        client.typeWithFullKeyEvents(LINK_KEY_INPUT, HSP);

        assertIssueInSection(section, HSP_1);
        assertIssueInSection(section, HSP_11);
        assertIssueNotInSection(section, MKY_1);
        assertIssueNotInSection(section, HSP_3);

        //this is a bit of a hack since chooseOkOnNextConfirmation() doesn't work FF2
//        client.chooseOkOnNextConfirmation();
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        getNavigator().gotoIssue(HSP_3);
        client.click("issue-to-subtask");
        client.waitForPageToLoad(PAGE_LOAD_WAIT);

        client.typeWithFullKeyEvents("parentIssueKey", MKY);

        assertThat.elementPresentByTimeout(PARENT_ISSUE_KEY_SECTION + getLegacySectionId(section), DROP_DOWN_WAIT);
        assertThat.elementPresentByTimeout(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + NO_KEY, DROP_DOWN_WAIT);
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + HSP_1);
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + MKY_1);
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + HSP_3);
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + HSP_11);

        client.typeWithFullKeyEvents("parentIssueKey", HSP);
        assertThat.visibleByTimeout(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + HSP_1, DROP_DOWN_WAIT);
        assertThat.elementPresent(PARENT_ISSUE_KEY_SECTION + getLegacySectionId(section));
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + NO_KEY);
        assertThat.elementPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + HSP_1);
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + MKY_1);
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + HSP_3);
        assertThat.elementNotPresent(PARENT_ISSUE_KEY_ITEM + getLegacySectionId(section) + _ + HSP_11);

    }

    private void assertIssueInSection (String section, String issue) {
        String sectionId = getSectionId(section);
        assertThat.elementPresentByTimeout("jquery=#" + sectionId + ":contains(" + issue + ")", DROP_DOWN_WAIT);
    }

    private void assertIssueNotInSection (String section, String issue) {
        String sectionId = getSectionId(section);
        assertThat.elementNotPresentByTimeout("jquery=#" + sectionId + ":contains(" + issue + ")", DROP_DOWN_WAIT);
    }

    public void testCaseInsensitivity()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().findAllIssues();
        getNavigator().gotoIssue(HSP_11);

        openLinkDialogAndFocusPicker();

        client.typeWithFullKeyEvents(LINK_KEY_INPUT, "Long");

        assertIssueInSection(CURRENT_SEARCH, HSP_8);

        client.typeWithFullKeyEvents(LINK_KEY_INPUT, "Lon");

        assertIssueInSection(CURRENT_SEARCH, HSP_8);

        client.typeWithFullKeyEvents(LINK_KEY_INPUT, "long");
        assertIssueInSection(CURRENT_SEARCH, HSP_8);

        client.typeWithFullKeyEvents(LINK_KEY_INPUT, "loNg");
        assertIssueInSection(CURRENT_SEARCH, HSP_8);
    }
}
