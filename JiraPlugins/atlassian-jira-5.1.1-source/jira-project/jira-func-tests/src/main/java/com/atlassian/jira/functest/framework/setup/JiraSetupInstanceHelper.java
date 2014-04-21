package com.atlassian.jira.functest.framework.setup;

import com.atlassian.jira.functest.framework.FuncTestHelperFactory;
import com.atlassian.jira.functest.framework.FuncTestWebClientListener;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.log.FuncTestOut;
import com.atlassian.jira.functest.framework.util.text.MsgOfD;
import com.atlassian.jira.webtests.LicenseKeys;
import com.atlassian.jira.webtests.WebTesterFactory;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.TenantOverridingEnvironmentData;
import com.meterware.httpunit.PostMethodWebRequest;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.File;

/**
 * This contains common code between the old and new style func test frameworks that can detect if JIRA is setup and the
 * conditions that it can be in.
 *
 * @since v4.0
 */
public class JiraSetupInstanceHelper
{
    private static final String SYSTEM_TENANT_NAME = "_jiraSystemTenant";

    private final WebTester tester;
    private final JIRAEnvironmentData jiraEnvironmentData;

    public JiraSetupInstanceHelper(final WebTester tester, final JIRAEnvironmentData jiraEnvironmentData)
    {
        this.tester = tester;
        this.jiraEnvironmentData = jiraEnvironmentData;
    }

    public void ensureJIRAIsReadyToGo(final FuncTestWebClientListener webClientListener)
    {
        FuncTestOut.log("Checking that JIRA is setup and ready to be tested...");
        if (!isSystemTenant())
        {
            // Working with the system tenant is best done in another tester, otherwise the session fixation protection
            // in the Multi-Tenant filter will get in our way.
            TenantOverridingEnvironmentData systemTenantEnvironmentData = new TenantOverridingEnvironmentData(SYSTEM_TENANT_NAME,
                    jiraEnvironmentData);
            WebTester systemTester = WebTesterFactory.createNewWebTester(systemTenantEnvironmentData);
            JiraSetupInstanceHelper helper = new JiraSetupInstanceHelper(systemTester, systemTenantEnvironmentData);

            // First we must ensure the system tenant is set up
            helper.ensureJiraTenantIsReadyToGo(webClientListener);

            // Now ensure that the tenant we want to run the tests on is provisioned
            if (!helper.isTenantProvisioned(jiraEnvironmentData.getTenant()))
            {
                helper.provisionTenant(jiraEnvironmentData.getTenant(), jiraEnvironmentData.shouldCreateDummyTenant());
                // Clear cookies, cos it won't work if we's don't.
                tester.getTestContext().getWebClient().clearCookies();
            }
        }
        ensureJiraTenantIsReadyToGo(webClientListener);
    }

    private void ensureJiraTenantIsReadyToGo(final FuncTestWebClientListener webClientListener)
    {
        if (!isJiraSetup(webClientListener))
        {
            detectJohnson();
            synchronized (JiraSetupInstanceHelper.class)
            {
                //
                // we might be in the state where we have a database of old V1 license data.  Many of the JIRA plugin
                // tests that use pre-populated HSQL data files are in this state
                //
                if (isV1LicState())
                {
                    updateV1LicenseState(LicenseKeys.V2_COMMERCIAL);
                }
                else
                {
                    setupJIRA();
                }
            }
        }
        login(FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD);
        if (isSystemTenant())
        {
            FuncTestOut.log("JIRA is setup and 'admin' is logged in. " + new MsgOfD());
        }
        else
        {
            FuncTestOut.log("JIRA tenant '" + jiraEnvironmentData.getTenant() + "' is setup and 'admin' is logged in. " + new MsgOfD());
        }
    }

    private boolean isJiraSetup(final FuncTestWebClientListener webClientListener)
    {
        tester.beginAt("/");
        tester.getDialog().getWebClient().addClientListener(webClientListener);

        return hasBeenSetup();
    }

    private boolean isTenantProvisioned(String tenant)
    {
        // Now ensure that the tenant has been provisioned
        tester.getTestContext().getWebClient().setExceptionsThrownOnErrorStatus(false);
        try
        {
            return tester.getTestContext().getWebClient().getResponse(tester.getTestContext().getBaseUrl() + "multitenant/" + tenant).getResponseCode() == 200;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            tester.getTestContext().getWebClient().setExceptionsThrownOnErrorStatus(true);
        }
    }

    private boolean hasBeenSetup()
    {
        boolean hasBeenSetUp = (jiraEnvironmentData.getContext() + "/secure/Dashboard.jspa").equals(tester.getDialog().getResponse().getURL().getPath());
        return (hasBeenSetUp);
    }

    private void detectJohnson()
    {
        final String response = tester.getDialog().getResponseText();
        if (response.contains("JIRA Access Constraints"))
        {
            FuncTestOut.log("Test failed because we are getting the following johnson page:\n" + response);
            Assert.fail("It appears that JIRA is currenty being Johnsoned.  That cant be good!");
        }
    }

    private void setupJIRA()
    {
        FuncTestOut.log("JIRA is not setup.  Installing a new V2 license and completing setup steps");
        try
        {
            // if database set up...
            if (!isDatabaseSetUp()) {
                setupDatabase();
            }
            blatInValidLicense(LicenseKeys.V2_COMMERCIAL, "Enterprise");

            // We can jump straight from Setup 2 to Setup 4 if the admin user is already configured:
            boolean step3Skipped = true;
            if (!tester.getDialog().isTextInResponse("4 of 4"))
            {
                step3Skipped = false;
                tester.assertTextPresent("3 of 4");
                setupJiraStep3(FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD, FunctTestConstants.ADMIN_FULLNAME, FunctTestConstants.ADMIN_EMAIL);
            }
            tester.assertTextPresent("4 of 4");
            setupJiraStep4();
            // During SetupComplete, we attempt to automatically log the user in
            // If we have skipped Step 3 (creating the admin user) we cannot automatically log the user in, as the user
            // object will not be stored in the session
            if (step3Skipped)
            {
                tester.assertTextPresent("Setup Complete");
            }
            else
            {
                // Assert that the user is logged in by checking if the profile link is present
                tester.assertLinkPresent("header-details-user-fullname");
            }
        }
        catch (RuntimeException rte)
        {
            FuncTestOut.log("Unable to setup JIRA because of " + rte.getMessage());
            throw rte;
        }
    }

    private boolean isDatabaseSetUp()
    {
        final boolean dbConfig = tester.getDialog().isTextInResponse("Basic Settings");
        final boolean step1 = tester.getDialog().isTextInResponse("Step 1 of 4");
        return !(dbConfig && step1);
    }

    private void setupDatabase()
    {
        if (StringUtils.isNotEmpty(jiraEnvironmentData.getProperty("databaseType")))
        {
            FuncTestOut.log("Setting up external db");
            setupDirectJDBCConnection(tester, jiraEnvironmentData);
        }
        else
        {
            FuncTestOut.log("Setting up internal db");
            // setup internal db
            tester.checkCheckbox("databaseOption", "INTERNAL");
            tester.submit("next");
        }
        Assert.assertTrue(tester.getDialog().getResponseText(), tester.getDialog().getResponseText().contains("Step 2 of 4:"));
    }

    public static void setupDirectJDBCConnection(WebTester webTester, JIRAEnvironmentData environmentData)
    {
        webTester.checkCheckbox("databaseOption", "EXTERNAL");
        webTester.setFormElement("databaseType", environmentData.getProperty("databaseType"));
        webTester.setFormElement("jdbcHostname", environmentData.getProperty("db.host"));
        webTester.setFormElement("jdbcPort", environmentData.getProperty("db.port"));
        // SID is only used for Oracle
        webTester.setFormElement("jdbcSid", environmentData.getProperty("db.instance"));
        // Database is used for all DBs except Oracle
        webTester.setFormElement("jdbcDatabase", environmentData.getProperty("db.instance"));
        webTester.setFormElement("jdbcUsername", environmentData.getProperty("username"));
        webTester.setFormElement("jdbcPassword", environmentData.getProperty("password"));
        webTester.setFormElement("schemaName", environmentData.getProperty("schema-name"));
        webTester.submit();
    }

    private void setupJiraStep3(String username, String password, String fullName, String email)
    {
        tester.setFormElement("username", username);
        tester.setFormElement("password", password);
        tester.setFormElement("confirm", password);
        tester.setFormElement("fullname", fullName);
        tester.setFormElement("email", email);
        tester.submit();
    }

    private void setupJiraStep4()
    {
        tester.submit("finish");
    }

    private void blatInValidLicense(LicenseKeys.License licenseKey, String buildType)
    {
        logLicense(licenseKey);

        tester.setFormElement("license", licenseKey.getLicenseString());
        tester.submit();
        tester.assertTextNotPresent("Invalid license type for this edition of JIRA. License should be of type " + buildType + ".");
    }

    private void login(String username, String password)
    {
        tester.beginAt("/login.jsp");
        tester.setFormElement("os_username", username);
        tester.setFormElement("os_password", password);
        tester.setWorkingForm("login-form");
        tester.submit();
    }

    private boolean isV1LicState()
    {
        final String responseText = tester.getDialog().getResponseText();
        return responseText.contains("The current version of your license is incompatible with this JIRA installation.");
    }

    private void updateV1LicenseState(final LicenseKeys.License licenseKey)
    {
        FuncTestOut.log("JIRA has data but it contains a V1 license.  Overwriting this with a V2 license");

        tester.gotoPage("secure/ConfirmInstallationWithLicense!default.jspa");
        tester.assertTextPresent("Confirm License Update");

        tester.setWorkingForm("jiraform");

        logLicense(licenseKey);

        tester.setFormElement("userName", FunctTestConstants.ADMIN_USERNAME);
        tester.setFormElement("password", FunctTestConstants.ADMIN_PASSWORD);
        tester.setFormElement("license", licenseKey.getLicenseString());
        tester.submit();

        // now that should have fixed is up so lets ensure thats the case
        final boolean hasRecovered = hasBeenSetup();
        if (!hasRecovered)
        {
            final String message = "It was detected that a V1 license was presented in the data and we tried to upgrade it to V2 but failed";
            FuncTestOut.log(message);
            Assert.fail(message);
        }
    }

    private void logLicense(final LicenseKeys.License licenseKey)
    {
        FuncTestOut.log("Using a '" + licenseKey.getDescription() + "' license which allows " + licenseKey.getMaxUsers() + " maximum users");
    }

    private boolean isSystemTenant()
    {
        return StringUtils.isBlank(jiraEnvironmentData.getTenant()) || SYSTEM_TENANT_NAME.equals(jiraEnvironmentData.getTenant());
    }

    private void provisionTenant(String tenant, boolean createDummyTenant)
    {
        provisionTenant(tenant);
        if (createDummyTenant)
        {
            provisionTenant(tenant + "-dummy");
        }
    }

    @SuppressWarnings ( { "ResultOfMethodCallIgnored" })
    private void provisionTenant(String tenant)
    {
        FuncTestOut.log("Provisioning Tenant " + tenant);
        // ensure that the home directory exists, and set up paths so the imports work
        File homeDirFile = new File(new FuncTestHelperFactory(tester, jiraEnvironmentData)
                .getAdministration().getSystemTenantHomeDirectory(), "tenants/" + tenant);
        homeDirFile.mkdirs();
        File attachmentDir = new File(homeDirFile, "data/attachments");
        attachmentDir.mkdirs();
        String homedir = homeDirFile.getAbsolutePath();
        CharArrayWriter writer = new CharArrayWriter();
        writer.append("<tenant name=\"").append(tenant).append("\">\n");
        writer.append("  <hostnames>\n");
        writer.append("    <hostname>").append(tenant).append(".example.org</hostname>\n");
        writer.append("  </hostnames>\n");
        writer.append("  <homeDir>").append(homedir).append("</homeDir>\n");
        writer.append("  <database>\n");
        writer.append("    <name>").append(tenant).append("</name>\n");
        writer.append("    <database-type>hsql</database-type>\n");
        writer.append("    <schema-name>PUBLIC</schema-name>\n");
        writer.append("    <jdbc-datasource>\n");
        writer.append("      <url>jdbc:hsqldb:").append(homedir).append("/database</url>\n");
        writer.append("      <driver-class>org.hsqldb.jdbcDriver</driver-class>\n");
        writer.append("      <username>sa</username>\n");
        writer.append("      <password></password>\n");
        writer.append("      <pool-size>10</pool-size>\n");
        writer.append("    </jdbc-datasource>\n");
        writer.append("  </database>\n");
        writer.append("</tenant>\n");
        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes());
        PostMethodWebRequest request = new PostMethodWebRequest(tester.getTestContext().getBaseUrl() + "multitenant", is, "application/xml");
        try
        {
            tester.getTestContext().getWebClient().getResponse(request);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error provisioning tenant " + tenant, e);
        }
        FuncTestOut.log("Tenant " + tenant + " Provisioned.");
    }

}
