package com.atlassian.jira.webtest.selenium.admin.licenses;

import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.jira.webtest.selenium.gadgets.GadgetTest;
import com.atlassian.jira.webtests.LicenseKeys;
import net.sourceforge.jwebunit.WebTester;

/**
 * Ported version of {@link com.atlassian.jira.webtests.ztests.license.TestUserLimitedLicense} that makes message assertions
 * in the admin gadget instead of the license info page.
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
@Quarantine
public class TestUserLimitedLicense extends GadgetTest
{
    @Override
    public void onSetUp()
    {
        super.onSetUp();
        addGadget("Admin");
    }

    public void testAdminPortlet_Starter()
    {
        _testAdminPortlet(LicenseKeys.V2_STARTER);
    }

    public void testAdminPortlet_CommercialLimited()
    {
        _testAdminPortlet(LicenseKeys.V2_COMMERCIAL_LIMITED);
    }

    private void _testAdminPortlet(final LicenseKeys.License license)
    {
        final Administration administration = getWebUnitTest().getAdministration();
        administration.switchToLicense(license);
        addUsersWhileUnderTheLimit("fatman");
        addUsersWhileUnderTheLimit("sadman");

        reloadDashboardAndSelectAdmin();

        assertThat.textPresentByTimeout("Projects", 5000);
        assertThat.textPresent("View all or create new");
        assertThat.linkPresentWithText("View all");
        assertThat.linkPresentWithText("create new");

        assertThat.textPresent("Users");
        assertThat.textPresent("Browse users, groups or create a new user.");
        assertThat.linkPresentWithText("users");
        assertThat.linkPresentWithText("groups");
        assertThat.linkPresentWithText("create a new user");

        assertThat.textPresent("Data");
        assertThat.textPresent("Restore or backup JIRA data as XML");
        assertThat.linkPresentWithText("Restore");
        assertThat.linkPresentWithText("backup");

        assertThat.textPresent("Setup");
        assertThat.textPresent("Configure JIRA or modify global permissions");
        assertThat.linkPresentWithText("Configure");
        assertThat.linkPresentWithText("modify global permissions");

        assertThat.textPresent("License");
        assertThat.elementHasText("id=niceName", license.getDescription());
        assertThat.linkPresentWithText("view details");
        assertThat.textPresent("(Support and updates available until");
        assertThat.textNotPresent("You have reached the number of users allowed to use JIRA by your license.");

        //lets add a user which should take us up to the user limit.
        administration.usersAndGroups().addUser("monkeyboy");
        reloadDashboardAndSelectAdmin();

        //check the correct warning is shown.
        assertThat.textPresentByTimeout("License", 5000);
        assertThat.elementHasText("id=niceName", license.getDescription());
        assertThat.linkPresentWithText("view details");
        assertThat.textPresent("You have reached the number of users allowed to use JIRA by your license.");
        assertThat.textPresent("If you require more user accounts, consider purchasing a full license.");

        //now lets exceed the limit and ensure the correct warning is shown
        administration.restoreData("TestStarterLicenseTooManyUsers.xml");
        administration.switchToLicense(license);
        reloadDashboardAndSelectAdmin();

        assertThat.textPresentByTimeout("License", 5000);
        assertThat.linkPresentWithText("your license");
        assertThat.linkPresentWithText("purchasing");
        assertThat.elementContainsText("id=userLimitMessage", "You have exceeded the number of users allowed to use JIRA under your license. "
                + "You will not be able to create any more issues in JIRA until you reduce the number of users with "
                + "the 'JIRA Users', 'JIRA Administrators' or 'JIRA System Administrators' global permissions. "
                + "If you require more user accounts, please consider upgrading your license.");
        assertThat.elementContainsText("id=personalSiteLink", "If you require more user accounts, consider purchasing a full license.");
    }

    private void reloadDashboardAndSelectAdmin()
    {
        selectDashboardFrame();
        getNavigator().gotoHome();
        selectGadget("Admin");
    }

    private void addUsersWhileUnderTheLimit(final String userName)
    {
        final WebTester tester = getWebUnitTest().getTester();
        final Navigation navigation = getWebUnitTest().getNavigation();
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("add_user");

        tester.assertTextNotPresent("Creating a new user will exceed the number of users allowed to use JIRA under your license.");
        tester.setFormElement("username", userName);
        tester.setFormElement("password", userName);
        tester.setFormElement("confirm", userName);
        tester.setFormElement("fullname", userName);
        tester.setFormElement("email", userName + "@example.com");
        tester.submit("Create");
        tester.assertTextPresent("jira-users");
    }
}
