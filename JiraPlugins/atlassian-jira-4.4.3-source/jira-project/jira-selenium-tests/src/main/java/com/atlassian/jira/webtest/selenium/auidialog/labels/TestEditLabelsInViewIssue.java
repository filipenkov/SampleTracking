package com.atlassian.jira.webtest.selenium.auidialog.labels;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.framework.components.LabelsPicker;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import com.atlassian.webtest.ui.keys.SpecialKeys;
import junit.framework.Test;

/**
 * Test the 'Edit Labels' dialog. 
 *
 * @since v4.2
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestEditLabelsInViewIssue extends AbstractAuiLabelsDialogTest
{
    private static final String ISSUE_KEY = "MKY-1";
    private static final int ISSUE_ID = 10001;

    private static final String LABELS_SYSTEM_FIELD_UL_SELECTOR_FORMAT_STRING = "jquery=#labels-%d-value";
    private static final String LABELS_CUSTOM_FIELD_UL_SELECTOR_FORMAT_STRING = "jquery=#customfield_%d-%d-value";
    private static final String LABELS_CUSTOM_FIELD_NAME = "Tags";
    private static final int LABELS_CUSTOM_FIELD_ID = 10000;

    private LabelsPicker systemFieldLabelsPicker;
    private LabelsPicker customFieldLabelsPicker;

    public static Test suite()
    {
        return suiteFor(TestEditLabelsInViewIssue.class);
    }

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoIssue(ISSUE_KEY);
        systemFieldLabelsPicker = LabelsPicker.newSystemLabelsPicker(VISIBLE_DIALOG_CONTENT_SELECTOR, context());
        customFieldLabelsPicker = LabelsPicker.newCustomFieldLabelsPicker(VISIBLE_DIALOG_CONTENT_SELECTOR, LABELS_CUSTOM_FIELD_ID,
                context());
    }

    private void openSystemFieldLabelsDialog()
    {
        setCurrentLabelsPicker(systemFieldLabelsPicker);
        openLabelsDialogWithoutChecking();
        assertLabelsDialogOpen();
    }

    private void openLabelsDialogWithoutChecking()
    {
        client.click("jquery=#issuedetails a.edit-labels");
    }

    private void openCustomLabelsDialog(long customFieldId)
    {
        setCurrentLabelsPicker(customFieldLabelsPicker);
        client.click(customFieldEditLabelsLinkSelector(customFieldId));
        assertLabelsDialogOpen();
    }

    private String customFieldEditLabelsLinkSelector(long customFieldId)
    {
        return String.format("jquery=#customfieldmodule li#rowForcustomfield_%d a.edit-labels", customFieldId);
    }

    private void openLabelsDialogFromOperationsMenu()
    {
        client.click("opsbar-operations_more");
        client.click("edit-labels");
        setCurrentLabelsPicker(systemFieldLabelsPicker);
        assertLabelsDialogOpen();
    }

    public void testPopularSuggestions()
    {
        openSystemFieldLabelsDialog();
        triggerSuggestionsDropdown();
        assertSuggestionsPresent();
        assertSuggestionsContain("gadgets");
        assertSuggestionsContain("keyboard");
    }

    public void testSuggestionsOnEditingQuery()
    {
        openSystemFieldLabelsDialog();
        insertLabelText("g");
        assertSuggestionsContain("gadgets");
        getCurrentPicker().inputLocatorObject().element().type(SpecialKeys.BACKSPACE);
        assertFalse(getCurrentPicker().suggestions().isOpen());
        insertLabelText("g");
        assertSuggestionsContain("gadgets");
        getCurrentPicker().inputLocatorObject().element().type(SpecialKeys.BACKSPACE);
        assertFalse(getCurrentPicker().suggestions().isOpen());
    }

    public void testPressingEscapeResetsStateOfLabelsOnNextDialogOpen()
    {
        assertNoSystemLabelsInIssuePage(ISSUE_ID);
        openSystemFieldLabelsDialog();
        assertNoLabelsInDialog();
        addLabel("test");
        assertLabelSelectedInDialog("test");
        closeDialogByEscape();
        assertNoSystemLabelsInIssuePage(ISSUE_ID);
        openSystemFieldLabelsDialog();
        assertNoLabelsInDialog();
    }

    public void testNewLabelOptionPresent()
    {
        assertNoSystemLabelsInIssuePage(ISSUE_ID);
        openSystemFieldLabelsDialog();
        insertLabelText("blah");
        assertSuggestionsDropdownPresent();
        assertNewLabelPresent("blah");
    }   

    public void testJavaScriptInjection()
    {
        assertNoSystemLabelsInIssuePage(ISSUE_ID);
        openSystemFieldLabelsDialog();
        hackyInsertLabelText("<script>alert(\"test\")</script>");
        assertSuggestionsDropdownPresent();
        assertNewLabelPresent("<script>alert(\"test\")</script>");
    }

    public void testEditSubtaskLabelsDoesntAffectParent()
    {
        getNavigator().gotoIssue("HSP-2");
        assertSystemLabelSelectedInIssuePage(10020, "blue");
        assertSystemLabelSelectedInIssuePage(10020, "yellow");

        client.click("id=actions_10021");
        final String LOC_LABELS = "jquery=#actions_10021_drop a.issueaction-edit-labels";
        assertThat.elementPresentByTimeout(LOC_LABELS, DEFAULT_TIMEOUT);
        client.click(LOC_LABELS);
        setCurrentLabelsPicker(systemFieldLabelsPicker);
        assertLabelsDialogOpen();

        addLabel("sunflowers");
        submitDialog();
        assertDialogNotOpen();

        assertSystemLabelSelectedInIssuePage(10020, "blue");
        assertSystemLabelSelectedInIssuePage(10020, "yellow");
        assertThat.elementNotPresent("li a:contains('sunflowers')");
    }

    public void testOptionalNotification()
    {
        openSystemFieldLabelsDialog();
        assertSendNotificationsCheckboxUnchecked();
        checkSendNotifications();
        addLabel("blah");
        submitDialog();
        assertDialogNotOpen();
        assertSystemLabelSelectedInIssuePage(ISSUE_ID, "blah");
        getNavigator().gotoIssue(ISSUE_KEY);
        openSystemFieldLabelsDialog();
        assertSendNotificationsCheckboxChecked();
        uncheckSendNotifications();
        submitDialog();
        assertDialogNotOpen();
        getNavigator().gotoIssue(ISSUE_KEY);
        openSystemFieldLabelsDialog();
        assertSendNotificationsCheckboxUnchecked();
    }

    public void testEditLabelsWithoutSession() throws Exception
    {
        backgroundLogout();
        openLabelsDialogWithoutChecking();
        assertAuiErrorMessage("You do not have the permission to see the specified issue");
    }

    public void testEditLabelsFromOperationsMenu()
    {
        openLabelsDialogFromOperationsMenu();
        addLabel("blah");
        addLabel("andblah");
        addLabel("blahblah");
        client.focus(defaultDialogSubmitSelector());
        submitDialog();
        assertDialogNotOpen();
        assertSystemLabelSelectedInIssuePage(ISSUE_ID, "blah");
        assertSystemLabelSelectedInIssuePage(ISSUE_ID, "andblah");
        assertSystemLabelSelectedInIssuePage(ISSUE_ID, "blahblah");
    }

    public void testChangeHistoryForSystemLabelsEdit() throws Exception
    {
        openSystemFieldLabelsDialog();
        addLabel("blah");
        addLabel("andblah");
        addLabel("blahblah");
        submitDialogAndWaitForAjax();
        getNavigator().openHistoryTab();
        assertThat.elementContainsText("jquery=#changehistory_10020 td.activity-name", "Labels");
        assertThat.elementContainsText("jquery=#changehistory_10020 td.activity-new-val", "andblah blah blahblah");
    }

    public void testEditCustomLabels() throws Exception
    {
        openCustomLabelsDialog(LABELS_CUSTOM_FIELD_ID);
        addLabel("customone");
        addLabel("customtwo");
        addLabel("customthree");
        submitDialogAndWaitForAjax();
        assertNoSystemLabelsInIssuePage(ISSUE_ID);
        assertCustomLabelSelectedInIssuePage(ISSUE_ID, LABELS_CUSTOM_FIELD_ID, "customone");
        assertCustomLabelSelectedInIssuePage(ISSUE_ID, LABELS_CUSTOM_FIELD_ID, "customtwo");
        assertCustomLabelSelectedInIssuePage(ISSUE_ID, LABELS_CUSTOM_FIELD_ID, "customthree");
    }

    public void testChangeHistoryForCustomLabelsEdit() throws Exception
    {
        openCustomLabelsDialog(LABELS_CUSTOM_FIELD_ID);
        addLabel("customone");
        addLabel("customtwo");
        addLabel("customthree");
        submitDialogAndWaitForAjax();
        getNavigator().openHistoryTab();
        assertThat.elementContainsText("jquery=#changehistory_10020 td.activity-name", LABELS_CUSTOM_FIELD_NAME);
        assertThat.elementContainsText("jquery=#changehistory_10020 td.activity-new-val", "customone customthree customtwo");
    }

    private void assertSystemLabelSelectedInIssuePage(int issueId, String label)
    {
        assertLabelSelectedFor(String.format(LABELS_SYSTEM_FIELD_UL_SELECTOR_FORMAT_STRING, issueId), label);
    }

    private void assertNoSystemLabelsInIssuePage(int issueId)
    {
        assertNoLabelsFor(String.format(LABELS_SYSTEM_FIELD_UL_SELECTOR_FORMAT_STRING, issueId));
    }

    private void assertCustomLabelSelectedInIssuePage(int issueId, long customFieldId, String label)
    {
        assertLabelSelectedFor(customLabelsFieldSelectorFor(issueId, customFieldId), label);
    }

    private void assertNoCustomLabelsInIssuePage(int issueId, long customFieldId)
    {
        assertNoLabelsFor(customLabelsFieldSelectorFor(issueId, customFieldId));
    }

    private String customLabelsFieldSelectorFor(int issueId, long customFieldId)
    {
        return String.format(LABELS_CUSTOM_FIELD_UL_SELECTOR_FORMAT_STRING, customFieldId, issueId);
    }

    private void assertLabelSelectedFor(String labelsFieldSelector, String label)
    {
        assertThat.elementPresentByTimeout(String.format("%s li a:contains('%s')", labelsFieldSelector, label), 1000);
    }

    private void assertNoLabelsFor(String labelsSelector)
    {
        assertThat.elementPresent(String.format("%s:contains(None)", labelsSelector));
        assertThat.elementNotPresent(String.format("%s li a", labelsSelector));
    }

}
