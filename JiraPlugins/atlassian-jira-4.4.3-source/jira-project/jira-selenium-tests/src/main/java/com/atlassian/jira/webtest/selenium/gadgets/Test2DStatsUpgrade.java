package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class Test2DStatsUpgrade extends GadgetTest
{

    @Override
    protected void restoreGadgetData()
    {
        restoreData("Test2DStatsPortletToGadget.xml");
    }

    public void testUpgrades()
    {
        _testFilterResultsUpgrade();
        _test2DStatsUpgrade();
        _test2DStatsUpgradeNoNum();
    }

    private void _testFilterResultsUpgrade()
    {
        getNavigator().gotoHome();
        client.selectFrame("gadget-10017");

        assertThat.elementPresentByTimeout("filter-results-content", TIMEOUT);
        assertThat.elementHasText("filter-results-content", "No matching issues found.");
        clickConfigButton();
        assertThat.elementPresentByTimeout("filter_filterId_name", TIMEOUT);
        assertThat.elementHasText("filter_filterId_name", "All Issues");

        assertThat.attributeContainsValue("num", "value", "50");
        assertThat.attributeContainsValue("filter_filterId_id", "value", "filter-10000");
        client.selectWindow(null);
    }

    private void _test2DStatsUpgrade()
    {
        getNavigator().gotoHome();
        client.selectFrame("gadget-10018");

        assertThat.elementPresentByTimeout("jquery=div.view:contains('The filter for this gadget did not return any issues')", 5000);

        clickConfigButton();
        assertThat.elementPresentByTimeout("filter_filterId_name", TIMEOUT);
        assertThat.elementHasText("filter_filterId_name", "All Issues");

        assertThat.attributeContainsValue("numberToShow", "value", "100");
        assertThat.attributeContainsValue("filter_filterId_id", "value", "filter-10000");
        client.selectWindow(null);
    }
    
    private void _test2DStatsUpgradeNoNum()
    {
        getNavigator().gotoHome();
        client.selectFrame("gadget-10020");

        assertThat.elementPresentByTimeout("jquery=div.view:contains('The filter for this gadget did not return any issues')", 5000);
        clickConfigButton();
        assertThat.elementPresentByTimeout("filter_filterId_name", TIMEOUT);
        assertThat.elementHasText("filter_filterId_name", "All Issues");

        assertThat.attributeContainsValue("numberToShow", "value", "5");
        assertThat.attributeContainsValue("filter_filterId_id", "value", "filter-10000");
        client.selectWindow(null);       
    }
}
