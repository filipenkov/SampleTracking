package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestShareUserDefault extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestShareUserDefaults.xml");
    }
    
    public void tearDownTest()
    {
        administration.restoreBlankInstance();
    }

    public void testDefaults()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.gotoAdminSection("user_defaults");
        Locator locator = new TableCellLocator(tester, "view_user_defaults", 4, 0);
        text.assertTextPresent(locator, "Default sharing for filters and dashboards");
        locator = new TableCellLocator(tester, "view_user_defaults", 4, 1);
        text.assertTextPresent(locator, "Private");

        tester.clickLinkWithText("Edit default values");

        locator = new TableCellLocator(tester, "edit_user_defaults", 5, 0);
        text.assertTextPresent(locator, "Default sharing for filters and dashboards");
        locator = new TableCellLocator(tester, "edit_user_defaults", 5, 1);
        text.assertTextPresent(locator, "Public");
        tester.assertRadioOptionPresent("sharePublic", "true");
        tester.assertRadioOptionPresent("sharePublic", "false");
        tester.assertRadioOptionSelected("sharePublic", "true");

        tester.gotoPage("secure/ViewProfile.jspa");

        tester.clickLink("edit_prefs_lnk");

        tester.assertRadioOptionPresent("shareDefault", "true");
        tester.assertRadioOptionPresent("shareDefault", "false");
        tester.assertRadioOptionSelected("shareDefault", "true");

        navigation.gotoAdminSection("user_defaults");
        tester.clickLinkWithText("Edit default values");

        tester.assertRadioOptionPresent("sharePublic", "true");
        tester.assertRadioOptionPresent("sharePublic", "false");
        tester.checkCheckbox("sharePublic", "false");
        tester.submit("Update");

        locator = new TableCellLocator(tester, "view_user_defaults", 4, 0);
        text.assertTextPresent(locator, "Default sharing for filters and dashboards");
        locator = new TableCellLocator(tester, "view_user_defaults", 4, 1);
        text.assertTextPresent(locator, "Public");

        tester.clickLinkWithText("Edit default values");

        locator = new TableCellLocator(tester, "edit_user_defaults", 5, 0);
        text.assertTextPresent(locator, "Default sharing for filters and dashboards");
        locator = new TableCellLocator(tester, "edit_user_defaults", 5, 1);
        text.assertTextPresent(locator, "Private");
        tester.assertRadioOptionPresent("sharePublic", "true");
        tester.assertRadioOptionPresent("sharePublic", "false");
        tester.assertRadioOptionSelected("sharePublic", "false");

        tester.gotoPage("secure/ViewProfile.jspa");

        tester.clickLink("edit_prefs_lnk");

        tester.assertRadioOptionPresent("shareDefault", "true");
        tester.assertRadioOptionPresent("shareDefault", "false");
        tester.assertRadioOptionSelected("shareDefault", "false");
        tester.checkCheckbox("shareDefault", "true");

        tester.submit();

        tester.gotoPage("secure/ViewProfile.jspa");
        tester.clickLink("edit_prefs_lnk");

        tester.assertRadioOptionPresent("shareDefault", "true");
        tester.assertRadioOptionPresent("shareDefault", "false");
        tester.assertRadioOptionSelected("shareDefault", "true");

        navigation.gotoAdminSection("user_defaults");
        locator = new TableCellLocator(tester, "view_user_defaults", 4, 0);
        text.assertTextPresent(locator, "Default sharing for filters and dashboards");
        locator = new TableCellLocator(tester, "view_user_defaults", 4, 1);
        text.assertTextPresent(locator, "Public");

        tester.clickLinkWithText("Edit default values");

        locator = new TableCellLocator(tester, "edit_user_defaults", 5, 0);
        text.assertTextPresent(locator, "Default sharing for filters and dashboards");
        locator = new TableCellLocator(tester, "edit_user_defaults", 5, 1);
        text.assertTextPresent(locator, "Public");
        tester.assertRadioOptionPresent("sharePublic", "true");
        tester.assertRadioOptionPresent("sharePublic", "false");
        tester.assertRadioOptionSelected("sharePublic", "false");
    }
}
