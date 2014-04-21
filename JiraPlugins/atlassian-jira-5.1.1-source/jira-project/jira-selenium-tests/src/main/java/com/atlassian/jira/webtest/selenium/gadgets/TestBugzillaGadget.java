package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Selenium test for the Bugzilla gadget.
 *
 * @since v4.0
 */

@WebTest({Category.SELENIUM_TEST })
public class TestBugzillaGadget extends GadgetTest
{
    public void onSetUp()
    {
        super.onSetUp();
        addGadget("Bugzilla Issue ID Search");
    }

    @Override
    protected void restoreData(final String file)
    {
        super.restoreData("TestBugzillaGadget.xml");
    }

    public void testBugzillaGadget()
    {
        _testConfiguration();
        _testIssueInJira();
        _testIssueNotInJira();
//      No longer able to enter no url into the gadget configurations
//        _testIssueNotInJiraAndNoUrl();
    }

    public void _testConfiguration()
    {
        clickConfigButton();
        waitForGadgetConfiguration();
        assertThat.textPresent("Bugzilla URL");
        assertThat.elementPresent("id=bugzillaUrl");
    }

    public void _testIssueInJira()
    {
        configureGadget("http://");

        assertThat.linkPresentWithText("Bugzilla");
        assertThat.textPresent("Issue ID");
        assertThat.elementPresent("//input[@id='bugId']"); //The text field
        client.type("bugId", "3");

        client.click("//form[@id='bugzilla-search-form']//input[@type='submit']");
        client.waitForPageToLoad();
        assertThat.linkPresentWithText("TES-1"); //Bugzilla issue id 3 should be linked to JIRA issue TES-1
    }

    public void _testIssueNotInJira()
    {
        String baseUrl = getWebUnitTest().getTestContext().getBaseUrl();
        final String bugzillaIssueId = "123";

        configureGadget(baseUrl);

        client.type("bugId", bugzillaIssueId);
        client.click("//form[@id='bugzilla-search-form']//input[@type='submit']");
        client.waitForPageToLoad();

        assertEquals(client.getEval("this.browserbot.getCurrentWindow().location"), baseUrl + "/show_bug.cgi?id=" + bugzillaIssueId);
    }

    public void _testIssueNotInJiraAndNoUrl()
    {
        final String bugzillaIssueId = "123";

        configureGadget("");

        client.type("bugId", bugzillaIssueId);
        client.click("//form[@id='bugzilla-search-form']//input[@type='submit']");
        assertThat.textPresentByTimeout("Issue not found", 10000);
    }

    private void configureGadget(String url)
    {
        gotoDashboard();
        clickConfigButton();
        waitForGadgetConfiguration();

        client.type("bugzillaUrl", url);
        submitGadgetConfig();
        waitForGadgetView("bugzilla-search");
    }

    private void gotoDashboard()
    {
        client.selectWindow(null);
        getNavigator().currentDashboard().view();
        selectGadget("Bugzilla Issue ID Search");
    }
}
