package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.FILTERS, Category.UPGRADE_TASKS })
public class TestFilterSharesUpgrade extends FuncTestCase
{
    public void testUpgrade()
    {
        administration.restoreData("sharedfilters/TestUpgradeSharePermissions.xml");
        navigation.manageFilters().goToDefault();
        Locator locator = getShareCell("mf_10000");
        text.assertTextPresent(locator, "Private filter");
        locator = getShareCell("mf_10001");
        text.assertTextPresent(locator, "Shared with all users");
        locator = getShareCell("mf_10002");
        text.assertTextPresent(locator, "Group: jira-users");
        locator = getShareCell("mf_10003");
        text.assertTextPresent(locator, "Group: jira-developers");
    }

    private XPathLocator getShareCell(String id)
    {
        return new XPathLocator(tester, "//tr[@id='" + id +  "']//ul[@class='shareList']");
    }
}
