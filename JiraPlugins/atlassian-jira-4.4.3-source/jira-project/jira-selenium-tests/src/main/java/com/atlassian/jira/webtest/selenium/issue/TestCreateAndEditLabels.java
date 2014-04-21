package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.jira.webtest.selenium.framework.dialogs.EditLabelsDialog;
import com.atlassian.jira.webtest.selenium.framework.model.SubmitType;
import com.atlassian.selenium.pageobjects.PageElement;
import com.atlassian.webtest.ui.keys.SpecialKeys;
import junit.framework.Test;

import java.util.Arrays;
import java.util.List;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;

/**
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
@Quarantine
public class TestCreateAndEditLabels extends JiraSeleniumTest
{
    private static final String REALLY_LONG_LABEL = "reallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelrl";

    public static Test suite()
    {
        return suiteFor(TestCreateAndEditLabels.class);
    }

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestCreateAndEditLabels.xml");
    }


    private static String textAreaForLabels()
    {
        return "jquery=#labels-multi-select textarea";
    }

    private static String textAreaForCustomField(long customFieldId)
    {
        return "jquery=#customfield_" + customFieldId + "-multi-select textarea";
    }

    public void testCreateAndEditLabels()
    {
        _testLabelField(null, "Labels");
    }

    public void testCreateAndEditCustomField()
    {
        _testLabelField(10000L, "Epic");
    }

    public void testEditRequiredLabels()
    {
        getNavigator().gotoAdmin();
        client.click("field_configuration", true);
        client.clickLinkWithText("Default Field Configuration", true);

        //make both the epic & labels system field required
        client.click("require_8", true);
        client.click("require_11", true);

        //then try to edit an issue without labels
        getNavigator().issue().editIssue("HSP-1");
        //clear out all the labels for the system field first!
        removeLabels(textAreaForLabels(), 15);
        client.click("issue-edit-submit", true);
        assertThat.textPresent("Labels is required.");

        addLabel(textAreaForLabels(), "something");

        //clear out all the labels for the CF field!
        removeLabels(textAreaForCustomField(10000L), 15);
        client.click("issue-edit-submit", true);
        assertThat.textPresent("Epic is required.");

        addLabel(textAreaForCustomField(10000L), "else");
        client.click("issue-edit-submit", true);

        final EditLabelsDialog labelsDialog = new EditLabelsDialog(context());
        labelsDialog.openFromViewIssue();
        removeLabels(textAreaForLabels(), 15);
        labelsDialog.submit(SubmitType.BY_CLICK, false);
        assertThat.textPresentByTimeout("Labels is required.", DROP_DOWN_WAIT);
        labelsDialog.closeByEscape();

    }

    private void _testLabelField(Long customFieldId, String fieldName)
    {
        String textAreaSelector;
        String issueTableCssClass;
        if (customFieldId == null)
        {
            textAreaSelector = textAreaForLabels();
            issueTableCssClass = "labels";
        }
        else
        {
            textAreaSelector = textAreaForCustomField(customFieldId);
            issueTableCssClass = "customfield_" + customFieldId;
        }

        final String firstKey = getNavigator().createIssue("homosapien", "Bug", "First test issue");
        final int firstIssueId = Integer.valueOf(client.getAttribute("jquery=#key-val @rel"));
        final String secondKey = getNavigator().createIssue("homosapien", "Bug", "Second test issue");
        final int secondIssueId = Integer.valueOf(client.getAttribute("jquery=#key-val @rel"));

        getNavigator().gotoIssue(firstKey);
        assertEquals("First test issue", new PageElement("jquery=#issue_header_summary", client).getText());

        assertNoLabelsPresentOnIssuePage(firstIssueId, customFieldId);

        //edit the issue and add labels
        getNavigator().editIssue(firstKey);
        addLabels(textAreaSelector, "some", "initial", "labels");
        client.click("issue-edit-submit", true);
        assertLabelsPresentOnIssuePage(firstIssueId, customFieldId, "initial", "labels", "some");

        //try adding a duplicate label
        getNavigator().editIssue(firstKey);
        addLabels(textAreaSelector, "labels");
        client.click("issue-edit-submit", true);
        assertLabelsPresentOnIssuePage(firstIssueId, customFieldId, "initial", "labels", "some");

        //test sorting
        getNavigator().editIssue(firstKey);
        addLabels(textAreaSelector, "anotherw");
        client.click("issue-edit-submit", true);
        assertLabelsPresentOnIssuePage(firstIssueId, customFieldId, "anotherw", "initial", "labels", "some");

        //edit labels on another issue
        getNavigator().gotoIssue(secondKey);
        assertEquals("Second test issue", new PageElement("jquery=#issue_header_summary", client).getText());
        assertNoLabelsPresentOnIssuePage(secondIssueId, customFieldId);

        getNavigator().editIssue(secondKey);
        addLabels(textAreaSelector, "dude", "foo", "xoo");
        client.click("issue-edit-submit", true);
        assertLabelsPresentOnIssuePage(secondIssueId, customFieldId, "dude", "foo", "xoo");

        //configure the navigator for the field!
        getNavigator().gotoPage("/secure/ViewUserIssueColumns!default.jspa", true);
        List<String> columnHeadings = Arrays.asList(new PageElement("jquery=#issuetable .rowHeader", client).getText().split("\\s"));
        if (!columnHeadings.contains(fieldName))
        {
            client.selectOption("fieldId", fieldName);
            client.click("jquery=#issue-nav-add-columns-submit", true);
        }
        columnHeadings = Arrays.asList(new PageElement("jquery=#issuetable .rowHeader", client).getText().split("\\s"));
        assertTrue(columnHeadings.contains(fieldName));

        //now check teh navigator is showing labels.
        getNavigator().findAllIssues();
        columnHeadings = Arrays.asList(new PageElement("jquery=#issuetable .rowHeader", client).getText().split("\\s"));
        assertTrue(columnHeadings.contains(fieldName));
        // TODO Find a way to enforce the order of these labels.
        assertThat.elementPresent("jquery=#issuetable td." + issueTableCssClass + ":contains('dude'):contains('foo'):contains('xoo')");
        assertThat.elementPresent("jquery=#issuetable td." + issueTableCssClass + ":contains('anotherw'):contains('initial'):contains('labels'):contains('some')");

        // validation of long labels
        getNavigator().editIssue(firstKey);
        addLabel(textAreaSelector, REALLY_LONG_LABEL);
        client.click("issue-edit-submit", true);
        assertTooLongLabelError();
    }

    private void addLabels(String textAreaSelector, String firstLabel, String... restLabels)
    {
        addLabel(textAreaSelector, firstLabel);
        for (String l : restLabels)
        {
            addLabel(textAreaSelector, l);
        }
    }

    private void addLabel(String textAreaSelector, String label)
    {
        context().ui().typeCharsFast(textAreaSelector, label);
        context().ui().typeInLocator(textAreaSelector, SpecialKeys.SPACE);
        assertHasLabelLozenge(textAreaSelector, label);
    }

    private void assertHasLabelLozenge(String textAreaSelector, String value)
    {
        assertThat.elementPresentByTimeout(textAreaSelector + " ~ div.representation li.item-row[title=" + value + "]",
                timeouts().timeoutFor(Timeouts.UI_ACTION));
    }

    private void removeLabels(String selector, int numLabels)
    {
        for(int i = 0; i< numLabels ;i ++) {
            context().ui().typeInLocator(selector, SpecialKeys.BACKSPACE);
            // TODO replace it with checking lozenge count
            // TODO really replace it with page objects (along with the rest of this test:)
            waitFor(200);
        }
    }

    private void assertTooLongLabelError()
    {
        assertThat.elementHasText("jquery=.aui-field-labelpicker .error", "The label '" + REALLY_LONG_LABEL
                + "' exceeds the maximum length for a single label of 255 characters.");
    }

    private void assertLabelsPresentOnIssuePage(int issueId, Long customFieldId, String firstExpectedLabel, String... restExpectedLabels)
    {
        final String firstExpectedLabelSelector;
        if (firstExpectedLabel == null)
        {
            firstExpectedLabelSelector = labelsLozengesLocator(issueId, customFieldId);
        }
        else
        {
            firstExpectedLabelSelector = labelsLozengesLocatorWithLabel(issueId, customFieldId, firstExpectedLabel);
        }
        Locator locator = SeleniumLocators.jQuery(firstExpectedLabelSelector, context());
        assertThat(locator.element().isPresent(), byDefaultTimeout());

        for (String expectedLabel : restExpectedLabels)
        {
            String restExpectedLabelSelector = labelsLozengesLocatorWithLabel(issueId, customFieldId, expectedLabel);
            Locator restLocator = SeleniumLocators.jQuery(restExpectedLabelSelector, context());
            assertThat(restLocator.element().isPresent(), byDefaultTimeout());
        }
    }

    private String labelsLozengesLocator(final long issueId, final Long customFieldId)
    {
        final String labelsLozengesLocatorFormatString = "#%s-%d-value";
        return String.format(labelsLozengesLocatorFormatString, customFieldId == null ? "labels" : "customfield_" + customFieldId, issueId);
    }

    private String labelsLozengesLocatorWithLabel(final long issueId, final Long customFieldId, final String label)
    {
        final String labelsLozengesLocatorFormatString = "#%s-%d-value li a.lozenge[title='%s'] span";
        return String.format(labelsLozengesLocatorFormatString, customFieldId == null ? "labels" : "customfield_" + customFieldId, issueId, label);
    }

    private void assertNoLabelsPresentOnIssuePage(int issueId, Long customFieldId)
    {
        if (customFieldId != null)
        {
            final String locator = labelsLozengesLocator(issueId, customFieldId);
            assertThat.elementNotPresent(locator);
        }
        else
        {
            assertLabelsPresentOnIssuePage(issueId, customFieldId, null);
        }
    }
}
