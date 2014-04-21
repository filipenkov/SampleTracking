package com.atlassian.jira.webtest.selenium.setup;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

import java.net.URLEncoder;

/**
 * Test the javascript on the Setup page.
 * <p/>
 * Tests the automatic fetching of licenses from MAC. Run as selenium test because basic javascript is needed to grab
 * the values from the form.
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestSetup extends JiraSeleniumTest
{
    private static final String ADMIN_FULL_NAME = "Mary Magdelene";

    public void onSetUp()
    {
        super.onSetUp();
        restoreUnsetupJIRA();
    }

    public void testSetupByFetchingLicenseFromMAC()
    {
        _doSetupStep2FetchLicenseFromMAC();
        _doSetupStep3and4();
    }

    public void testImportSetupByFetchingLicenseFromMAC()
    {
        getNavigator().gotoPage("secure/SetupImport!default.jspa", true);
        assertThat.textPresent("Import Existing Data");
        assertThat.textPresent("This setup page is to import existing data from another JIRA installation.");

        String xmlFileAbsolutePath = environmentData.getXMLDataLocation().getAbsolutePath() + "/TestXMLRestore.xml";
        client.type("filename", xmlFileAbsolutePath);
        _fetchLicenseFromMAC();

        assertThat.attributeContainsValue("filename", "value", xmlFileAbsolutePath);
        assertFalse(client.getText("license").isEmpty());
        
        client.clickButtonWithName("import", true);
        waitForRestore();
        // Incase the paths specified in XML import don't exist in tomcat
        if (client.isTextPresent("Either create the paths shown below and reimport, or"))
        {
            client.click("reimport", true);
            waitForRestore();
        }
        assertThat.textPresent("Setup Complete");

        // Simple check to make sure XML has been successfully imported
        getNavigator().disableWebSudo();
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        client.click("browse_link", true);
        assertThat.textPresent("homosapien");
        assertThat.textPresent("monkey");

    }

    private void _gotoSetupStep1()
    {
        getNavigator().gotoPage("secure/Setup.jspa", true);
        assertThat.textPresent("Step 2 of 4: Application properties");
    }

    private void _doSetupStep2FetchLicenseFromMAC()
    {
        _gotoSetupStep1();

        // Fill in mandatory fields
        client.type("title", "TestSetup JIRA");
        _fetchLicenseFromMAC();

        // Check we're back in JIRA with fields filled in properly
        assertThat.textPresent("Step 2 of 4: Application properties");
        assertThat.attributeContainsValue("title", "value", "TestSetup JIRA");
        assertFalse(client.getText("license").isEmpty());

        // Submit Step 1 with Default paths.
        client.clickButtonWithName("next", true);
    }

    private void _doSetupStep2()
    {
        // Step 2
        assertThat.textPresent("Step 3 of 4: Administrator account");
        client.type("username", ADMIN_USERNAME);
        client.type("password", ADMIN_USERNAME);
        client.type("confirm", ADMIN_USERNAME);
        client.type("fullname", ADMIN_FULL_NAME);
        client.type("email", "admin@example.com");
        client.clickButtonWithName("next", true);
    }

    private void _doSetupStep4()
    {
        // Step 3
        assertThat.textPresent("Step 4 of 4: Email Notification");
        client.clickButtonWithName("finish", true);
        assertThat.textPresent("Setup Complete");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        assertThat.textPresent(ADMIN_FULL_NAME);
    }

    private void _doSetupStep3and4()
    {
        _doSetupStep2();
        _doSetupStep4();
    }

    /**
     * NOTE: If MAC is down, we can't fetch the license...therefore the test will FAIL!!!
     */
    private void _fetchLicenseFromMAC()
    {
        // Check license field is not pre-populated
        assertThat.elementContainsText("license", "");

        // Go to MAC to fetch the license
        client.click("fetchLicense", true);

        // If Func Test User is not logged in, enter details required to access MAC
        if (!client.isTextPresent("Func Test User"))
        {
            client.type("username", "func-test-user@atlassian.com");
            client.type("password", "0TpOK5LL");
            client.submit("//form[@id='loginform']");
            client.waitForPageToLoad(PAGE_LOAD_WAIT_TIME);
        }

        client.type("orgname", "Atlassian");
        client.clickButtonWithName("_action_evaluation", true); // This form has no name...so click button instead

        // Submit MAC's callback form to return to JIRA setup process
        // Note: We need to construct the URL from the form elements instead of submitting form as submitting it will
        // open a security warning confirmation box which Selenium can't seem to bypass nicely
        final String action = client.getAttribute("//form[@id='callbackform']@action");
        final String license = client.getText("//form[@id='callbackform']/textarea");
        final String licenseField = client.getAttribute("//form[@id='callbackform']/textarea@name");
        final String url = action + "?" + licenseField + "=" + URLEncoder.encode(license);
        client.open(url);
        client.waitForPageToLoad();
    }
}
