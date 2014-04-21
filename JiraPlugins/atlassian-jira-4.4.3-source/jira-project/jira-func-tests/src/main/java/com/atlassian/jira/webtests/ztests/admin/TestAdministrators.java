package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.xml.sax.SAXException;

/**
 * Tests the Administrators action.
 *
 * @since v3.12
 */
@WebTest ({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestAdministrators extends JIRAWebTest
{
    private static final String XPATH_ADMIN = "//ul[@id='adminlist']//a";
    private static final String XPATH_SYS_ADMIN = "//ul[@id='sysadminlist']//a";

    public TestAdministrators(String name)
    {
        super(name);
    }

    public void testDummy() {}

//    Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
    public void _testWithBothSystemAdminsAndAdmins() throws SAXException
    {
        try
        {
            //When we do this, "admin" no longer has import permission.
            restoreData("TestWithSystemAdmin.xml");
            gotoPage("/secure/Administrators.jspa");

            text.assertTextPresent(new XPathLocator(tester, XPATH_ADMIN), ADMIN_FULLNAME);
            text.assertTextPresent(new XPathLocator(tester, XPATH_SYS_ADMIN), "Root");
        }
        finally
        {
            //Need to change back to consistent data where "admin" is a system admin. If we don't all the other tests
            //will fail.
            navigation.login(SYS_ADMIN_USERNAME);
            administration.restoreBlankInstance();
            navigation.login(ADMIN_USERNAME);
        }
    }

//    Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
    public void _testWithOnlySysAdmins() throws SAXException
    {
        restoreBlankInstance();
        gotoPage("/secure/Administrators.jspa");
        text.assertTextPresent(new XPathLocator(tester, XPATH_SYS_ADMIN), ADMIN_FULLNAME);
        assertions.assertNodeDoesNotExist(XPATH_ADMIN);
    }

//    Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
    public void _testMailAddressesNormal()
    {
        restoreBlankInstance();

        gotoDashboard();

        clickLinkWithText("Contact Administrators");

        assertLinkPresent(ADMIN_USERNAME);
        assertTextPresent("admin@example.com");
    }

//    Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
    public void _testMailAddressesHidden()
    {
        restoreBlankInstance();

        // Edit the email setting to hide
        editEmailVisibility("hide");

        clickLinkWithText("Contact Administrators");

        assertLinkNotPresent("admin");
        assertTextNotPresent("admin@example.com");
    }

//    Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
    public void _testMailAddressesMasked()
    {
        restoreBlankInstance();

        // Edit the email setting to mask
        editEmailVisibility("mask");

        clickLinkWithText("Contact Administrators");

        assertLinkPresent("admin");
        assertTextPresent("admin at example dot com");
    }

//    Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
    public void _testMailAddressesLoggedInUsersOnly()
    {
        try
        {
            restoreBlankInstance();
            // Edit the email setting to logged in only
            editEmailVisibility("user");

            clickLinkWithText("Contact Administrators");

            assertLinkPresent("admin");
            assertTextPresent("admin@example.com");

            // logout and make sure it is not around any more
            logout();
            clickLinkWithText("Contact Administrators");
            assertLinkNotPresent("admin");
            assertTextNotPresent("admin@example.com");
        }
        finally
        {
            login(ADMIN_USERNAME);
        }

    }

    private void editEmailVisibility(String visibility)
    {
        gotoAdmin();
        clickLink("general_configuration");
        clickLinkWithText("Edit Configuration");
        setFormElement("title", "jWebTest JIRA installation");
        checkCheckbox("emailVisibility", visibility);
        submit("Update");
    }
}