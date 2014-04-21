package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.admin.CustomFields;
import com.atlassian.jira.functest.framework.admin.FieldConfigurations;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.framework.Window;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Selenium test for the Two Dimensional Stats Gadget.
 */
@SkipInBrowser(browsers={Browser.IE}) //Selenium Command Exception - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestTwoDimensionalStatsGadget extends GadgetTest
{
    private static final String X_STAT_TYPE = "xstattype";
    private static final String TWO_DIMENSIONAL_FILTER_STATISTICS_GADGET = "Two Dimensional Filter Statistics";
    private static final int TIMEOUT = 3000;
    private static final String MORE_OR_LESS_LINK = "//p[@class='more']/a";

    @Override
    protected void restoreGadgetData()
    {
        //does nothing, each test imports its own data.
    }

    public void testConfigureAndView()
    {
        restoreData("TestTwoDimensionalStatsGadget.xml");
        addGadget(TWO_DIMENSIONAL_FILTER_STATISTICS_GADGET);
        waitForGadgetConfiguration();

        _testBasicConfig();
        _testBadConfig();
        _testProjectVsAssignee();
        _testGroupPickerCf();
        _testProjectVsAssigneeReversedDirection();

        _testFixVersion();

        _testReporter();

        _testIssueType();

        _testComponents();

        _testEmptyFilter();

        _testFilterLink();

        _testXofYShowing();

        _testMoreOrLessLink();
    }

    public void testIrrelevantIssues() throws Exception
    {
        restoreData("TestTwoDimensionalStatsGadgetIrrelevant.xml");
        getWebUnitTest().getAdministration().reIndex();
        getNavigator().currentDashboard().view();
        List<int[]> tableData = new ArrayList<int[]>();
        tableData.add(new int[]{1 ,	0,	0,	0,	0,	1});
        tableData.add(new int[]{0 ,	0,	0,	1,	0,	1});
        tableData.add(new int[]{0 ,	1,	1,	0,	0,	2});
        tableData.add(new int[]{0 ,	0,	0,	0,	3,	3});
        tableData.add(new int[]{1 ,	1,	1,	1,	3,	7});
        String[] keys = {"opt1", "Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};

        assertTableData(keys, tableData);

        getCopyOfDefaultFieldConfiguration().showFields("select cf");
        getWebUnitTest().getAdministration().reIndex();
        getNavigator().currentDashboard().view();

        tableData = new ArrayList<int[]>();
        tableData.add(new int[]{1 ,	0,	0,	0,	0,	1});
        tableData.add(new int[]{0 ,	0,	0,	1,	1,	2});
        tableData.add(new int[]{0 ,	1,	1,	0,	2,	4});
        tableData.add(new int[]{1 ,	1,	1,	1,	3,	7});
        keys = new String[] {"opt1", "Optoin 1", "None", "Total Unique Issues:"};

        assertTableData(keys, tableData);

        getCopyOfDefaultFieldConfiguration().hideFields("select cf");
        getCopyOfDefaultFieldConfiguration().showFields("Fix Version/s");
        getWebUnitTest().getAdministration().reIndex();
        getNavigator().currentDashboard().view();

        tableData = new ArrayList<int[]>();
        tableData.add(new int[]{1, 0, 0, 0, 0, 0, 1});
        tableData.add(new int[]{0, 0, 0, 0, 0, 1, 1});
        tableData.add(new int[]{0, 1, 1, 0, 0, 0, 2});
        tableData.add(new int[]{0, 0, 0, 1, 2, 0, 3});
        tableData.add(new int[]{1, 1, 1, 1, 2, 1, 7});
        keys = new String[] {"opt1", "Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};
        assertTableData(keys, tableData);

        getDefaultFieldConfiguration().hideFields("select cf");
        getCopyOfDefaultFieldConfiguration().hideFields("Fix Version/s");
        getCopyOfDefaultFieldConfiguration().showFields("select cf");
        getWebUnitTest().getAdministration().reIndex();
        getNavigator().currentDashboard().view();

        tableData = new ArrayList<int[]>();
        tableData.add(new int[]{0, 0, 0, 0, 1, 1});
        tableData.add(new int[]{0, 0, 0, 0, 2, 2});
        tableData.add(new int[]{1, 1, 1, 1, 0, 4});
        tableData.add(new int[]{1, 1, 1, 1, 3, 7});
        keys = new String[] {"Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};
        assertTableData(keys, tableData);

        getDefaultFieldConfiguration().hideFields("Fix Version/s");
        getDefaultFieldConfiguration().showFields("select cf");
        getCopyOfDefaultFieldConfiguration().showFields("Fix Version/s");
        getCopyOfDefaultFieldConfiguration().hideFields("select cf");
        getWebUnitTest().getAdministration().reIndex();
        getNavigator().currentDashboard().view();

        tableData = new ArrayList<int[]>();
        tableData.add(new int[]{0, 0, 1, 1});
        tableData.add(new int[]{0, 0, 1, 1});
        tableData.add(new int[]{0, 0, 2, 2});
        tableData.add(new int[]{1, 2, 0, 3});
        tableData.add(new int[]{1, 2, 4, 7});
        keys = new String[] {"opt1", "Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};

        assertTableData(keys, tableData);

        getDefaultFieldConfiguration().hideFields("select cf");
        getCopyOfDefaultFieldConfiguration().showFields("select cf");
        getWebUnitTest().getAdministration().reIndex();
        getNavigator().currentDashboard().view();

        tableData = new ArrayList<int[]>();
        tableData.add(new int[]{0, 1, 0, 1});
        tableData.add(new int[]{1, 1, 0, 2});
        tableData.add(new int[]{0, 0, 4, 4});
        tableData.add(new int[]{1, 2, 4, 7});
        keys = new String[] {"Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};

        assertTableData(keys, tableData);

        getDefaultFieldConfiguration().showFields("Fix Version/s");
        getWebUnitTest().getAdministration().reIndex();
        getNavigator().currentDashboard().view();

        tableData = new ArrayList<int[]>();
        tableData.add(new int[]{0, 0, 0, 0, 1, 0, 1});
        tableData.add(new int[]{0, 0, 0, 1, 1, 0, 2});
        tableData.add(new int[]{1, 1, 1, 0, 0, 1, 4});
        tableData.add(new int[]{1, 1, 1, 1, 2, 1, 7});
        keys = new String[] {"Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};

        assertTableData(keys, tableData);

        getDefaultFieldConfiguration().hideFields("Fix Version/s");
        getDefaultFieldConfiguration().showFields("select cf");
        getWebUnitTest().getAdministration().reIndex();
        getNavigator().currentDashboard().view();

        tableData = new ArrayList<int[]>();
        tableData.add(new int[]{0, 0, 1, 1});
        tableData.add(new int[]{0, 1, 1, 2});
        tableData.add(new int[]{1, 1, 2, 4});
        tableData.add(new int[]{1, 2, 4, 7});
        keys = new String[] {"opt1", "Optoin 1", "None", "Total Unique Issues:"};

        assertTableData(keys, tableData);

        getDefaultFieldConfiguration().showFields("Fix Version/s");
        getCustomFields().editConfigurationSchemeContextById("10000", "10010", null, new String[] {}, new String[] {"10010"});
        getWebUnitTest().getAdministration().reIndex();
        getNavigator().currentDashboard().view();

        tableData = new ArrayList<int[]>();
        tableData.add(new int[]{1, 0, 0, 0, 0, 0, 1});
        tableData.add(new int[]{0, 0, 0, 0, 0, 1, 1});
        tableData.add(new int[]{0, 1, 1, 0, 0, 0, 2});
        tableData.add(new int[]{0, 0, 0, 1, 2, 0, 3});
        tableData.add(new int[]{1, 1, 1, 1, 2, 1, 7});
        keys = new String[] {"opt1", "Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};

        assertTableData(keys, tableData);

        getCustomFields().editConfigurationSchemeContextById("10000", "10010", null, new String[] {}, new String[] {"10011"});
        getWebUnitTest().getAdministration().reIndex();
        getNavigator().currentDashboard().view();

        tableData = new ArrayList<int[]>();
        tableData.add(new int[]{0, 0, 0, 0, 1, 0, 1});
        tableData.add(new int[]{0, 0, 0, 1, 1, 0, 2});
        tableData.add(new int[]{1, 1, 1, 0, 0, 1, 4});
        tableData.add(new int[]{1, 1, 1, 1, 2, 1, 7});
        keys = new String[] {"Optoin 1", "None", "Irrelevant", "Total Unique Issues:"};

        assertTableData(keys, tableData);
    }

    private FieldConfigurations.FieldConfiguration getDefaultFieldConfiguration()
    {
        return getWebUnitTest().getAdministration().fieldConfigurations().fieldConfiguration("Default Field Configuration");
    }

    private FieldConfigurations.FieldConfiguration getCopyOfDefaultFieldConfiguration()
    {
        return getWebUnitTest().getAdministration().fieldConfigurations().fieldConfiguration("Copy of Default Field Configuration");
    }

    private CustomFields getCustomFields()
    {
        return getWebUnitTest().getAdministration().customFields();
    }


    private void assertTableData(final String[] keys, final List<int[]> tableData)
    {
        int y = 2;
        for (String key : keys)
        {
            assertEquals(key, client.getTable("twodstatstable." + y + "." + 0));
            y++;
        }
        assertTableData(tableData);
    }

    private void assertTableData(final List<int[]> tableData)
    {
        client.selectWindow(null);
        int y = 2;
        for (Iterator<int[]> iterator = tableData.iterator(); iterator.hasNext();)
        {
            int[] row =  iterator.next();
            int x = 1;
            for (int i = 0; i < row.length; i++)
            {
                int cellValue = row[i];
                assertCorrectNumberIssues(cellValue, y, x);
                x++;
            }
            y++;
        }
        client.selectWindow(null);
    }

    private void _testMoreOrLessLink()
    {
        getNavigator().gotoHome();
        goToGadget();
        checkShowingXofY(1,2);
        List<int[]> tableData1 = new ArrayList<int[]>();
        tableData1.add(new int[]{0, 0, 4, 4});
        tableData1.add(new int[]{1, 2, 4, 7});
        assertTableData(tableData1);
        clickMoreButton();
        checkShowingXofY(2,2);
        List<int[]> tableData2 = new ArrayList<int[]>();
        tableData2.add(new int[]{0, 0, 4, 4});
        tableData2.add(new int[]{1, 2, 0, 3});
        tableData2.add(new int[]{1, 2, 4, 7});
        assertTableData(tableData2);
        clickLessButton();
        checkShowingXofY(1,2);
        assertTableData(tableData1);
    }

    private void _testXofYShowing()
    {
        getNavigator().gotoHome();
        goToGadget();
        checkShowingXofY(1,2);
        clickConfigButton();
        assertThat.visibleByTimeout("//input[@id='numberToShow' and @value='1']", TIMEOUT);
        setTextField("numberToShow", "2");
        submitGadgetConfig();
        checkShowingXofY(2,2);
        setTextField("numberToShow", "5");
        submitGadgetConfig();
        checkShowingXofY(2,2);
        setTextField("numberToShow", "1");
        submitGadgetConfig();
        waitFor(TIMEOUT);
        client.selectWindow(null);
    }


    private void _testGroupPickerCf()
    {
        changeConfig(new SelectChoice(X_STAT_TYPE, "Group Picker CF"));

        assertEquals("jira-administrators", client.getTable("twodstatstable.1.0"));
        assertEquals("None", client.getTable("twodstatstable.1.1"));

        assertTrue(extractMarkupForHeader(2).contains("%22Group+Picker+CF%22+%3D+jira-administrators"));
        assertTrue(extractMarkupForHeader(3).contains("%22Group+Picker+CF%22+is+EMPTY"));

        assertCorrectNumberIssues(0, 2, 1, "project+%3D+HSP+AND+%22Group+Picker+CF%22+%3D+jira-administrators");
        assertCorrectNumberIssues(3, 2, 2, "project+%3D+HSP+AND+%22Group+Picker+CF%22+is+EMPTY");
        assertCorrectNumberIssues(1, 3, 1, "project+%3D+MKY+AND+%22Group+Picker+CF%22+%3D+jira-administrators");
        assertCorrectNumberIssues(3, 3, 2, "project+%3D+MKY+AND+%22Group+Picker+CF%22+is+EMPTY");
    }

    private void _testBadConfig()
    {
        setTextField("filterId", "");
        submitGadgetConfig();
        assertThat.elementPresentByTimeout("//div[@class='error' and contains(text(), 'filter specified')]", 5000);

        setTextField("filterId", "somefilterthatdoesntexist");
        submitGadgetConfig();
        assertThat.elementPresentByTimeout("//div[@class='error' and contains(text(), 'Invalid project or filter')]", 5000);

        setTextField("numberToShow", "0");
        submitGadgetConfig();
        assertThat.elementPresentByTimeout("//div[@class='error' and contains(text(), 'greater than 0')]", 5000);

        setTextField("numberToShow", "notanumber");
        submitGadgetConfig();
        assertThat.elementPresentByTimeout("//div[@class='error' and contains(text(), 'must be an integer')]", 5000);

        setTextField("numberToShow", "10");
    }

    private void assertCorrectNumberIssues(int numResults, int yPos, int xPos, String jqlInUrl)
    {
        assertEquals(Integer.toString(numResults), client.getTable("twodstatstable." + yPos + "." + xPos));

        final String locator = "//table[@id='twodstatstable']/tbody/tr[" + (yPos + 1) + "]/td[" + (xPos + 1) + "]/a/@href";

        if( client.isElementPresent(locator))
        {
            String url = client.getAttribute(locator);
            if (jqlInUrl != null)
            {
                assertTrue(url.contains(jqlInUrl));
            }

            String winId = url + "_" + xPos + "_" + yPos;
            Window.openAndSelect(getSeleniumClient(), url, winId);

            if (numResults == 0)
            {
                assertThat.textPresent("No matching issues");
            }
            else
            {
                assertThat.textPresent("Displaying issues 1 to " + numResults + " of " + numResults + " matching issues");
            }
            Window.close(getSeleniumClient(), winId);
            goToGadget();
        }
    }

    private void assertCorrectNumberIssues(int numResults, int yPos, int xPos)
    {
        assertCorrectNumberIssues(numResults, yPos, xPos, null);
    }

    private void goToGadget()
    {
        selectGadget(TWO_DIMENSIONAL_FILTER_STATISTICS_GADGET);
    }

    private void _testEmptyFilter()
    {
        clickConfigButton();
        selectProjectOrFilterFromAutoComplete("quickfind", "empty", "filter-10001");
        submitGadgetConfig();
        assertThat.textPresentByTimeout("The filter for this gadget did not return any issues", 5000);
    }

    private void _testComponents()
    {
        changeConfig(new SelectChoice("xstattype", "Components"));

        assertEquals("New Component 1", client.getTable("twodstatstable.1.0"));
        assertEquals("New Component 2", client.getTable("twodstatstable.1.1"));
        assertEquals("No component", client.getTable("twodstatstable.1.2"));
        String firstComponentMarkup = extractMarkupForHeader(2);
        String secondComponentMarkup = extractMarkupForHeader(3);
        String noComponentMarkup = extractMarkupForHeader(4);

        assertTrue(firstComponentMarkup.contains("component+%3D+%22New+Component+1%22+AND+project+%3D+HSP"));
        assertTrue(secondComponentMarkup.contains("component+%3D+%22New+Component+2%22+AND+project+%3D+HSP"));
        assertTrue(noComponentMarkup.contains("component+is+EMPTY"));

        assertCorrectNumberIssues(0, 2, 1);
        assertCorrectNumberIssues(0, 2, 2);
        assertCorrectNumberIssues(4, 2, 3);
        assertCorrectNumberIssues(1, 3, 1);
        assertCorrectNumberIssues(2, 3, 2);
        assertCorrectNumberIssues(0, 3, 3);

        changeConfig("numberToShow", "1");
    }

    private void _testIssueType()
    {
        changeConfig(new SelectChoice(X_STAT_TYPE, "Issue Type"));

        String bugImgMarkup = "<img width=\"16\" height=\"16\" alt=\"Bug\" title=\"Bug - A problem which impairs or prevents the functions of the product.\" src=\"" +
                getSeleniumConfiguration().getBaseUrl() + "/images/icons/" + "bug.gif\">";

        String featureImgMarkup = "<img width=\"16\" height=\"16\" alt=\"New Feature\" title=\"New Feature - A new feature of the product, which has yet to be developed.\" src=\"" +
                getSeleniumConfiguration().getBaseUrl() + "/images/icons/" + "newfeature.gif\">";

        assertEquals("Bug", client.getTable("twodstatstable.1.0"));
        assertEquals("New Feature", client.getTable("twodstatstable.1.1"));
        assertEquals("Task", client.getTable("twodstatstable.1.2"));

        assertCorrectNumberIssues(1, 2, 1);
        assertCorrectNumberIssues(0, 2, 2);
        assertCorrectNumberIssues(3, 2, 3);

        assertCorrectNumberIssues(2, 3, 1);
        assertCorrectNumberIssues(1, 3, 2);
        assertCorrectNumberIssues(0, 3, 3);

        String firstComponentMarkup = extractMarkupForHeader(2);
        String secondComponentMarkup = extractMarkupForHeader(3);

        //Img attributes seem to be a bit randomly ordered
        assertSynonyms(firstComponentMarkup.substring(0, firstComponentMarkup.indexOf(">") + 1), bugImgMarkup);
        assertSynonyms(secondComponentMarkup.substring(0, secondComponentMarkup.indexOf(">") + 1), featureImgMarkup);
    }

    private void _testReporter()
    {
        changeConfig(new SelectChoice(X_STAT_TYPE, "Reporter"));

        //Only one reporter, so it will be every issue in each project
        assertCorrectNumberIssues(4, 2, 1);
        assertCorrectNumberIssues(3, 3, 1);
    }

    private void _testFixVersion()
    {
        changeConfig(new SelectChoice(X_STAT_TYPE, "Fix For Versions (all)"));

        //Should have versions on the x axis (row 1) now
        assertEquals("homosapien 1", client.getTable("twodstatstable.1.0"));
        assertEquals("homosapien 2", client.getTable("twodstatstable.1.1"));
        assertEquals("monkey 1", client.getTable("twodstatstable.1.2"));
        assertEquals("monkey 2", client.getTable("twodstatstable.1.3"));
        assertEquals("Unscheduled", client.getTable("twodstatstable.1.4"));
        assertEquals("T:", client.getTable("twodstatstable.1.5"));
    }

    private void _testProjectVsAssigneeReversedDirection()
    {
        //Flip the direction
        changeConfig(new SelectChoice("sortDirection", "Descending"), new SelectChoice("showTotals", "Yes"), new SelectChoice("ystattype", "Project"), new SelectChoice(X_STAT_TYPE, "Assignee"));

        assertCorrectNumberIssues(1, 2, 1);
        assertCorrectNumberIssues(3, 3, 1);

        //Total for monkey
        assertCorrectNumberIssues(4, 2, 3);
        //Total for homosapien
        assertCorrectNumberIssues(3, 3, 3);
        //Total for admin
        assertCorrectNumberIssues(4, 4, 1);
        //Total for Dr Zaius
        assertCorrectNumberIssues(3, 4, 2);
        //Total for everything
        assertEquals("7", client.getTable("twodstatstable.4.3"));
    }

    private void _testProjectVsAssignee()
    {
        // X : proj, Y : assignee
        client.select("ystattype", "Project");
        selectProjectOrFilterFromAutoComplete("quickfind", "test", "filter-10000");
        submitGadgetConfig();

        assertThat.textPresentByTimeout("testFilter", 5000);

        assertThat.elementPresentByTimeout("twodstatstable", 5000);
        assertEquals("Project", client.getTable("twodstatstable.0.0"));
        assertEquals("Assignee", client.getTable("twodstatstable.0.1"));
        assertEquals("Administrator", client.getTable("twodstatstable.1.0"));
        assertEquals("drzaius", client.getTable("twodstatstable.1.1"));

        assertEquals("homosapien", client.getTable("twodstatstable.2.0"));
        assertEquals("monkey", client.getTable("twodstatstable.3.0"));

        assertCorrectNumberIssues(3, 2, 1);
        assertCorrectNumberIssues(0, 2, 2);

        assertCorrectNumberIssues(1, 3, 1);
        assertCorrectNumberIssues(3, 3, 2);
    }

    private void _testBasicConfig()
    {
        assertThat.textPresent("Saved Filter:");
        assertThat.textPresent("XAxis");
        assertThat.textPresent("YAxis");

        assertThat.textPresent("Sort By");
        assertThat.textPresent("Sort Direction");
        assertThat.textPresent("Show Totals");
        assertThat.textPresent("Number of Results");

        String[] statValues = { "assignees", "components", "issuetype", "fixfor", "allFixfor", "priorities", "version", "allVersion", "reporter", "resolution", "statuses" };
        assertFieldOptionValuesPresent(X_STAT_TYPE, statValues);
        assertFieldOptionValuesPresent("ystattype", statValues);
    }

    private void changeConfig(SelectChoice... options)
    {
        clickConfigButton();
        for (SelectChoice choice : options)
        {
            client.select(choice.key, choice.option);
        }
        submitGadgetConfig();
        waitFor(2500);
    }

    private void changeConfig(String key, String value)
    {
        clickConfigButton();
        client.type(key, value);
        submitGadgetConfig();
        waitFor(1500);
    }

    private class SelectChoice
    {
        private String key;
        private String option;

        private SelectChoice(String key, String option)
        {
            this.key = key;
            this.option = option;
        }
    }

    public String extractMarkupForHeader(int pos)
    {
        return client.getEval("selenium.browserbot.getCurrentWindow().jQuery(selenium.browserbot.getCurrentWindow().jQuery('#twodstatstable th')[" + Integer.toString(pos) + "]).html()");
    }

    private void assertSynonyms(String first, String second)
    {
        char[] firstChars = first.toCharArray();
        char[] secondChars = second.toCharArray();

        assertEquals("Strings [" + first + " ] and [" + second + "] are not equal in length", firstChars.length, secondChars.length);
        Arrays.sort(firstChars);
        Arrays.sort(secondChars);
        assertTrue("Strings [" + first + " ] and [" + second + "] do not have same characters", Arrays.equals(firstChars, secondChars));
    }

    private void clickMoreButton()
    {
        assertThat.elementContainsText(MORE_OR_LESS_LINK, "Show more");
        client.click(MORE_OR_LESS_LINK);
        waitFor(TIMEOUT);
        assertThat.elementContainsText(MORE_OR_LESS_LINK, "Show less");
    }

    private void clickLessButton()
    {
        assertThat.elementContainsText(MORE_OR_LESS_LINK, "Show less");
        client.click(MORE_OR_LESS_LINK);
        waitFor(TIMEOUT);
        assertThat.elementContainsText(MORE_OR_LESS_LINK, "Show more");
    }

    private void checkShowingXofY(int x, int y)
    {
        waitFor(TIMEOUT);
        assertThat.elementContainsText("//div[@class='table-footer']/div[1]", "Showing " + x + " of " + y + " statistics.");
        assertThat.elementContainsText("//div[@class='table-footer']/div[1]/strong[1]", Integer.toString(x));
        assertThat.elementContainsText("//div[@class='table-footer']/div[1]/strong[2]", Integer.toString(y));
    }

    private void _testFilterLink()
    {
        clickConfigButton();
        selectProjectOrFilterFromAutoComplete("quickfind", "test", "filter-10000");
        submitGadgetConfig();
        waitFor(2000);
        client.clickLinkWithText("testFilter", true);
        int numResults = 7;
        assertThat.textPresent("Displaying issues 1 to " + numResults + " of " + numResults + " matching issues");
    }
}
