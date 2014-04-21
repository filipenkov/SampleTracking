package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.admin.Project;
import com.atlassian.jira.functest.framework.navigation.IssueNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.framework.Window;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SeleniumClient;
import com.atlassian.selenium.SkipInBrowser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static com.atlassian.jira.webtests.JIRAWebTest.ISSUE_TYPE_BUG;
import static com.atlassian.jira.webtests.JIRAWebTest.PROJECT_HOMOSAP;
import static com.atlassian.jira.webtests.JIRAWebTest.PROJECT_MONKEY;
import static java.util.Arrays.asList;

/**
 * Selenium test for the road map gadget.
 *
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //Pop-up not found problem - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestRoadmapGadget extends GadgetTest
{
    private static final String PROJECT_GORILLA = "Gorilla";
    private static final String PROJECT_BUMBLEBEE = "Bumblebee";
    private static final String PROJECT_DRAGONFLY = "Dragonfly";
    private static final String PROJECT_DOLPHIN = "Dolphin";
    private static final String CATEGORY_PRIMATES = "Primates";
    private static final String RESULTS_COUNT_LOCATOR = "class=results-count-total";
    private static final String CATEGORY_BUGS = "Bugs";
    private static final String PROJECTS_OR_CATEGORIES_ID = "projectsOrCategories";

    private static final String DAYS_ID = "days";

    private final DateFormat df = new SimpleDateFormat("dd/MMM/yy");
    private String monkeyV11Id;
    private String monkeyV12Id;

    private static final String NUM_ID = "num";
    private Set<String> visitedLinks = new HashSet<String>();

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        createTestData();

        addGadget("Road Map");
    }

    private void createTestData()
    {
        JIRAWebTest jwt = getWebUnitTest();
        Project proj = jwt.getAdministration().project();
        proj.addProject(PROJECT_GORILLA, "GRL", "admin");
        proj.addProject(PROJECT_BUMBLEBEE, "BEE", "admin");
        proj.addProject(PROJECT_DRAGONFLY, "FLY", "admin");
        proj.addProject(PROJECT_DOLPHIN, "DOL", "admin");
        jwt.createProjectCategory(CATEGORY_PRIMATES, "All primates");
        jwt.placeProjectInCategory(PROJECT_HOMOSAP, CATEGORY_PRIMATES);
        jwt.placeProjectInCategory(PROJECT_MONKEY, CATEGORY_PRIMATES);
        jwt.placeProjectInCategory(PROJECT_GORILLA, CATEGORY_PRIMATES);
        jwt.createProjectCategory(CATEGORY_BUGS, "All insects");
        jwt.placeProjectInCategory(PROJECT_BUMBLEBEE, CATEGORY_BUGS);
        jwt.placeProjectInCategory(PROJECT_DRAGONFLY, CATEGORY_BUGS);

        proj.releaseVersion("HSP", "New Version 1", getDate(-14));
        proj.editVersionDetails("HSP", "New Version 4", null, "Test Version Description 4", getDate(-7));
        proj.editVersionDetails("HSP", "New Version 5", null, "Test Version Description 5", getDate(6));

        monkeyV11Id = proj.addVersion("MKY", "1.1", "v1.1", getDate(4));
        monkeyV12Id = proj.addVersion("MKY", "1.2", "v1.2", getDate(13));
        proj.addVersion("MKY", "1.3", "v1.3", null); // release date not specified

        proj.addVersion("GRL", "2", "ver 2", getDate(-4));

        proj.addVersion("BEE", "2.1", "v2.1", getDate(5));

        proj.addVersion("FLY", "3.1", "v3.1", getDate(8));

        proj.addVersion("DOL", "4.1", "v4.1", getDate(8));

        IssueNavigation issue = jwt.getNavigation().issue();
        String ik = issue.createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, "Need more men");
        issue.setFixVersions(ik, "New Version 4");
        issue.resolveIssue(ik, "Fixed", null);

        ik = issue.createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, "Need more women");
        issue.setFixVersions(ik, "New Version 5");
        issue.resolveIssue(ik, "Fixed", null);
        ik = issue.createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, "Need more boys");
        issue.setFixVersions(ik, "New Version 5");
        ik = issue.createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, "Need more girls");
        issue.setFixVersions(ik, "New Version 5");

        ik = issue.createIssue(PROJECT_MONKEY, ISSUE_TYPE_BUG, "Need more jnr devs");
        issue.setFixVersions(ik, "1.1");

        ik = issue.createIssue(PROJECT_GORILLA, ISSUE_TYPE_BUG, "Need more snr devs");
        issue.setFixVersions(ik, "2");

        ik = issue.createIssue(PROJECT_BUMBLEBEE, ISSUE_TYPE_BUG, "Need more managers");
        issue.setFixVersions(ik, "2.1");

        ik = issue.createIssue(PROJECT_DRAGONFLY, ISSUE_TYPE_BUG, "Need more tech writers");
        issue.setFixVersions(ik, "3.1");

        ik = issue.createIssue(PROJECT_DOLPHIN, ISSUE_TYPE_BUG, "Need more admins");
        issue.setFixVersions(ik, "4.1");
    }

    public void testRoadMapGadget() throws UnsupportedEncodingException
    {
        _testConfiguration();

        goToConfig();
        _testAllProjects();

        goToConfig();
        _testNumberOfResults();

        goToConfig();
        _testSingleProject();

        goToConfig();
        _testSingleProjectAllOverdue();

        goToConfig();
        _testSingleProjectNoneOverdue();

        goToConfig();
        _testMultipleProjects();

        goToConfig();
        _testSingleCategory();

        goToConfig();
        _testMultipleCategories();

        goToConfig();
        _testBothProjectAndCategorySelected();
    }

    private void goToConfig()
    {
        clickConfigButton();
        resetConfig();
    }

    private void resetConfig()
    {
        waitForGadgetConfiguration();
        // restore default values for following _test* methods that assumes the default state
        selectByLabels(PROJECTS_OR_CATEGORIES_ID);
        getSeleniumClient().type(DAYS_ID, "30");
        getSeleniumClient().type(NUM_ID, "10");
    }

    public void _testConfiguration()
    {
        waitForGadgetConfiguration();

        assertThat.textPresent("Projects and Categories");
        assertThat.textPresent("Projects or categories to use as the basis for the graph.");
        assertThat.textPresent("All Projects");
        assertThat.textPresent(PROJECT_BUMBLEBEE);
        assertThat.textPresent(PROJECT_DOLPHIN);
        assertThat.textPresent(PROJECT_DRAGONFLY);
        assertThat.textPresent(PROJECT_GORILLA);
        assertThat.textPresent(PROJECT_HOMOSAP);
        assertThat.textPresent(PROJECT_MONKEY);
        assertThat.textPresent(CATEGORY_BUGS);
        assertThat.textPresent(CATEGORY_PRIMATES);

        assertThat.textPresent("Days");
        assertThat.textPresent("Period to cover (in days)");
        // assert default value
        assertThat.attributeContainsValue(DAYS_ID, "value", "30");

        assertThat.textPresent("Number of Results");
        assertThat.textPresent("Number of results to display (maximum of 50).");
        // assert default value
        assertThat.attributeContainsValue(NUM_ID, "value", "10");

        assertRefreshIntervalFieldPresent();

        assertTextFieldError(DAYS_ID, "-1", "Days must be greater or equal to zero.");
        assertTextFieldError(DAYS_ID, "NaN", "Days must be a number");
        assertTextFieldError(DAYS_ID, "1001", "Days must not exceed 1000");
        assertTextFieldError(NUM_ID, "-1", "Number must be greater than 0");
        assertTextFieldError(NUM_ID, "NaN", "The value must be an integer greater than 0 and less than or equal to 50");
        assertTextFieldError(NUM_ID, "51", "Number must not exceed 50");
        assertSelectFieldError(PROJECTS_OR_CATEGORIES_ID, null, "No project/category selected");
    }

    public void _testAllProjects() throws UnsupportedEncodingException
    {
        // default to all projects, 30 days and 10 items
        waitForGadgetConfiguration();
        selectByLabels(PROJECTS_OR_CATEGORIES_ID, "All Projects");
        submitGadgetConfig();

        assertThat.textPresentByTimeout("1 of 1 issues resolved.", TIMEOUT);

        assertGadgetTitle("Road Map: Next 30 Days (Until " + getDate(30) + ")");

        assertEquals(8, countVersions());
        assertEquals(2, countOverdueVersions());

        // 100% resolved
        verifyVersion(0, "HSP", PROJECT_HOMOSAP, "10001", "New Version 4", "Test Version Description 4",
                getDate(-7), 1, 1, 100, 0);
        // 100% unresolved
        verifyVersion(2, "MKY", PROJECT_MONKEY, monkeyV11Id, "1.1", "v1.1",
                getDate(4), 1, 0, 0, 100);
        // 33% resolved, 67% unresolved
        verifyVersion(4, "HSP", PROJECT_HOMOSAP, "10002", "New Version 5", "Test Version Description 5",
                getDate(6), 3, 1, 34, 66);
        // no issues
        verifyVersion(7, "MKY", PROJECT_MONKEY, monkeyV12Id, "1.2", "v1.2", getDate(13), 0, 0, 0, 0);

        // verify the exclusion of a released version
        assertThat.textNotPresent("New Version 1");
        // verify the exclusion of a version with no specified release date
        assertThat.textNotPresent("v1.3");
    }

    public void _testNumberOfResults()
    {
        waitForGadgetConfiguration();
        getSeleniumClient().select(PROJECTS_OR_CATEGORIES_ID, "label=All Projects");
        getSeleniumClient().type(NUM_ID, "5");
        submitGadgetConfig();

        assertThat.textPresentByTimeout("1 of 1 issues resolved.", TIMEOUT);
        assertThat.textNotPresentByTimeout("monkey : 1.2", TIMEOUT);
        assertEquals(5, countVersions());
        assertEquals(2, countOverdueVersions());

        // verify the original value is displayed when reconfig
        clickConfigButton();
        assertThat.attributeContainsValue(NUM_ID, "value", "5");
    }

    public void _testSingleProject()
    {
        waitForGadgetConfiguration();
        getSeleniumClient().select(PROJECTS_OR_CATEGORIES_ID, "label=" + PROJECT_HOMOSAP);
        submitGadgetConfig();

        assertThat.textPresentByTimeout("1 of 3 issues resolved.", TIMEOUT);
        assertThat.textNotPresentByTimeout("monkey : 1.2", TIMEOUT);
        assertEquals(2, countVersions());
        assertEquals(1, countOverdueVersions());
    }

    public void _testSingleProjectAllOverdue()
    {
        waitForGadgetConfiguration();
        getSeleniumClient().select(PROJECTS_OR_CATEGORIES_ID, "label=" + PROJECT_HOMOSAP);
        getSeleniumClient().type(DAYS_ID, "1");
        submitGadgetConfig();

        assertThat.textPresentByTimeout("1 of 1 issues resolved.", TIMEOUT);
        assertThat.textNotPresentByTimeout("monkey : 1.2", TIMEOUT);

        assertGadgetTitle("Road Map: Next 1 Days (Until " + getDate(1) + ")");

        assertEquals(1, countVersions());
        assertEquals(1, countOverdueVersions());

        // verify the original value is displayed when reconfig
        clickConfigButton();
        assertThat.attributeContainsValue(DAYS_ID, "value", "1");
    }

    public void _testSingleProjectNoneOverdue()
    {
        waitForGadgetConfiguration();
        getSeleniumClient().select(PROJECTS_OR_CATEGORIES_ID, "label=" + PROJECT_MONKEY);
        submitGadgetConfig();

        assertThat.textPresentByTimeout("0 of 1 issues resolved.", TIMEOUT);
        assertThat.textNotPresentByTimeout("homosapien : New Version 4", TIMEOUT);

        assertEquals(2, countVersions());
        assertEquals(0, countOverdueVersions());
    }

    public void _testMultipleProjects()
    {
        waitForGadgetConfiguration();
        getSeleniumClient().select(PROJECTS_OR_CATEGORIES_ID, "label=" + PROJECT_HOMOSAP);
        getSeleniumClient().addSelection(PROJECTS_OR_CATEGORIES_ID, "label=" + PROJECT_MONKEY);
        submitGadgetConfig();

        assertThat.textPresentByTimeout("1 of 1 issues resolved.", TIMEOUT);
        assertThat.textNotPresentByTimeout("Bumblebee : 2.1", TIMEOUT);

        assertEquals(4, countVersions());
        assertEquals(1, countOverdueVersions());
    }

    public void _testSingleCategory()
    {
        waitForGadgetConfiguration();
        getSeleniumClient().select(PROJECTS_OR_CATEGORIES_ID, "label=" + CATEGORY_BUGS);
        submitGadgetConfig();

        assertThat.textPresentByTimeout("0 of 1 issues resolved.", TIMEOUT);
        assertThat.textNotPresentByTimeout("homosapien : New Version 4", TIMEOUT);

        assertEquals(2, countVersions());
        assertEquals(0, countOverdueVersions());
    }

    public void _testMultipleCategories()
    {
        waitForGadgetConfiguration();
        getSeleniumClient().select(PROJECTS_OR_CATEGORIES_ID, "label=" + CATEGORY_BUGS);
        getSeleniumClient().addSelection(PROJECTS_OR_CATEGORIES_ID, "label=" + CATEGORY_PRIMATES);
        submitGadgetConfig();

        assertThat.textPresentByTimeout("No issues.", TIMEOUT);
        assertThat.textNotPresentByTimeout("Dolphin : 4.1", TIMEOUT);

        assertEquals(7, countVersions());
        assertEquals(2, countOverdueVersions());
    }

    public void _testBothProjectAndCategorySelected()
    {
        waitForGadgetConfiguration();
        getSeleniumClient().select(PROJECTS_OR_CATEGORIES_ID, "label=" + CATEGORY_PRIMATES);
        getSeleniumClient().addSelection(PROJECTS_OR_CATEGORIES_ID, "label=" + PROJECT_DOLPHIN);
        submitGadgetConfig();

        assertThat.textPresentByTimeout("No issues.", TIMEOUT);
        assertThat.textNotPresentByTimeout("Bumblebee : 2.1", TIMEOUT);

        assertEquals(6, countVersions());
        assertEquals(2, countOverdueVersions());

        clickConfigButton();
        Set<String> selected = new HashSet<String>(asList
                (getSeleniumClient().getSelectedLabels(PROJECTS_OR_CATEGORIES_ID)));
        assertEquals(new HashSet<String>(asList(CATEGORY_PRIMATES, PROJECT_DOLPHIN)), selected);
    }

    private Number countVersions()
    {
        return getSeleniumClient().getXpathCount(
                "//table[@id='road-map-content']/tbody/tr[contains(@class, 'desc')]");
    }

    private Number countOverdueVersions()
    {
        return getSeleniumClient().getXpathCount(
                "//table[@id='road-map-content']/tbody/tr[contains(@class, 'overdue') and contains(@class, 'desc')]");
    }

    private void verifyVersion(final int index, final String projKey, String projName,
            String verId, String verName, String verDesc, String relDate,
            int allCount, int resolvedCount, int resolvedPercentage, int unresolvedPercentage)
            throws UnsupportedEncodingException
    {
        final String baseUrl = getBaseUrl();
        int trNum = index * 2 + 1;
        String trLocator = "//table[@id='road-map-content']/tbody/tr[" + trNum + "]";
        String trDescLocator = "//table[@id='road-map-content']/tbody/tr[" + (trNum + 1) + "]";

        String projAnchorLocator = trLocator + "/td[contains(@class, 'version')]/a[1]";
        assertThat.elementContainsText(projAnchorLocator, projName);
        assertThat.attributeContainsValue(projAnchorLocator, "href", baseUrl + "/browse/" + projKey);
        assertThat.attributeContainsValue(projAnchorLocator, "target", "_parent");
        followProjectLink(projAnchorLocator, projKey, projName);

        String verAnchorLocator = trLocator + "/td[contains(@class, 'version')]/a[2]";
        assertThat.elementContainsText(verAnchorLocator, verName);
        assertThat.attributeContainsValue(verAnchorLocator, "href", baseUrl + "/browse/" + projKey + "/fixforversion/" + verId);
        assertThat.attributeContainsValue(verAnchorLocator, "title", verDesc);
        assertThat.attributeContainsValue(verAnchorLocator, "target", "_parent");
        followVersionLink(verAnchorLocator, projKey, projName, verId, verName);

        assertThat.elementContainsText(trDescLocator + "/td[contains(@class, 'versionDesc')]", verDesc);

        String relDateLocator = trLocator + "/td[contains(@class, 'relDate')]";
        assertThat.elementContainsText(relDateLocator, relDate);

        if (allCount > 0)
        {
            String percentageGraphLocator = trLocator + "/td[contains(@class, 'progress')]/div[contains(@class, 'percentageGraph')]";
            if (resolvedCount > 0)
            {
                String resolvedBarAnchorLocator = percentageGraphLocator + "/a[1]";
                assertThat.elementContainsText(resolvedBarAnchorLocator,
                        "Resolved Issues - " + resolvedPercentage + "%");
                assertThat.attributeContainsValue(resolvedBarAnchorLocator, "href",
                        baseUrl + "/secure/IssueNavigator.jspa?reset=true&jqlQuery=" +
                                URLEncoder.encode("project = " + projName +
                                        " AND fixVersion = " + quote(verName) +
                                        " AND resolution is not EMPTY", "UTF-8"));
                assertThat.attributeContainsValue(resolvedBarAnchorLocator, "style",
                        "width: " + resolvedPercentage + "%");
                assertThat.attributeContainsValue(resolvedBarAnchorLocator, "title",
                        "Resolved Issues - " + resolvedPercentage + "% (" + resolvedCount + " issues)");
                assertThat.attributeContainsValue(resolvedBarAnchorLocator, "target", "_parent");
                followIssueNavigatorLink(resolvedBarAnchorLocator, projKey, projName, verId, verName, true, resolvedCount);
            }
            if (resolvedCount < allCount)
            {
                String unresolvedBarAnchorLocator = percentageGraphLocator + "/a[" + (resolvedCount == 0 ? 1 : 2) + "]";
                assertThat.elementContainsText(unresolvedBarAnchorLocator,
                        "Unresolved Issues - " + unresolvedPercentage + "%");
                assertThat.attributeContainsValue(unresolvedBarAnchorLocator, "href",
                        baseUrl + "/secure/IssueNavigator.jspa?reset=true&jqlQuery=" +
                                URLEncoder.encode("project = " + projName +
                                        " AND fixVersion = " + quote(verName) +
                                        " AND resolution is EMPTY", "UTF-8"));
                assertThat.attributeContainsValue(unresolvedBarAnchorLocator, "style",
                        "width: " + unresolvedPercentage + "%");
                assertThat.attributeContainsValue(unresolvedBarAnchorLocator, "title",
                        "Unresolved Issues - " + unresolvedPercentage + "% (" + (allCount - resolvedCount) + " issues)");
                assertThat.attributeContainsValue(unresolvedBarAnchorLocator, "target", "_parent");
                followIssueNavigatorLink(unresolvedBarAnchorLocator, projKey, projName, verId, verName, false, allCount - resolvedCount);
            }
            assertThat.elementContainsText(trDescLocator + "/td[contains(@class, 'progressDesc')]/span",
                    resolvedCount + " of " + allCount + " issues resolved.");
        }
        else
        {
            assertThat.elementContainsText(
                    trLocator + "/td[contains(@class, 'progress')]/span[contains(@class, 'no-issues')]", "No issues.");
        }
    }

    private String quote(final String input)
    {
        // project name is not quoted if there is no space in it (luckily we don't have those names in the test
        // version name seems always be quoted:
        // when contains space, dot or is numeric (so that it's not mixed up with id?)
        return input == null ? input : "\"" + input + "\"";
    }

    private void followProjectLink(final String projAnchorLocator, final String projKey, final String projName)
    {
        String projUrl = getSeleniumClient().getAttribute(projAnchorLocator + "/@href");
        if (!visitedLinks.contains(projUrl))
        {
            Window.openAndSelect(getSeleniumClient(), projUrl, projKey);
            String title = getSeleniumClient().getTitle();
            assertTrue("Window title [" + title + "] does not contain '" + projName + "'", title.contains(projName));
            assertThat.elementContainsText("css=#content > header > h1", projName);
            Window.close(getSeleniumClient(), projKey);
            goToGadget();
            visitedLinks.add(projUrl);
        }
    }

    private void followVersionLink(final String versionAnchorLocator, final String projKey, final String projName,
            final String versionId, final String versionName)
    {
        String verUrl = getSeleniumClient().getAttribute(versionAnchorLocator + "/@href");
        if (!visitedLinks.contains(verUrl))
        {
            String winId = projKey + "_" + versionId;
            Window.openAndSelect(getSeleniumClient(), verUrl, winId);
            String title = getSeleniumClient().getTitle();
            String expectedTitle = projName + ": " + versionName;
            assertTrue("Window title [" + title + "] does not contain '" + expectedTitle + "'", title.contains(expectedTitle));
            assertThat.elementContainsText("css=#content > header > .breadcrumbs", projName);
            assertThat.elementContainsText("css=#content > header > h1", versionName);
            Window.close(client, winId);
            goToGadget();
            visitedLinks.add(verUrl);
        }
    }

    private void followIssueNavigatorLink(final String countAnchorLocator, final String projKey, final String projName,
            final String verId, final String verName, final boolean resolved, final int expectedCount)
    {
        final SeleniumClient client = getSeleniumClient();
        String navUrl = client.getAttribute(countAnchorLocator + "/@href");
        if (!visitedLinks.contains(navUrl))
        {
            String winId = projKey + "_" + verId + "_" + resolved + "_" + expectedCount;
            Window.openAndSelect(client, navUrl, winId);
            assertTrue(client.getTitle().contains("Issue Navigator"));
            assertThat.elementContainsText("jqltext",
                    "project = " + projName + " AND fixVersion = " + quote(verName) + " AND resolution is" +
                            (resolved ? " not " : " ") + "EMPTY");

            final int total;
            if (!client.isElementPresent(RESULTS_COUNT_LOCATOR))
            {
                total = 0;
            }
            else
            {
                total = Integer.parseInt(client.getText(RESULTS_COUNT_LOCATOR));
            }

            assertEquals(expectedCount, total);
            Window.close(client, winId);
            goToGadget();
            visitedLinks.add(navUrl);
        }
    }

    private void goToGadget()
    {
        selectGadget("Road Map");
    }

    private String getDate(int daysToAdd)
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd);

        return df.format(cal.getTime());
    }
}
