package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Func test to ensure that cron triggers are imported and converted correctly from
 * a previous version of JIRA (3.7).
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.UPGRADE_TASKS })
public class TestCronEditorUpgradeTask extends JIRAWebTest
{

    public TestCronEditorUpgradeTask(String name)
    {
        super(name);
    }


    public void setUp()
    {
        super.setUp();
        restoreDataWithFullRefresh("TestCronEditorUpgradeTask.xml");
    }

    public void tearDown()
    {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoDashboard();
        restoreBlankInstance();
    }

    /**
     * Checks that all the correct filter subscriptions are shown after import.
     */
    public void testCorrectSubscriptionsAreShown()
    {
        gotoPage("/secure/ViewSubscriptions.jspa?filterId=10000");

        /**
         * NOTE: a lot of these triggers can't be tested via a func test, since the
         * cron string is not always the same.  For example, the 23m trigger may be 0 0/20 * * * ? or
         * 0 5/20 * * * ? depending on what time the data is imported.
         */

        // 20w Simple Trigger
//        assertTextPresent("0 25 12 13 1/4 ?");
        // 1m Simple Trigger
        assertTextPresent("0 * * * * ?");
//        // 23 minute Simple Trigger
//        assertTextPresent("0 5/20 * * * ?");
//        // 4h 20m Simple Trigger
//        assertTextPresent("0 45 0/4 * * ?");
//        // 5w 2d Simple Trigger
//        assertTextPresent("The 2nd day of every month at 12:25 pm");
//        assertTextPresent("0 25 12 2 * ?");
        // 1d Simple Trigger
//        assertTextPresent("Daily at 12:25 pm");
//        assertTextPresent("0 25 12 * * ?");
    }

    public void testPermissionsForEditFilterSubscription()
    {
        //add a user we can use for testing access permission.
        addUser(FRED_USERNAME, FRED_USERNAME, FRED_USERNAME, "fred@example.com");

        //first check that we can get to the edit subscription page.
        gotoPage("/secure/FilterSubscription!default.jspa?subId=10003&filterId=10000");
        assertTextPresent("Filter Subscription");

        //then check we get prompted for a login.
        logout();
        gotoPage("/secure/FilterSubscription!default.jspa?subId=10003&filterId=10000");
        assertTextNotPresent("Filter Subscription");
        assertTextPresent("emember my login on this computer");

        //login as fred who shouldn't be able to view this subscription.
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoPage("/secure/FilterSubscription!default.jspa?subId=10003&filterId=10000");
        assertTextNotPresent("Filter Subscription");
        assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");
        logout();
    }
}
