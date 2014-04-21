package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.webtest.framework.core.TimedAssertions;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import org.apache.log4j.Logger;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.by;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators.css;
import static com.atlassian.webtest.ui.keys.Sequences.chars;

/**
 * Base class for selenium testing JIRA gadgets.
 *
 * @since v4.0
 */
public abstract class GadgetTest extends JiraSeleniumTest
{
    protected static final int TIMEOUT = 40000;
    protected static final int GADGET_DIRECTORY_TIMEOUT = 60000;

    private static final Logger log = Logger.getLogger(GadgetTest.class);
    private static final String GADGET_TITLE_LOCATOR = "//h3[@class='dashboard-item-title']";


    protected String title;
    protected Locator openDropdownLocator;

    /**
     * Loads minimal starting data for gadget testing and does an admin login.
     */
    public void onSetUp()
    {
        super.onSetUp();
        this.openDropdownLocator = css("#quickfind-container .suggestions.dropdown-ready", context());
        restoreGadgetData();
    }

    @Override
    protected void onTearDown() throws Exception
    {
        //make sure we're not stuck in a gadget iframe at the end of the test!
        client.selectWindow(null);
        super.onTearDown();
    }

    protected void restoreGadgetData()
    {
        restoreData("BaseGadgetData.xml");
    }

    protected void loginAsAdmin()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    /**
     * Adds a gadget with the given name using the gadget directory. The gadget's frame is the currently selected frame
     * on exit from this method.
     *
     * @param gadgetTitle the proper title of the gadget, e.g. "Issues: Resolution Time"
     */
    public void addGadget(String gadgetTitle)
    {
        assertThat.elementPresentByTimeout("add-gadget", TIMEOUT);
        client.click("add-gadget");
        // This can occasionally take > 30s, so the normal TIMEOUT will not suffice
        assertThat.elementPresentByTimeout("category-all", GADGET_DIRECTORY_TIMEOUT);
        String pseudoId = "macro-" + gadgetTitle.replaceAll("\\W*", "");
        client.click("jquery=#" + pseudoId + " .macro-button-add");
        client.click("css=button.finish");
        assertThat.elementPresentByTimeout(GADGET_TITLE_LOCATOR, TIMEOUT);
        selectGadget(gadgetTitle);
    }

    /**
     * Adds a gadget with the given name using the gadget directory. The gadget's frame is the currently selected frame
     * on exit from this method.
     *
     * @param gadgetTitle the proper title of the gadget, e.g. "Issues: Resolution Time"
     * @param iframeTitle the title of the iframe
     */
    public void addGadget(String gadgetTitle, String iframeTitle)
    {
        assertThat.elementPresentByTimeout("add-gadget", TIMEOUT);
        client.click("add-gadget");
        // This can occasionally take > 30s, so the normal TIMEOUT will not suffice
        assertThat.elementPresentByTimeout("category-all", GADGET_DIRECTORY_TIMEOUT);
        String pseudoId = "macro-" + gadgetTitle.replaceAll("\\W*", "");
        client.click("jquery=#" + pseudoId + " .macro-button-add");
        client.click("css=button.finish");
        assertThat.elementPresentByTimeout(GADGET_TITLE_LOCATOR, TIMEOUT);
        selectGadget(iframeTitle);
    }

    /**
     * Sets a select field to a badValue, submits the form and asserts the expectedMessage is present.
     * <p/>
     * WARNING: the bad value (if not <code>null</code>) cannot be removed. So try to test all valid selections before
     * this one.
     *
     * @param field the name of the field.
     * @param badValue the value to give it that should produce the expectedError
     * @param expectedError the error message expected, if it does not occur, it's a failure.
     */
    public void assertSelectFieldError(String field, String badValue, String expectedError)
    {
        setSelectField(field, badValue);
        submitGadgetConfig();
        assertThat.elementPresentByTimeout("jquery=div.error:contains(" + expectedError + ")", TIMEOUT);

//        client.waitForCondition("selenium.browserbot.getCurrentWindow().jQuery('div.error:contains("
//                + expectedError + ")').length === 1", TIMEOUT);
    }

    /**
     * Sets a select field to the given value.
     * <p/>
     * WARNING: for any invalid value other than <code>null</code>, it is, to my knowledge, unable to remove the invaild
     * value.
     *
     * @param field the id or the name attribute of the field
     * @param value the option value (not label) to set, or <code>null</code> to remove all existing valid selections
     */
    protected void setSelectField(final String field, final String value)
    {
        if (value == null)
        {
            client.removeAllSelections(field);
        }
        else
        {
            client.getEval("this.browserbot.getCurrentWindow().jQuery('[name=" + field + "]').find('option').val('"
                    + value + "')");
        }
    }

    /**
     * Sets a (multi-) select field by label(s). It will first remove all previously selected labels.
     *
     * @param selectFieldLocator the Selenium locator to locate the select field
     * @param labels the labels to select. To remove all labels, don't pass this parameter.
     */
    protected void selectByLabels(final String selectFieldLocator, final String... labels)
    {
        client.removeAllSelections(selectFieldLocator);
        boolean noneSelected = true;
        for (String label : labels)
        {
            if (label != null)
            {
                if (noneSelected)
                {
                    client.select(selectFieldLocator, "label=" + label);
                    noneSelected = false;
                }
                else
                {
                    client.addSelection(selectFieldLocator, "label=" + label);
                }
            }
        }
    }

    /**
     * Sets a text field (or hidden field etc.) to the given value, submits the form and asserts the presence of the
     * given error.
     *
     * @param field name of the field.
     * @param badValue value to give the field to produce the error.
     * @param expectedError the error expected.
     */
    public void assertTextFieldError(String field, String badValue, String expectedError)
    {
        setTextField(field, badValue);
        submitGadgetConfig();
        assertThat.elementPresentByTimeout("jquery=div.error:contains(" + expectedError + ")", TIMEOUT);
//        client.waitForCondition("selenium.browserbot.getCurrentWindow().jQuery('div.error:contains("
//                + expectedError + ")').length === 1", TIMEOUT);
    }

    /**
     * Sets a text field (or hidden field etc.) to the given value.
     *
     * @param field name of the field.
     * @param value value to give the field.
     */
    protected void setTextField(final String field, final String value)
    {
        client.getEval("this.browserbot.getCurrentWindow().jQuery('[name=" + field + "]').val('" + value + "')");
    }

    /**
     * Asserts that the given option labels are present for the given field.
     *
     * @param field the field.
     * @param labels the labels to assert are present.
     */
    protected void assertFieldOptionLabelsPresent(final String field, final String[] labels)
    {
        for (String label : labels)
        {
            log.debug("checking that " + field + " has label " + label);
            assertThat.elementPresentByTimeout("jquery=:input[name=" + field + "] option:contains(" + label + ")");
        }
    }

    /**
     * Asserts that the given option values are present for the given field.
     *
     * @param field the field.
     * @param optionValues the values to assert are present.
     */
    public void assertFieldOptionValuesPresent(final String field, final String[] optionValues)
    {
        for (String optionValue : optionValues)
        {
            log.debug("checking that " + field + " has label " + optionValue);
            client.waitForCondition("selenium.browserbot.getCurrentWindow().jQuery(':input[name=" + field + "] > option[value=" + optionValue + "]').length === 1", TIMEOUT);
        }
    }

    public void assertFieldOptionValuePresent(final String field, final String optionValue)
    {
        assertFieldOptionValuesPresent(field, new String[] { optionValue });
    }

    /**
     * Waits for a gadget to load by waiting for its configuration form
     */
    protected void waitForGadgetConfiguration()
    {
        visibleByTimeoutWithDelay("css=form.aui",TIMEOUT);
    }

    protected void waitForGadgetView(String id)
    {
        visibleByTimeoutWithDelay("//div[@id='" + id + "']", TIMEOUT);


    }

    protected void selectGadget(String gadgetTitle)
    {
        final String frameId = client.getEval("this.browserbot.getCurrentWindow().jQuery(\"div.dashboard h3:contains('"
                + gadgetTitle + "')\").closest('.dashboard-item-frame').find('iframe').attr('id')");
        client.selectFrame(frameId);
    }

    /**
     * Submits the "form" that is the gadget config view.
     */
    protected void submitGadgetConfig()
    {
        client.click("//div[@class='buttons']/input[1]"); // gadget save config
    }

    /**
     * Asserts that the 'Refresh Interval' select field and all its options are present
     */
    protected void assertRefreshIntervalFieldPresent()
    {
        assertThat.textPresent("Refresh Interval");
        assertThat.textPresent("How often you would like this gadget to update");
        assertThat.textPresent("Never");
        assertThat.textPresent("Every 15 Minutes");
        assertThat.textPresent("Every 30 Minutes");
        assertThat.textPresent("Every 1 Hour");
        assertThat.textPresent("Every 2 Hours");
    }

    /**
     * Asserts that the 'Period' select field and all its options are present
     */
    protected void assertPeriodFieldPresent()
    {
        assertThat.textPresent("Period");
        assertThat.textPresent("The length of periods represented on the graph.");
        assertThat.textPresent("Hourly");
        assertThat.textPresent("Daily");
        assertThat.textPresent("Weekly");
        assertThat.textPresent("Monthly");
        assertThat.textPresent("Quarterly");
        assertThat.textPresent("Yearly");
    }

    /**
     * Asserts that the 'Days Previously' text field is present
     */
    protected void assertDaysPreviouslyFieldPresent()
    {
        assertThat.textPresent("Days Previously");
        assertThat.textPresent("Days (including today) to show in the graph.");
        assertTextFieldError("daysprevious", "InvalidDay", "Days must be a number");
    }

    /**
     * Maximizes the gadget if it isn't already
     *
     * @param gadgetTitle the gadget to maximize.
     */
    protected void maximizeGadget(String gadgetTitle)
    {
        selectDashboardFrame();
        waitFor(5000);
        String minimizeLocator = "id=disabled-menu-link";
        if (!client.isVisible(minimizeLocator))
        {
            client.click("class=aui-icon maximize");
            assertThat.elementPresentByTimeout(minimizeLocator, TIMEOUT);
        }
        selectGadget(gadgetTitle);
    }

    /**
     * Minimizes the gadget if it isn't already
     *
     * @param gadgetTitle the gadget to minimize.
     */
    protected void minimizeGadget(String gadgetTitle)
    {
        selectDashboardFrame();
        client.refresh();
        String minimizeLocator = "id=disabled-menu-link";
        if (!client.isElementPresent(minimizeLocator))
        {
            client.click("class=aui-icon maximize");
            assertThat.elementPresentByTimeout(minimizeLocator, TIMEOUT);
        }
        selectGadget(gadgetTitle);
    }

    protected void selectDashboardFrame()
    {
        getSeleniumClient().selectFrame("relative=top");
    }

    /**
     * Deletes a gadget if it exists. Returns frame focus to top.
     *
     * @param gadgetTitle the gadget to delete.
     */
    protected void deleteGadget(String gadgetTitle)
    {
        final String gadgetDivId = client.getEval("this.browserbot.getCurrentWindow().jQuery('div.dashboard h3:contains("
                + gadgetTitle + ")').parents('.gadget').attr('id')");

        if (gadgetDivId != null)
        {
            client.click("css=#" + gadgetDivId + " a.delete");
            client.getConfirmation();
        }

        assertThat.elementNotPresentByTimeout("id=" + gadgetDivId, TIMEOUT);
    }

    /**
     * Clicks the gadget configure button
     *
     * @return true if the button is found and clicked (gadget view was shown), or false if the button is not found,
     *         presumably already showing the configuration view
     */
    protected boolean clickConfigButton()
    {
        return clickMenuButton("configure");
    }

    /**
     * Clicks the gadget configure button
     *
     * @return true if the button is found and clicked (gadget view was shown), or false if the button is not found,
     *         presumably already showing the configuration view
     */
    protected boolean clickRefreshButton()
    {
        return clickMenuButton("reload");
    }

    private boolean clickMenuButton(String classIdentifier)
    {
        String currentWindowName = client.getEval("this.browserbot.getCurrentWindow().name");

        // selects parent window
        selectDashboardFrame();

        // save current window
        String configButtonLocator = "css=#" + currentWindowName + "-renderbox ." + classIdentifier + " a";

        waitFor(100); // todo Fix this crapness

        if (client.isElementPresent(configButtonLocator))
        {
            client.click(configButtonLocator);
            client.selectFrame(currentWindowName);
            return true;
        }
        client.selectFrame(currentWindowName);
        return false;
    }

    /**
     * Asserts the title of the gadget.
     *
     * @param expectedTitle the expected title of the gadget
     */
    protected void assertGadgetTitle(final String expectedTitle)
    {
        // select the top window to locate the title
        selectDashboardFrame();
        assertThat.elementContainsText(GADGET_TITLE_LOCATOR, expectedTitle);
        // select the gadget iframe for the rest of the test
        selectGadget(expectedTitle);
    }

    /**
     * Asserts that there is no gadget with the title specified
     *
     * @param expectedTitle the expected title of the gadget
     */
    protected void assertGadgetNotVisible(final String expectedTitle)
    {
        // select the top window to locate the title
        selectDashboardFrame();
        assertThat.elementDoesntHaveText(GADGET_TITLE_LOCATOR, expectedTitle);
    }

    protected String getBaseUrl()
    {
        final String baseUrl = getWebUnitTest().getTestContext().getBaseUrl();
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    protected void selectProjectOrFilterFromAutoComplete(String field, String hint, String id)
    {
        final String clickTarget = "id=" + id + "_" + field + "_listitem";
        assertAutoComplete("id=" + field, hint, clickTarget);
        client.click(clickTarget);
    }

    /**
     * Asserts autocompletion field.
     * <p/>
     * Note that the hint should contain only 1 character if you need to assert the exact matching character sequence.
     * For example, if you type "homo", the suggestion list may be created after you typed "h" and before you typed the
     * last "omo". So the highlighted matching character sequence is &lt;strong&gt;h&lt;strong&gt;.
     *
     * @param field the id or name of the field that is autocompleted
     * @param hint the text typed to trigger the suggestion list (at least 3 characters long)
     * @param autoCompleteLocators an array of Selenium locators that should be visible
     */
    protected void assertAutoComplete(final String field, final String hint, final String... autoCompleteLocators)
    {
        context().ui().clear(field).typeInLocator(field, chars(hint));
        if (autoCompleteLocators != null)
        {
            for (String locator : autoCompleteLocators)
            {
                final Locator toFind = SeleniumLocators.create(locator, context());
                assertThat(openDropdownLocator.combine(toFind).element().isPresent(), by(TIMEOUT));
            }
        }
    }

    protected void viewGadgetAsAnonymous(String gadgetTitle)
    {
        viewGadgetAsAnonymous(gadgetTitle, null);
    }

    protected void viewGadgetAsAnonymous(String gadgetTitle, Runnable configureGadget)
    {
        selectDashboardFrame();
        loginAsAdmin();
        getNavigator().gotoPage("secure/admin/jira/EditDefaultDashboard!default.jspa", true);
        addGadget(gadgetTitle);
        if (configureGadget != null)
        {
            configureGadget.run();
        }
        selectDashboardFrame();
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        selectGadget(gadgetTitle);
        assertThat.visibleByTimeout("css=div.view", TIMEOUT);
    }

}
