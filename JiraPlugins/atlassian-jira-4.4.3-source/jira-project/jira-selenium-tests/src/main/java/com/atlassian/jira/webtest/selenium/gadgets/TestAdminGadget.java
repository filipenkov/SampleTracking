package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.driver.admin.plugins.PluginsManagement;
import com.atlassian.jira.webtest.selenium.Quarantine;

import java.io.IOException;

/**
 * Selenium test for the Administration Gadget.
 * <p/>
 * For license messages and other tests around the Admin Gadget, see {@link com.atlassian.jira.webtest.selenium.admin.licenses.TestUserLimitedLicense}
 */
@WebTest({Category.SELENIUM_TEST })
@Quarantine
public class TestAdminGadget extends GadgetTest
{
    private static final String WARNING_MSG_HSQLDB = "//p[@id='hsqlWarning']";
    private static final String WARNING_MSG_BACKUP = "//p[@id='backupMessage']";
    private static final String JIM_KEY = "com.atlassian.jira.plugins.jira-importers-plugin";

    @Override
    protected void restoreGadgetData()
    {
    }

    public void testHsqlAndBackup() throws IOException
    {
        restoreDataAndAddAdminGadget("TestAdminGadgetWithBackup.xml");
        validateHsqlAndBackupWarningMessages(true);
        assertAdminLinksPresent();
    }

    public void testHsqlAndNoBackup() throws IOException
    {
        restoreDataAndAddAdminGadget("TestAdminGadgetWithNoBackup.xml");
        validateHsqlAndBackupWarningMessages(false);
        assertThat.elementVisible("//div[@class='isAdmin']");
        assertAdminLinksPresent();
    }

    public void testJim() throws IOException
    {
        restoreDataAndAddAdminGadget("TestAdminGadgetWithBackup.xml");
        assertAdminLinksPresent();
        assertThat.elementPresent("//dd[@class='jim']");
        // Now disable jim
        client.selectFrame("relative=top");
        getNavigator().gotoHome();
        PluginsManagement plugins = new PluginsManagement(globalPages());
        plugins.disableSystemPlugin(JIM_KEY);
        assertThat.elementNotPresent("//dd[@class='jim']");
    }

    // You need to ensure you can't see the gadget on a shared dashboard and that you can't add the gadget (See it in the directory).
    public void testNotSystemAdministrator()
    {
        restoreData("TestAdminGadgetNonAdmin.xml");
        getNavigator().logout(getXsrfToken());
        getNavigator().login("fred", "fred");

        // first check whether we can see the Admin gadget in the directory.
        client.click("add-gadget");
        assertThat.elementPresentByTimeout("category-all", GADGET_DIRECTORY_TIMEOUT);
        // we check for another gadget element just to be positive that there are gadgets being displayed
        assertThat.elementPresentByTimeout("macro-Introduction", 30000);
        // but the admin one is what we REALLY care about
        assertThat.elementNotPresent("macro-Admin");
        client.click("jquery=button.finish:visible");

        // delete fred's MyDashboard so that we go to the default dashboard (which has the admin gadget on it)
        client.mouseDown("css=li#dashboard-tools-dropdown a");
        client.click("css=li#dashboard-tools-dropdown a");
        client.clickAndWaitForAjaxWithJquery("css=a#delete_dashboard", 5000);
        assertThat.visibleByTimeout("delete-dshboard", 10000);
        client.clickButton("Delete", true);

        String frameId = client.getEval("this.browserbot.getCurrentWindow().jQuery('div.dashboard h3:contains("
                + "Admin" + ")').closest('.dashboard-item-frame').find('iframe').attr('id')");
        assertEquals("null", frameId);

        // JRA-18647
        // copy the default dash and make sure the admin gadget isn't there either
        client.click("copy_dashboard", 10000);
        client.typeInElementWithName("portalPageName", "My New Dash");
        client.submit("jiraform");
        waitFor(2000);
        frameId = client.getEval("this.browserbot.getCurrentWindow().jQuery('div.dashboard h3:contains("
                + "Admin" + ")').closest('.dashboard-item-frame').find('iframe').attr('id')");
        assertEquals("null", frameId);
    }

    private void validateHsqlAndBackupWarningMessages(boolean exportServiceOn)
            throws IOException
    {
        getWebUnitTest().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        boolean isHSQL = getWebUnitTest().usingHsqlDb();
        if (isHSQL)
        {
            //check there is warning
            assertThat.elementVisible(WARNING_MSG_HSQLDB);
            if (exportServiceOn)
            {
                //check there is NO warning

                assertThat.elementNotPresent(WARNING_MSG_BACKUP);
            }
            else
            {
                //check there is warning
                assertThat.elementVisible(WARNING_MSG_BACKUP);
                assertThat.linkVisibleWithText("create a backup service");
            }
        }
        else
        {
            if (exportServiceOn)
            {
                //check there is NO warning
                assertThat.elementNotPresent(WARNING_MSG_BACKUP);
            }
            else
            {
                //check there is warning
                assertThat.elementVisible(WARNING_MSG_BACKUP);
                assertThat.linkVisibleWithText("create a backup service");
            }
        }
    }

    private void assertAdminLinksPresent()
    {
        assertThat.linkVisibleWithText("backup");
        assertThat.linkVisibleWithText("Restore");
        assertThat.linkVisibleWithText("view details");
    }

    private void assertPresentButEmpty(String xpath)
    {
        assertThat.elementPresent(xpath);
        assertThat.elementNotPresent(xpath + "/text()");
    }

    private void assertPresentButInvisible(String xpath)
    {
        assertThat.elementPresent(xpath);
        assertThat.elementNotVisible(xpath);
    }

    private void restoreDataAndAddAdminGadget(String file)
    {
        restoreData(file);
        addGadget("Admin");
        assertThat.elementPresentByTimeout("css=.g-admin", 10000);
    }
}
