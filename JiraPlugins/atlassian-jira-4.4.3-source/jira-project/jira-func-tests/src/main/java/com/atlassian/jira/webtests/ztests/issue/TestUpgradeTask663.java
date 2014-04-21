package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 *
 * @since v4.4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.UPGRADE_TASKS })
public class TestUpgradeTask663 extends FuncTestCase
{
    private final String MKY3 = "MKY-3";
    private final String MKY5 = "MKY-5";

    private final String HSP2 = "HSP-2";
    private final String HSP8 = "HSP-8";
    private final String HSP9 = "HSP-9";
    private final String HSP10 = "HSP-10";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestUpgradeTask663.xml");
    }

    public TestUpgradeTask663(String name)
    {
        this.setName(name);
    }

    public void testDuplicatesAreRemoved()   throws Exception
    {
        navigation.issueNavigator().displayAllIssues();
        tester.assertLinkPresentWithText(MKY5);
        tester.assertLinkPresentWithText(HSP8);
        tester.assertLinkPresentWithText(HSP9);
        tester.assertTextNotPresent(MKY3);
        tester.clickLinkWithText(HSP2);
    }

    public void testMaxProjectCount() throws Exception
    {
        navigation.issue().createIssue("homosapien", "Bug", "Next Bug");
        navigation.issueNavigator().displayAllIssues();
        tester.assertLinkPresentWithText(HSP10);
    }
}
