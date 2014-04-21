package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.Flaky;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

/**
 * Tests for the labels picker in the edit issue, create issue, and bulk edit pages.
 *
 *
 *
 * @since v4.2
 */
@SkipInBrowser(browsers={Browser.IE}) //Time out issue - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
@Quarantine
public class TestEditLabelsPicker extends JiraSeleniumTest
{
    private static final String ENTER_KEY = "\\13";
    private static final String ESCAPE_KEY = "\\27";

    private static final String JQUERY_LOCATOR = "jquery=";
    private static final String DROP_MENU_SELECTOR = " .drop-menu";
    private static final String SUGGESTIONS_LIST_SELECTOR = " ul.labels-suggested";
    private static final String TEXTAREA_SELECTOR = " textarea";
    private static final String LABEL_SELECTOR_FORMAT_STRING = "jquery=%s ul.items li[title~=%s] button";

    private static final int CF_TAGS_ID = 10000;
    private static final long MKY_1_ISSUE_ID = 10001;

    public static Test suite()
    {
        return suiteFor(TestEditLabelsPicker.class);
    }

    public void testLabelPickersAppearInCreateIssue()
    {
        restoreData("TestEditLabels.xml");

        // go to the create issue form
        client.click("id=create_link");
        assertThat.visibleByTimeout("quick-create-button", DROP_DOWN_WAIT);
        client.click("id=quick-create-button", true);
        assertLabelsAndCustomFieldsUseLabelPicker();
    }

    @Flaky
    /**
     * This test is flaky, after clicking on the dropdown suggestions don't appear "sometimes"
     * see assertLabelsSystemFieldUsingLabelPicker
     */
    public void testLabelPickersAppearInEditIssue()
    {
        restoreData("TestEditLabels.xml");

        getNavigator().editIssue("MKY-1");
        assertLabelsAndCustomFieldsUseLabelPicker();
    }

    public void testLabelPickersInEditLabelsPage()
    {
        restoreData("TestEditLabels.xml");
        getNavigator().gotoPage(String.format("secure/EditLabels!default.jspa?id=%s", MKY_1_ISSUE_ID), true);
        assertLabelsSystemFieldUsingLabelPicker();
        getNavigator().gotoPage(String.format("secure/EditLabels!default.jspa?id=%d&customFieldId=%d&noLink=false", MKY_1_ISSUE_ID, CF_TAGS_ID), true);
        assertLabelsCustomFieldUsingLabelPicker();
    }


    public void testLabelsInBulkEdit() throws Exception
    {
        restoreData("TestEditLabels.xml");

        BulkChangeWizard wizard = getNavigator().findIssuesWithJql("summary ~ '\"Too much\"'")
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperations.EDIT)
                .checkActionForField("labels")
                .checkActionForField("customfield_10000");

        assertLabelsAndCustomFieldsUseLabelPicker();

        // finish the bulk editing and assert values are now set on issue
        wizard.finaliseFields().complete();
        client.clickLinkWithText("Too much", true);
        assertThat.elementContainsText("css=#customfield_10000-10001-value", "paul");

        // also assert that the values previously chosen for the Labels system field are retained
        assertThat.elementContainsText("css=#labels-10001-value", "mouse");
    }

    public void testLabelsInBulkMove() throws Exception
    {
        // note that Labels System Field can never be "bulk moved" as it is not a "requirable" field.
        restoreData("TestEditLabelsBulkMove.xml");
        
        BulkChangeWizard wizard = getNavigator().findIssuesWithJql("summary ~ '\"Too much\"'")
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperations.MOVE)
                .chooseTargetContextForAll("homosapien");

        // try to continue without entering a value
        wizard.finaliseFields();
        assertThat.elementContainsText("css=#customfield_10000_container", "Tags is required.");

        assertLabelsCustomFieldUsingLabelPicker();

        // label "paul" should now be selected. complete wizard and assert the new value is chosen
        wizard.finaliseFields().complete();
        client.clickLinkWithText("Too much", true);
        assertThat.elementContainsText("css=#customfield_10000-10001-value", "paul");

        // also assert that the values previously chosen for the Labels system field are retained
        assertThat.elementContainsText("css=#labels-10001-value", "keyboard");
        assertThat.elementContainsText("css=#labels-10001-value", "gadgets");
    }

    private void assertLabelsAndCustomFieldsUseLabelPicker()
    {
        assertLabelsSystemFieldUsingLabelPicker();
        assertLabelsCustomFieldUsingLabelPicker();
    }

    private void assertLabelsSystemFieldUsingLabelPicker()
    {
        // verify suggestions appear
        client.click(dropMenuForLabels());
        assertThat.elementPresentByTimeout(suggestionsForLabels(), 1000); 
        assertThat.elementContainsText(suggestionsForLabels(), "gadgets");
        assertThat.elementContainsText(suggestionsForLabels(), "keyboard");
        client.keyPress("jquery=body", ESCAPE_KEY);

        // create a label
        client.typeWithFullKeyEvents(textAreaForLabels(), "mouse");
        waitFor(400); // Need the wait as the JS doesn't execute fast enough
        client.keyPress(textAreaForLabels(), ENTER_KEY);
        assertLabelSelectedForLabels("mouse");
    }

    private void assertLabelsCustomFieldUsingLabelPicker()
    {
        // verify suggestions appear (custom field)
        client.click(dropMenuForCustomField(CF_TAGS_ID));
        assertThat.elementPresentByTimeout(suggestionsForCustomField(CF_TAGS_ID), 1000); 
        assertThat.elementContainsText(suggestionsForCustomField(CF_TAGS_ID), "john");
        assertThat.elementContainsText(suggestionsForCustomField(CF_TAGS_ID), "ringo");
        client.keyPress("jquery=body", ESCAPE_KEY);

        // create a label (custom field)
        client.typeWithFullKeyEvents(textAreaForCustomField(CF_TAGS_ID), "paul");
        waitFor(400); // Need the wait as the JS doesn't execute fast enough
        client.keyPress(textAreaForCustomField(CF_TAGS_ID), ENTER_KEY);
        assertLabelSelectedForCustomField(CF_TAGS_ID, "paul");
    }

    private static String dropMenuForLabels()
    {
        return JQUERY_LOCATOR + containerForLabels() + DROP_MENU_SELECTOR;
    }

    private static String dropMenuForCustomField(long customFieldId)
    {
        return JQUERY_LOCATOR + containerForCustomField(customFieldId) + DROP_MENU_SELECTOR;
    }

    private static String suggestionsForLabels()
    {
        return JQUERY_LOCATOR + "#labels-suggestions" + SUGGESTIONS_LIST_SELECTOR;
    }

    private static String suggestionsForCustomField(long customFieldId)
    {
        return JQUERY_LOCATOR + "#customfield_" + customFieldId + "-suggestions" + SUGGESTIONS_LIST_SELECTOR;
    }

    private static String textAreaForLabels()
    {
        return JQUERY_LOCATOR + containerForLabels() + TEXTAREA_SELECTOR;
    }

    private static String textAreaForCustomField(long customFieldId)
    {
        return JQUERY_LOCATOR + containerForCustomField(customFieldId) + TEXTAREA_SELECTOR;
    }

    private static String containerForLabels()
    {
        return "#labels-multi-select";
    }

    private static String containerForCustomField(long customFieldId)
    {
        return "#customfield_" + customFieldId + "-multi-select";
    }

    private void assertLabelSelectedForLabels(String label)
    {
        assertThat.elementPresent(String.format(LABEL_SELECTOR_FORMAT_STRING, containerForLabels(), label));
    }

    private void assertLabelSelectedForCustomField(long customFieldId, String label)
    {
        assertThat.elementPresent(String.format(LABEL_SELECTOR_FORMAT_STRING, containerForCustomField(customFieldId), label));
    }
}
