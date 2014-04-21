package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

/**
 * Selenium test for the Issue Statistics gadget
 */
@Quarantine
@SkipInBrowser(browsers={Browser.IE}) //JS Error XSS Protection - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestStatsGadget extends StatsTestBase
{
    @Override
    public void onSetUp()
    {
        super.onSetUp();
        addGadget("Issue Statistics");
    }

    public void testConfiguration()
    {
        waitForGadgetConfiguration();
        assertThat.textPresent("Project or Saved Filter:");
        // select homosapien
        selectProjectOrFilterFromAutoComplete("quickfind", "homo", "project-10000");
        // assert "homosapien" appears in suggestion list when typing hom
        assertAutoComplete("id=quickfind", "h", // should match both the name and the key
                "jquery=h5:contains(Projects)", // project group heading should be visible
                "jquery=ul > li > a > strong:contains(h)", // "h" should be highlighted
                "jquery=ul > li > a:contains(homosapien)",  // "homosapien" should be visible
                "jquery=ul > li > a:contains(HSP)"); // HSP should be visible
        // assert "monkey" appears in suggestion list when typing mky
        assertAutoComplete("id=quickfind", "mky", // should match both the name and the key
                "jquery=h5:contains(Projects)", // project group heading should be visible
                "jquery=ul > li > a > strong",  // some text should be highlighted
                "jquery=ul > li > a:contains(monkey)", // "monkey" should be visible
                "jquery=ul > li > a:contains(MKY)"); // MKY should be visible
        // assert homosapien is selected
        assertThat.textPresent(HOMOSAPIEN);

        assertThat.textPresent("Statistic Type");
        assertThat.textPresent("Select which type of statistic to display for this project or saved filter");
        assertThat.textPresent("Assignee");
        assertThat.textPresent("Components");
        assertThat.textPresent("Issue");
        assertThat.textPresent("Type");
        assertThat.textPresent("Fix For Versions (non-archived)");
        assertThat.textPresent("Fix For Versions (all)");
        assertThat.textPresent("Priority");
        assertThat.textPresent("Project");
        assertThat.textPresent("Raised In Versions (non-archived)");
        assertThat.textPresent("Raised In Versions (all)");
        assertThat.textPresent("Reporter");
        assertThat.textPresent("Resolution");
        assertThat.textPresent("Status");

        assertRefreshIntervalFieldPresent();

        assertSelectFieldError("statType", "IamAnInvalidStatType", "Invalid Statistics Type");
    }

    public void testAssignee()
    {
        getWebUnitTest().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getWebUnitTest().addUserToGroup(FRED_USERNAME, JIRA_DEVELOPERS);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, null, null, FRED_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, null, null, ADMIN_NAME, null, null, null, null, null);

        configGadget(HOMOSAPIEN, "project-10000", "Assignee", 0);

        assertThat.elementPresentByTimeout("//li[@class='stats-row']", 10000);
        assertThat.elementContainsText("xpath=(//div[@class='stats-percentage'])[1]", "50%");
        assertThat.elementContainsText("xpath=(//div[@class='stats-percentage'])[2]", "50%");
        assertThat.linkPresentWithText(HOMOSAPIEN);
        assertThat.linkPresentWithText(ADMIN_NAME);
        assertThat.linkPresentWithText(FRED_NAME);
    }

    //JRA-19177
    public void testFilterLinksToSavedFilterInNavigator()
    {
        getWebUnitTest().gotoIssue("");
        getWebUnitTest().saveFilter("testFilter", "some desc");
        
        waitForGadgetConfiguration();
        //configure the gadget with a filter such that the filter wont have to be modified.
        selectProjectOrFilterFromAutoComplete("quickfind", "test", "filter-10000");
        client.select("includeResolvedIssues", "Yes");
        client.click("css=input.button.save");
        assertThat.textPresentByTimeout("Total Issues:", 10000);
        assertThat.linkPresentWithText("testFilter");
        client.click("stats-gadget-project-or-filter-link", true);

        assertThat.elementContainsText("filter-description", "testFilter");       
    }

}
