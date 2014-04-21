package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests the gadget's "include resolved issues" preference
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestWatchedIssuesGadgetResolvedIssues extends GadgetTest
{
    @Override
    public void onSetUp()
    {
        internalSetup();
        restoreData("TestWatchedIssuesGadgetResolved.xml");
    }

    public void testTogglePreference() throws Exception
    {
        selectGadget("Watched Issues");
        waitForGadgetView("watched-content");
        clickConfigButton();
        waitForGadgetConfiguration();
        getSeleniumClient().check("showResolved", "true");
        submitGadgetConfig();

        // assert that the gadget shows the correct number of results
        waitForGadgetView("watched-content");
        assertThat.linkPresentWithText("15 matching issues.");

        // assert that following the link to the navigator executes the same search as the gadget
        client.clickLinkWithText("15 matching issues.", true);
        assertThat.textPresentByTimeout("15 matching issues", 5000);

        getNavigator().gotoHome();
        selectGadget("Watched Issues");
        waitForGadgetView("watched-content");
        clickConfigButton();
        waitForGadgetConfiguration();
        getSeleniumClient().uncheck("showResolved");
        submitGadgetConfig();

        // assert that the gadget shows the correct number of results
        waitForGadgetView("watched-content");
        assertThat.linkPresentWithText("12 matching issues.");

        // assert that following the link to the navigator executes the same search as the gadget
        client.clickLinkWithText("12 matching issues.", true);
        assertThat.textPresentByTimeout("12 matching issues", 5000);
    }
}
