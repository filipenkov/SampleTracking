package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Version;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.VersionClient;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;
import net.sourceforge.jwebunit.WebTester;

/**
 * Selenium Test for the Heat Map Gadget.
 *
 * @since v4.1
 */
@SkipInBrowser(browsers={Browser.IE}) //JS Error - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestHeatMapGadget extends StatsTestBase
{
    private static final String HOMO_HINT = "homo";
    private static final String HOMO_ID = "project-10000";
    private static final String HEAT_MAP_GADGET_NAME = "Heat Map";

    public static Test suite()
    {
        return suiteFor(TestHeatMapGadget.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        addGadget(HEAT_MAP_GADGET_NAME);
        selectDashboardFrame();
    }

    public void testConfiguration() throws InterruptedException
    {
        selectGadget(HEAT_MAP_GADGET_NAME);

        waitForGadgetConfiguration();

        assertThat.textPresent("Project or Saved Filter:");

        assertThat.textPresent("Statistic Type:");
        assertThat.textPresent("Select which type of statistic to display for this filter.");
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

        assertThat.textPresent("Select which type of statistic to display for this filter.");

        assertRefreshIntervalFieldPresent();

        assertSelectFieldError("statType", "IamAnInvalidStatType", "Invalid Statistics Type");
    }

    public void testAssigneeChart()
    {
        getWebUnitTest().addUserToGroup(FRED_USERNAME, JIRA_DEVELOPERS);
        try
        {
            getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
            getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, null, null, FRED_NAME, null, null, null, null, null);
            getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, null, null, ADMIN_NAME, null, null, null, null, null);
        }
        finally
        {
            getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        }

        selectUnconfiguredGadget();
        configGadget(HOMO_HINT, HOMO_ID, "Assignee", 0);
        assertHeatMapPresent();

        assertHeatMapPresentAndValid(ADMIN_NAME, 1, "50", "assignee");
        assertHeatMapPresentAndValid(FRED_NAME, 1, "50", "assignee");
    }

    public void testComponentChart()
    {
        loginAsAdmin();
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, new String[] { NEW_COMPONENT_1 }, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, new String[] { NEW_COMPONENT_2 }, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, new String[] { NEW_COMPONENT_3 }, null, null, ADMIN_NAME, null, null, null, null, null);

        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, new String[] { NEW_COMPONENT_1, NEW_COMPONENT_3 }, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, new String[] { NEW_COMPONENT_1, NEW_COMPONENT_2 }, null, null, ADMIN_NAME, null, null, null, null, null);

        getWebUnitTest().addColumnToIssueNavigator(new String[] { JIRAWebTest.COMPONENTS_FIELD_ID });

        selectUnconfiguredGadget();
        configGadget(HOMO_HINT, HOMO_ID, "Components", 0);
        assertHeatMapPresent();

        assertHeatMapPresentAndValid(NEW_COMPONENT_1, 3, "37", COMPONENTS_COLUMN);
        assertHeatMapPresentAndValid(NEW_COMPONENT_2, 2, "25", COMPONENTS_COLUMN);
        assertHeatMapPresentAndValid(NEW_COMPONENT_3, 2, "25", COMPONENTS_COLUMN);
        assertHeatMapPresentAndValid("No component", 1, "12", COMPONENTS_COLUMN);
    }

    public void testIssueTypeChart()
    {
        loginAsAdmin();
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, TYPE_BUG, BLARGH, null, new String[] { NEW_COMPONENT_1 }, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, TYPE_NEW_FEATURE, BLARGH, null, new String[] { NEW_COMPONENT_2 }, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, TYPE_TASK, BLARGH, null, new String[] { NEW_COMPONENT_3 }, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, TYPE_IMPROVEMENT, BLARGH, null, new String[] { NEW_COMPONENT_3 }, null, null, ADMIN_NAME, null, null, null, null, null);

        selectUnconfiguredGadget();
        configGadget(HOMO_HINT, HOMO_ID, "Issue Type", 0);
        assertHeatMapPresent();

        assertHeatMapPresentAndValid(TYPE_BUG, 1, "25", ISSUETYPE_COLUMN);
        assertHeatMapPresentAndValid(TYPE_NEW_FEATURE, 1, "25", ISSUETYPE_COLUMN);
        assertHeatMapPresentAndValid(TYPE_TASK, 1, "25", ISSUETYPE_COLUMN);
        assertHeatMapPresentAndValid(TYPE_IMPROVEMENT, 1, "25", ISSUETYPE_COLUMN);
    }

    public void testFixForVersionNonArchivedChart()
    {
        prepareFixForVersionTest();

        selectUnconfiguredGadget();
        configGadget(HOMO_HINT, HOMO_ID, "Fix For Versions (non-archived)", 0);
        assertHeatMapPresent();

        assertHeatMapPresentAndValid(NEW_VERSION_4, 2, "50", FIX_VERSIONS_COLUMN);
        assertHeatMapPresentAndValid(NEW_VERSION_5, 2, "50", FIX_VERSIONS_COLUMN);
    }

    public void testFixForVersionAllChart()
    {
        prepareFixForVersionTest();

        selectUnconfiguredGadget();
        configGadget(HOMO_HINT, HOMO_ID, "Fix For Versions (all)", 0);
        assertHeatMapPresent();

        assertHeatMapPresentAndValid(NEW_VERSION_4, 2, "40", FIX_VERSIONS_COLUMN);
        assertHeatMapPresentAndValid(NEW_VERSION_5, 2, "40", FIX_VERSIONS_COLUMN);
        assertHeatMapPresentAndValid(NEW_VERSION_1, 1, "20", FIX_VERSIONS_COLUMN);
    }

    public void testPriorityChart()
    {
        loginAsAdmin();
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, PRIORITY_BLOCKER, null, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, PRIORITY_CRITICAL, null, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, PRIORITY_MAJOR, null, null, null, ADMIN_NAME, null, null, null, null, null);

        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, PRIORITY_MINOR, null, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, PRIORITY_TRIVIAL, null, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, PRIORITY_BLOCKER, null, null, null, ADMIN_NAME, null, null, null, null, null);

        selectUnconfiguredGadget();
        configGadget(HOMO_HINT, HOMO_ID, "Priority", 0);
        assertHeatMapPresent();

        assertHeatMapPresentAndValid(PRIORITY_BLOCKER, 2, "33", PRIORITY_COLUMN);
        assertHeatMapPresentAndValid(PRIORITY_CRITICAL, 1, "16", PRIORITY_COLUMN);
        assertHeatMapPresentAndValid(PRIORITY_MAJOR, 1, "16", PRIORITY_COLUMN);
        assertHeatMapPresentAndValid(PRIORITY_MINOR, 1, "16", PRIORITY_COLUMN);
        assertHeatMapPresentAndValid(PRIORITY_TRIVIAL, 1, "16", PRIORITY_COLUMN);
    }

    public void testProjectChart()
    {
        loginAsAdmin();
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, null, null, ADMIN_NAME, null, null, null, null, null);

        getWebUnitTest().addIssueOnly(MONKEY, null, null, BLARGH, null, null, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(MONKEY, null, null, BLARGH, null, null, null, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(MONKEY, null, null, BLARGH, null, null, null, null, ADMIN_NAME, null, null, null, null, null);

        getWebUnitTest().addColumnToIssueNavigator(new String[] { "Project" });

        // Create a filter named "blargh"
        client.selectFrame("relative=top");
        getNavigator().findAllIssues();
        client.click("id=filtersavenew", true);
        client.type("filterName", BLARGH);
        client.click("submit", true);

        selectUnconfiguredGadget();
        configGadget(BLARGH, "filter-10000", "Project", 0);
        assertHeatMapPresent();

        assertHeatMapPresentAndValid(HOMOSAPIEN, 3, "50", "project");
        assertHeatMapPresentAndValid(MONKEY, 3, "50", "project");
    }

    public void testRaisedInVersionsNonArchivedChart()
    {
        loginAsAdmin();
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, new String[] { NEW_VERSION_4 }, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, new String[] { NEW_VERSION_4, NEW_VERSION_5 }, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, new String[] { NEW_VERSION_5 }, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, new String[] { NEW_VERSION_1 }, null, ADMIN_NAME, null, null, null, null, null);

        getWebUnitTest().addColumnToIssueNavigator(new String[] { JIRAWebTest.AFFECTS_VERSIONS_FIELD_ID });

        archiveVersion1();

        selectUnconfiguredGadget();
        configGadget(HOMO_HINT, HOMO_ID, "Raised In Versions (non-archived)", 0);
        assertHeatMapPresent();

        assertHeatMapPresentAndValid(NEW_VERSION_4, 2, "50", VERSIONS_COLUMN);
        assertHeatMapPresentAndValid(NEW_VERSION_5, 2, "50", VERSIONS_COLUMN);
    }

    public void testRaisedInVersionsAllChart()
    {
        loginAsAdmin();
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, new String[] { NEW_VERSION_4 }, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, new String[] { NEW_VERSION_4, NEW_VERSION_5 }, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, new String[] { NEW_VERSION_5 }, null, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, new String[] { NEW_VERSION_1 }, null, ADMIN_NAME, null, null, null, null, null);

        getWebUnitTest().addColumnToIssueNavigator(new String[] { JIRAWebTest.AFFECTS_VERSIONS_FIELD_ID });

        archiveVersion1();

        selectUnconfiguredGadget();
        configGadget(HOMO_HINT, HOMO_ID, "Raised In Versions (all)", 0);
        assertHeatMapPresent();

        assertHeatMapPresentAndValid(NEW_VERSION_4, 2, "40", VERSIONS_COLUMN);
        assertHeatMapPresentAndValid(NEW_VERSION_5, 2, "40", VERSIONS_COLUMN);
        assertHeatMapPresentAndValid(NEW_VERSION_1, 1, "20", VERSIONS_COLUMN);
    }

    public void testReporterChart()
    {
        loginAsAdmin();
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, new String[] { NEW_VERSION_4 }, null, null, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, new String[] { NEW_VERSION_4, NEW_VERSION_5 }, null, null, null, null, null, null, null);

        getWebUnitTest().addColumnToIssueNavigator(new String[] { JIRAWebTest.REPORTER_FIELD_ID });

        getWebUnitTest().login(FRED_USERNAME, FRED_USERNAME);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, new String[] { NEW_VERSION_5 }, null, null, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, new String[] { NEW_VERSION_1 }, null, null, null, null, null, null, null);

        selectUnconfiguredGadget();
        configGadget(HOMO_HINT, HOMO_ID, "Reporter", 0);
        assertHeatMapPresent();

        assertHeatMapPresentAndValid(ADMIN_NAME, 2, "50", REPORTER_COLUMN);
        assertHeatMapPresentAndValid(FRED_NAME, 2, "50", REPORTER_COLUMN);
    }

    public void testResolutionChart()
    {
        loginAsAdmin();
        String issueKey = getWebUnitTest().addIssue(HOMOSAPIEN, "HSP", null, BLARGH, null, null, null, null, null, null, null, null, null, null);
        transitionIssue(issueKey, "Resolve Issue", RESOLUTION_FIXED);
        issueKey = getWebUnitTest().addIssue(HOMOSAPIEN, "HSP", null, BLARGH, null, null, null, null, null, null, null, null, null, null);
        transitionIssue(issueKey, "Resolve Issue", RESOLUTION_WON_T_FIX);
        issueKey = getWebUnitTest().addIssue(HOMOSAPIEN, "HSP", null, BLARGH, null, null, null, null, null, null, null, null, null, null);
        transitionIssue(issueKey, "Resolve Issue", RESOLUTION_DUPLICATE);
        issueKey = getWebUnitTest().addIssue(HOMOSAPIEN, "HSP", null, BLARGH, null, null, null, null, null, null, null, null, null, null);
        transitionIssue(issueKey, "Resolve Issue", RESOLUTION_INCOMPLETE);
        issueKey = getWebUnitTest().addIssue(HOMOSAPIEN, "HSP", null, BLARGH, null, null, null, null, null, null, null, null, null, null);
        transitionIssue(issueKey, "Resolve Issue", RESOLUTION_CANNOT_REPRODUCE);

        getWebUnitTest().addIssueOnly(HOMOSAPIEN, "HSP", null, BLARGH, null, null, null, null, null, null, null, null, null, null);

        selectUnconfiguredGadget();
        configGadget(HOMO_HINT, HOMO_ID, "Resolution", 0);
        assertHeatMapPresent();

        assertHeatMapPresentAndValid(RESOLUTION_FIXED, 1, "16", RESOLUTION_COLUMN);
        // have to escape the damn apos so I can't use constant here
        assertHeatMapPresentAndValid("Won't Fix", 1, "16", RESOLUTION_COLUMN);
        assertHeatMapPresentAndValid(RESOLUTION_DUPLICATE, 1, "16", RESOLUTION_COLUMN);
        assertHeatMapPresentAndValid(RESOLUTION_INCOMPLETE, 1, "16", RESOLUTION_COLUMN);
        assertHeatMapPresentAndValid(RESOLUTION_CANNOT_REPRODUCE, 1, "16", RESOLUTION_COLUMN);
        assertHeatMapPresentAndValid("Unresolved", 1, "16", RESOLUTION_COLUMN);
    }

    public void testStatusChart()
    {
        loginAsAdmin();
        String issueKey = getWebUnitTest().addIssue(HOMOSAPIEN, "HSP", null, BLARGH, null, null, null, null, null, null, null, null, null, null);
        transitionIssue(issueKey, "Resolve Issue", RESOLUTION_FIXED);
        issueKey = getWebUnitTest().addIssue(HOMOSAPIEN, "HSP", null, BLARGH, null, null, null, null, null, null, null, null, null, null);
        transitionIssue(issueKey, "Resolve Issue", RESOLUTION_WON_T_FIX);

        issueKey = getWebUnitTest().addIssue(HOMOSAPIEN, "HSP", null, BLARGH, null, null, null, null, null, null, null, null, null, null);
        transitionIssue(issueKey, "Close Issue", RESOLUTION_FIXED);

        getWebUnitTest().addIssueOnly(HOMOSAPIEN, "HSP", null, BLARGH, null, null, null, null, null, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, "HSP", null, BLARGH, null, null, null, null, null, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, "HSP", null, BLARGH, null, null, null, null, null, null, null, null, null, null);

        selectUnconfiguredGadget();
        configGadget(HOMO_HINT, HOMO_ID, "Status", 0);
        assertHeatMapPresent();

        assertHeatMapPresentAndValid("Open", 3, "50", STATUS_COLUMN);
        assertHeatMapPresentAndValid("Resolved", 2, "33", STATUS_COLUMN);
        assertHeatMapPresentAndValid("Closed", 1, "16", STATUS_COLUMN);
    }

    private void transitionIssue(String issueKey, String action, String resolution)
    {
        getWebUnitTest().gotoPage("/browse/" + issueKey);
        final WebTester tester = getWebUnitTest().getTester();
        tester.clickLinkWithText(action);
        // Resolve Issue Link
        tester.selectOption("resolution", resolution);
        tester.submit();
    }

    private void prepareFixForVersionTest()
    {
        loginAsAdmin();
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, null, new String[] { NEW_VERSION_4 }, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, null, new String[] { NEW_VERSION_4, NEW_VERSION_5 }, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, null, new String[] { NEW_VERSION_5 }, ADMIN_NAME, null, null, null, null, null);
        getWebUnitTest().addIssueOnly(HOMOSAPIEN, null, null, BLARGH, null, null, null, new String[] { NEW_VERSION_1 }, ADMIN_NAME, null, null, null, null, null);

        getWebUnitTest().addColumnToIssueNavigator(new String[] { JIRAWebTest.FIX_VERSIONS_FIELD_ID });

        // Archive version 1
        archiveVersion1();

    }

    protected void selectUnconfiguredGadget()
    {
        getNavigator().currentDashboard().view();
        selectGadget(HEAT_MAP_GADGET_NAME);
        waitForGadgetConfiguration();
    }

    private void archiveVersion1()
    {
        VersionClient vc = new VersionClient(environmentData);
        final Version version = vc.get("10000");
        version.archived(true);

        vc.putResponse(version);
    }

    private void assertHeatMapPresentAndValid(String name, int issues, String percent, String columnName)
    {
        // verify map in the normal view
        visibleByTimeoutWithDelay("jQuery=ul[class='heatmap']>li>a:contains(" + name + ")", 15000);
        verifyHeatMapValid(name, issues, percent, columnName);

        client.selectFrame("relative=top");
        getNavigator().currentDashboard().view();

        assertThat.elementPresentByTimeout("css=a.maximize", 10000);
        client.click("css=a.maximize");
        selectGadget(HEAT_MAP_GADGET_NAME);

        // check the map on this screen.
        visibleByTimeoutWithDelay("jquery=ul[class='heatmap']>li>a:contains('" + name + "')", 15000);
        verifyHeatMapValid(name, issues, percent, columnName);

        client.selectFrame("relative=top");
        getNavigator().currentDashboard().view();

        // reset our state
        client.click("css=a.maximize");
        selectGadget(HEAT_MAP_GADGET_NAME);
        assertHeatMapPresent();
    }

    private void verifyHeatMapValid(String name, int issues, String percent,
            String columnName)
    {
        client.click("jquery=ul[class='heatmap']>li>a:contains('" + name + "')", true);

        // xpath indices are 1-based and we need to get past the header row so we start at 2.
        int startIndex = 1;

        String cellLocator = " > td." + columnName + ":contains('" + (name.equals("Unresolved") ? "Unresolved" : name) + "')";

        // handle issue type a little differently because it has an image column instead of text
        if (columnName.equals(ISSUETYPE_COLUMN) || columnName.equals(PRIORITY_COLUMN))
        {
            cellLocator = " > td." + columnName + " img[alt='" + name + "']";
        }
        else if (columnName.equals(COMPONENTS_COLUMN) && name.equals("No component"))
        {
            // edge case where it's just blank instead of displaying the text
            cellLocator = " > td." + columnName;
        }

        for (int x = 0; x < issues; x++)
        {
            String locator = "css=table#issuetable tbody tr:nth-child(" + (startIndex + x) + ")" + cellLocator;
            assertThat.elementPresent(locator);
        }
    }

    private void assertHeatMapPresent()
    {
        assertThat.elementPresentByTimeout("css=ul[class='heatmap']", 10000);
    }
}
