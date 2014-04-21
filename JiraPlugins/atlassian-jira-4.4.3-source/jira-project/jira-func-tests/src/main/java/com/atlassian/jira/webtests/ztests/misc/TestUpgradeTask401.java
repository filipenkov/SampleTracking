package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import junit.framework.Assert;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.IMPORT_EXPORT, Category.UPGRADE_TASKS })
public class TestUpgradeTask401 extends FuncTestCase
{

    protected void setUpTest()
    {
        //this data has the following setup:
        // * 2 resolution date custom fields
        // * several issues with the resolution date set
        // * default navigator columns with the resolution date on it
        // * user navigator column default with the resolution date on it for user admin
        // * a filter navigator column layout with the resolution date on it for user fred
        // * a portletconfiguration that uses the resolution date
        // * a filter that uses the resolution date
        //
        // Once imported, everything should use the resolution date system field, and the custom fields should be gone!

        Calendar cal = Calendar.getInstance();
        cal.set(2008, 9, 29, 0, 0, 0);
        String fromDate = Long.toString(cal.getTimeInMillis());
        cal.set(2008, 9, 30, 0, 0, 0);
        String toDate = Long.toString(cal.getTimeInMillis());
        Map<String, String> replacements = EasyMap.build("DATE_PARAM_FROM", fromDate, "DATE_PARAM_TO", toDate);
        try
        {
            administration.restoreDataWithReplacedTokens("TestUpgradeTask401.xml", replacements);
        }
        catch (IOException e)
        {
            Assert.fail("Could not restore backup with replacement data: " + e.getMessage());
        }
    }

    public void testUpgrade()
    {
        //check custom fields are gone
        navigation.gotoAdminSection("view_custom_fields");
        //check we're on the right page
        tester.assertTextPresent("View Custom Fields");
        //one resolution date field was called Daffodil the other Cancer Council
        tester.assertTextNotPresent("Daffodil");
        tester.assertTextNotPresent("Cancer");
        tester.assertTextNotPresent("Council");

        //check the issue navigator columns in the admin section
        navigation.gotoAdminSection("issue_field_columns");
        tester.assertTextPresent("Issue Navigator Default Columns");
        final TableCellLocator locator = new TableCellLocator(tester, "issuetable", 0, 11);
        text.assertTextPresent(locator, "Resolved");
        text.assertTextNotPresent(locator, "Daffodil");
        text.assertTextNotPresent(locator, "Cancer");

        //check the issue navigator columns in the navigator (i.e the user's defaults)
        navigation.issueNavigator().displayAllIssues();
        tester.assertTextPresent("Issue Navigator");
        tester.clickLinkWithText("Configure");
        tester.assertTextPresent("Issue Navigator Columns");
        text.assertTextSequence(new WebPageLocator(tester),
                "The table below shows issue fields in order of appearance in", "your", "Issue Navigator.");
        final TableCellLocator locator2 = new TableCellLocator(tester, "issuetable", 0, 11);
        text.assertTextPresent(locator2, "Resolved");
        text.assertTextNotPresent(locator2, "Daffodil");
        text.assertTextNotPresent(locator2, "Cancer");

        //finally check the filter columns have been converted for user fred
        navigation.logout();
        navigation.login(FRED_USERNAME);
        navigation.issueNavigator().displayAllIssues();
        tester.clickLink("managefilters");
        tester.clickLink("filterlink_10000");
        tester.assertTextPresent("Issue Navigator");
        tester.assertTextPresent("All homosapien issues");
        final TableCellLocator locator3 = new TableCellLocator(tester, "issuetable", 0, 11);
        text.assertTextPresent(locator3, "Resolved");
        text.assertTextNotPresent(locator3, "Daffodil");
        text.assertTextNotPresent(locator3, "Cancer");

        navigation.logout();
        navigation.login(ADMIN_USERNAME);

        //Can't check the portletconfiguration was updated since that requires the charting plugin to be installed.

        //check the filter now uses the resolved date in its query instead of the custom fields
        navigation.issueNavigator().displayAllIssues();
        tester.clickLink("managefilters");
        tester.clickLink("filterlink_10010");
        tester.assertTextPresent("All issues");
        tester.assertTextPresent("Issue Navigator");
        final Locator locator4 = new WebPageLocator(tester);
        text.assertTextSequence(locator4,
                "Summary",
                "Resolved After",
                "Resolved Before",
                "Resolved", "From 1 week ago to 2 weeks from now", 
                "Sorted by", "Key descending", "Operations");
        text.assertTextNotPresent(locator4, "Daffodil");
        text.assertTextNotPresent(locator4, "Cancer");
    }

}
