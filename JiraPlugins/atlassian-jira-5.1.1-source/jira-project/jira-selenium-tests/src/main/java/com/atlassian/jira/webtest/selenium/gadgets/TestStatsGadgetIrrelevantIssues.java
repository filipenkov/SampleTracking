package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests the breakdown of statistics when fields are configured to be invisible for some of the filter results.
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestStatsGadgetIrrelevantIssues extends GadgetTest
{
    private static final String STAT_IRRELEVANT = "Irrelevant";
    private static final String SELECT_CF_FIELD_CONFIG_NAME = "select cf";

    @Override
    public void onSetUp()
    {
        internalSetup();
        restoreData("TestStatsGadgetIrrelevantIssues.xml");
    }

    public void testDifferentFieldConfigurations() throws Exception
    {
        // data has three field configurations: Default, Copy (which we will be changing) and Unused (to keep fields visible)
        // we are only concerned with Select CF
        // data begins with Select CF being shown in both configurations

        selectGadget("Issue Statistics");
        waitForGadgetView("content");

        assertStatsTable(new String[][] {{"opt1", "1", "14"}, {"Optoin 1", "2", "29"}, {"None", "4", "57"}}, 7);

        // hide Select CF in default config and reload dashboard
        getWebUnitTest().getAdministration().fieldConfigurations().defaultFieldConfiguration().hideFields(SELECT_CF_FIELD_CONFIG_NAME);
        getWebUnitTest().getAdministration().reIndex();

        reloadDashboardAfterChangeAndSelectGadget();
        assertStatsTable(new String[][] {{"Optoin 1", "1", "14"}, {"None", "2", "29"}, { STAT_IRRELEVANT, "4", "57"}}, 7);

        // hide Select CF in Copy config too and reload dashboard
        getWebUnitTest().getAdministration().fieldConfigurations().fieldConfiguration("Copy of Default Field Configuration").hideFields(SELECT_CF_FIELD_CONFIG_NAME);
        getWebUnitTest().getAdministration().reIndex();

        reloadDashboardAfterChangeAndSelectGadget();
        assertStatsTable(new String[][] {{ STAT_IRRELEVANT, "7", "100"}}, 7);

        // show Select CF in default config (still hidden in Copy) and reload dashboard
        getWebUnitTest().getAdministration().fieldConfigurations().defaultFieldConfiguration().showFields(SELECT_CF_FIELD_CONFIG_NAME);
        getWebUnitTest().getAdministration().reIndex();

        reloadDashboardAfterChangeAndSelectGadget();
        assertStatsTable(new String[][] {{"opt1", "1", "14"}, {"Optoin 1", "1", "14"}, {"None", "2", "29"}, { STAT_IRRELEVANT, "3", "43"}}, 7);
    }

    private void assertStatsTable(final String[][] stats, int total)
    {
        assertThat.elementHasText("//span[@id='stats-gadget-total-issues']", String.valueOf(total));
        for (int i = 0; i < stats.length; i++)
        {
            String[] statRow = stats[i];
            final int rowIdx = i + 1;
            final String statName = statRow[0];
            final String statCount = statRow[1];
            final String statPercent = statRow[2];

            if (STAT_IRRELEVANT.equals(statName))
            {
                assertThat.elementContainsText(String.format("//li[@class='stats-row'][%d]/div[@class='stats-title']/span[2]", rowIdx), statName);
            }
            else
            {
                assertThat.elementContainsText(String.format("//li[@class='stats-row'][%d]/div[@class='stats-title']/span[2]/a", rowIdx), statName);
            }
            assertThat.elementContainsText(String.format("//li[@class='stats-row'][%d]/div[@class='stats-title']/span[@class='stats-count']", rowIdx), statCount);
            assertThat.elementContainsText(String.format("//li[@class='stats-row'][%d]/div[@class='stats-percentage']//span[@class='percent-val']", rowIdx), statPercent);
        }
    }

    private void reloadDashboardAfterChangeAndSelectGadget()
    {
        selectDashboardFrame();
        getNavigator().gotoHome();
        selectGadget("Issue Statistics");
        waitForGadgetView("content");
    }
}
