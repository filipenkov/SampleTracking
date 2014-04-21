package com.atlassian.jira.webtest.selenium.auidialog.labels;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.framework.components.LabelsPicker;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

/**
 * @since v4.2
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestEditLabelsInIssueNavigator extends AbstractAuiLabelsDialogTest
{
    private static final String LABEL_HOLDER_SELECTOR_FORMAT = "jquery=tr#issuerow10001 td.labels ul.labels a:contains('%s')";
    private static final String LOC_LABELS = "jquery=#actions_10001_drop a.issueaction-edit-labels";
    private static final String LOC_COG = "id=actions_10001";

    public static Test suite()
    {
        return suiteFor(TestEditLabelsInIssueNavigator.class);
    }

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().findAllIssues();
        setCurrentLabelsPicker(LabelsPicker.newSystemLabelsPicker(VISIBLE_DIALOG_CONTENT_SELECTOR, context()));
    }

    private void openLabelsDialogFromOperationsMenu()
    {
        client.click(LOC_COG);
        assertThat.elementPresentByTimeout(LOC_LABELS, DEFAULT_TIMEOUT);
        client.click(LOC_LABELS);
        assertLabelsDialogOpen();
    }

    public void testAddLabelFromOperationsMenu()
    {
        openLabelsDialogFromOperationsMenu();
        addLabelAndAssert("frommenu");
    }

    public void testRemoveAllExistingLabels()
    {
        openLabelsDialogFromOperationsMenu();
        assertNoLabelsInDialog();
        addLabelAndAssert("one");
        openLabelsDialogFromOperationsMenu();
        addLabelAndAssert("two");
        getNavigator().findAllIssues();
        assertLabelSelectedInIssueNavPage("one");
        assertLabelSelectedInIssueNavPage("two");
        openLabelsDialogFromOperationsMenu();
        removeLabels(2);
        submitDialogAndWaitForAjax();
        assertLabelNotSelectedInIssueNavPage("one");
        assertLabelNotSelectedInIssueNavPage("two");
    }

    //JRADEV-2404
    public void testAddMultipleLabelsShowsOneMessage()
    {
        openLabelsDialogFromOperationsMenu();
        addLabelAndAssert("one");

        assertThat.visibleByTimeout("jquery=#affectedIssueMsg", DROP_DOWN_WAIT);
        int numNotifications = Integer.parseInt(client.getEval("dom=this.browserbot.getCurrentWindow().jQuery(\"#affectedIssueMsg .aui-message.warning\").length"));
        assertEquals(1, numNotifications);

        openLabelsDialogFromOperationsMenu();
        addLabelAndAssert("two");

        assertThat.visibleByTimeout("jquery=#affectedIssueMsg", DROP_DOWN_WAIT);
        numNotifications = Integer.parseInt(client.getEval("dom=this.browserbot.getCurrentWindow().jQuery(\"#affectedIssueMsg .aui-message.warning\").length"));
        assertEquals(1, numNotifications);

        openLabelsDialogFromOperationsMenu();
        addLabelAndAssert("three");

        assertThat.visibleByTimeout("jquery=#affectedIssueMsg", DROP_DOWN_WAIT);
        numNotifications = Integer.parseInt(client.getEval("dom=this.browserbot.getCurrentWindow().jQuery(\"#affectedIssueMsg .aui-message.warning\").length"));
        assertEquals(1, numNotifications);
    }

    private void addLabelAndAssert(String label)
    {
        addLabel(label);
        submitDialogAndWaitForAjax();
        assertLabelSelectedInIssueNavPage(label);
    }

    private void assertLabelSelectedInIssueNavPage(String label)
    {
        assertThat.elementPresentByTimeout(String.format(LABEL_HOLDER_SELECTOR_FORMAT, label), 1000);
    }

    private void assertLabelNotSelectedInIssueNavPage(String label)
    {
        assertThat.elementNotPresentByTimeout(String.format(LABEL_HOLDER_SELECTOR_FORMAT, label), 1000);
    }
}
