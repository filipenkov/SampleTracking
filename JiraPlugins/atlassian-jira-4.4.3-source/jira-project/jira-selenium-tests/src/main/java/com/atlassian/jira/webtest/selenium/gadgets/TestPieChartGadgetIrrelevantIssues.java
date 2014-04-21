package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsPresentCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;

/**
 * Tests the breakdown of statistics in a pie chart when fields are configured to be invisible for some of the filter results.
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestPieChartGadgetIrrelevantIssues extends GadgetTest
{
    private static final String STAT_IRRELEVANT = "Irrelevant";
    private static final String STAT_NONE = "None";
    private static final String PIE_CHART_GADGET_TITLE = "Pie Chart";
    private static final String GADGET_VIEW = "chart";
    private static final String SELECT_CF_FIELD_CONFIG_NAME = "select cf";
    private static final String CHART_TABLE_LOCATOR = "div.view.chart table.aui";

    // TODO this is bad, don't use Selenium locators directly in test. Consider implementing a gadget page object
    private SeleniumLocator chartTableLocator;

    @Override
    public void onSetUp()
    {
        internalSetup();
        restoreData("TestPieChartGadgetIrrelevantIssues.xml");
        chartTableLocator = SeleniumLocators.css(CHART_TABLE_LOCATOR, context());
    }

    public void testDifferentFieldConfigurations() throws Exception
    {
        // data has three field configurations: Default, Copy (which we will be changing) and Unused (to keep fields visible)
        // we are only concerned with Select CF
        // data begins with Select CF being shown in both configurations

        maximizeGadget(PIE_CHART_GADGET_TITLE);
        waitForGadgetView(GADGET_VIEW);

        assertChartDataTable(new String[][] {{"opt1", "1", "14"}, {"Optoin 1", "2", "28"}, { STAT_NONE, "4", "57"}}, 7);

        // hide Select CF in default config and reload dashboard
        getWebUnitTest().getAdministration().fieldConfigurations().defaultFieldConfiguration().hideFields(SELECT_CF_FIELD_CONFIG_NAME);
        getWebUnitTest().getAdministration().reIndex();

        reloadDashboardAfterChangeAndSelectGadget();
        assertChartDataTable(new String[][] {{"Optoin 1", "1", "14"}, { STAT_NONE, "2", "28"}, { STAT_IRRELEVANT, "4", "57"}}, 7);

        // hide Select CF in Copy config too and reload dashboard
        getWebUnitTest().getAdministration().fieldConfigurations().fieldConfiguration("Copy of Default Field Configuration").hideFields(SELECT_CF_FIELD_CONFIG_NAME);
        getWebUnitTest().getAdministration().reIndex();

        reloadDashboardAfterChangeAndSelectGadget();
        assertChartDataTable(new String[][] {{ STAT_IRRELEVANT, "7", "100"}}, 7);

        // show Select CF in default config (still hidden in Copy) and reload dashboard
        getWebUnitTest().getAdministration().fieldConfigurations().defaultFieldConfiguration().showFields(SELECT_CF_FIELD_CONFIG_NAME);
        getWebUnitTest().getAdministration().reIndex();

        reloadDashboardAfterChangeAndSelectGadget();
        assertChartDataTable(new String[][] {{"Optoin 1", "1", "14"}, {"opt1", "1", "14"}, { STAT_NONE, "2", "28"}, { STAT_IRRELEVANT, "3", "42"}}, 7);
    }

    private void reloadDashboardAfterChangeAndSelectGadget()
    {
        selectDashboardFrame();
        getNavigator().gotoHome();
        maximizeGadget(PIE_CHART_GADGET_TITLE);
        waitForGadgetView(GADGET_VIEW);
    }

    private void waitForChartTable()
    {
        assertThat(tablePresentCondition(), isTrue().byDefaultTimeout());
    }

    private TimedCondition tablePresentCondition()
    {
        return IsPresentCondition.forContext(context()).locator(chartTableLocator).defaultTimeout(Timeouts.PAGE_LOAD).build();
    }


    private void assertChartDataTable(final String[][] stats, int total)
    {
        waitForChartTable();
        // note: we don't actually assert anything about the chart/imagemap, just the data table (in canvas mode)
        assertThat.elementHasText("//a[@title='All']/strong", String.valueOf(total));
        for (int i = 0; i < stats.length; i++)
        {
            String[] statRow = stats[i];
            int idx = i + 1;
            final String statName = statRow[0];
            final String statCount = statRow[1];
            final String statPercent = statRow[2];

            assertThat.elementContainsText(String.format("//table[@class='aui']//tr[%d]/td[1]", idx), statName);
            assertThat.elementContainsText(String.format("//table[@class='aui']//tr[%d]/td[2]", idx), statCount);
            assertThat.elementContainsText(String.format("//table[@class='aui']//tr[%d]/td[3]", idx), statPercent);
        }
    }
}