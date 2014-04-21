package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests the breakdown of statistics in a heat map when fields are configured to be invisible for some of the filter results.
 * Based on {@link com.atlassian.jira.webtest.selenium.gadgets.TestPieChartGadgetIrrelevantIssues}
 *
 * @since v4.1
 */
@WebTest({Category.SELENIUM_TEST })
public class TestHeatMapGadgetIrrelevantIssues extends GadgetTest
{
    private static final String STAT_IRRELEVANT = "Irrelevant";
    private static final String STAT_NONE = "None";
    private static final String HEATMAP_GADGET_TITLE = "Heat Map";
    private static final String HEATMAP_VIEW = "heatmap";
    private static final String SELECT_CF_FIELD_CONFIG_NAME = "select cf";

    @Override
    protected void restoreGadgetData()
    {
        restoreData("TestHeatMapGadgetIrrelevantIssues.xml");
    }

    public void testDifferentFieldConfigurations() throws Exception
    {
        // data has three field configurations: Default, Copy (which we will be changing) and Unused (to keep fields visible)
        // we are only concerned with Select CF
        // data begins with Select CF being shown in both configurations

        maximizeGadget(HEATMAP_GADGET_TITLE);
        waitForGadgetView(HEATMAP_VIEW);

        assertHeatMapItems(new String[][] {{"Optoin 1", "2", "28"}, {"opt1", "1", "14"}, { STAT_NONE, "4", "57"}}, 7);

        // hide Select CF in default config and reload dashboard
        getWebUnitTest().getAdministration().fieldConfigurations().defaultFieldConfiguration().hideFields(SELECT_CF_FIELD_CONFIG_NAME);
        getWebUnitTest().getAdministration().reIndex();

        reloadDashboardAfterChangeAndSelectGadget();
        assertHeatMapItems(new String[][] {{"Optoin 1", "1", "14"}, { STAT_NONE, "2", "28"}, { STAT_IRRELEVANT, "4", "57"}}, 7);

        // hide Select CF in Copy config too and reload dashboard
        getWebUnitTest().getAdministration().fieldConfigurations().fieldConfiguration("Copy of Default Field Configuration").hideFields(SELECT_CF_FIELD_CONFIG_NAME);
        getWebUnitTest().getAdministration().reIndex();

        reloadDashboardAfterChangeAndSelectGadget();
        assertHeatMapItems(new String[][] {{ STAT_IRRELEVANT, "7", "100"}}, 7);

        // show Select CF in default config (still hidden in Copy) and reload dashboard
        getWebUnitTest().getAdministration().fieldConfigurations().defaultFieldConfiguration().showFields(SELECT_CF_FIELD_CONFIG_NAME);
        getWebUnitTest().getAdministration().reIndex();

        reloadDashboardAfterChangeAndSelectGadget();
        assertHeatMapItems(new String[][] {{"Optoin 1", "1", "14"}, {"opt1", "1", "14"}, { STAT_NONE, "2", "28"}, { STAT_IRRELEVANT, "3", "42"}}, 7);
    }

    private void reloadDashboardAfterChangeAndSelectGadget()
    {
        selectDashboardFrame();
        getNavigator().gotoHome();
        maximizeGadget(HEATMAP_GADGET_TITLE);
        waitForGadgetView(HEATMAP_VIEW);
    }

    private void assertHeatMapItems(final String[][] stats, int total)
    {
        assertThat.elementHasText("//a[@title='All']/strong", String.valueOf(total));
        for (int i = 0; i < stats.length; i++)
        {
            String[] statRow = stats[i];
            final String statName = statRow[0];

            assertThat.elementPresentByTimeout("css=ul[class='heatmap']", 10000);
            assertThat.elementPresentByTimeout("jquery=ul[class='heatmap']>li:nth-child(" + (i + 1) +  "):contains('" + statName + "')", 10000);
        }
    }
}