package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebResponse;
import net.sourceforge.jwebunit.TestContext;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class Test500Page extends FuncTestCase
{
    private boolean isBeforeJdk = false;

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("Test500Page.xml");
        // isBeforeJdk moves the current page to the system info page
        isBeforeJdk = new EnvironmentUtils(tester, getEnvironmentData(), navigation).isJavaBeforeJdk15();
        navigation.gotoDashboard();
    }

    public void test500PageServiceParamVisibility()
    {
        //check admins can see the service params
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        tester.gotoPage("/500page.jsp");

        //check listeners
        text.assertTextSequence(locator.page(), "ParamListeners", "com.atlassian.jira.event.listeners.DebugParamListener");
        text.assertTextSequence(locator.page(), "Param1:", "paramKey");
        text.assertTextSequence(locator.page(), "Param2:", "paramValue");

        //check services
        assertServiceHeaderPresent("Pop Service", "com.atlassian.jira.service.services.mail.MailFetcherService", "123");
        text.assertTextSequence(locator.page(), "popserver:", "fake server");
        text.assertTextSequence(locator.page(), "handler.params:", "project=hsp, issuetype=1, catchemail=sam@atlassian.com");
        text.assertTextSequence(locator.page(), "forwardEmail:", "fake@example.com");
        text.assertTextSequence(locator.page(), "handler:", "Create a new issue or add a comment to an existing issue");

        //check that non-logged in users cannot see the service params
        navigation.logout();
        tester.gotoPage("/500page.jsp");

        //check listeners
        text.assertTextSequence(locator.page(), "ParamListeners", "com.atlassian.jira.event.listeners.DebugParamListener");
        text.assertTextNotPresent(locator.page(), "Param1:");
        text.assertTextNotPresent(locator.page(), "paramKey");
        text.assertTextNotPresent(locator.page(), "Param2:");
        text.assertTextNotPresent(locator.page(), "paramValue");

        //check services
        assertServiceHeaderPresent("Pop Service", "com.atlassian.jira.service.services.mail.MailFetcherService", "123");
        assertServiceParamsNotVisible();

        //check that users with no global permission cannot see the service params
        navigation.login(BOB_USERNAME, BOB_PASSWORD);
        tester.gotoPage("/500page.jsp");

        //check listeners
        text.assertTextSequence(locator.page(), "ParamListeners", "com.atlassian.jira.event.listeners.DebugParamListener");
        text.assertTextNotPresent(locator.page(), "Param1:");
        text.assertTextNotPresent(locator.page(), "paramKey");
        text.assertTextNotPresent(locator.page(), "Param2:");
        text.assertTextNotPresent(locator.page(), "paramValue");

        //check services
        assertServiceHeaderPresent("Pop Service", "com.atlassian.jira.service.services.mail.MailFetcherService", "123");
        assertServiceParamsNotVisible();
    }

    public void test500PageContainsMemoryAndInputArgsInfo()
    {
        tester.gotoPage("/500page.jsp");

        text.assertTextPresent(locator.page(), "Used PermGen Memory");
        text.assertTextPresent(locator.page(), "Free PermGen Memory");
        text.assertTextPresent(locator.page(), "JVM Input Arguments");

        if (isBeforeJdk)
        {
            // Make sure the warning message are present
            text.assertTextPresent(locator.page(), "Unable to determine, this requires running JDK 1.5 and higher.");
        }
        else
        {
            // Make sure the warning message are not present
            text.assertTextNotPresent(locator.page(), "Unable to determine, this requires running JDK 1.5 and higher.");
        }
    }

    public void testExternalUserManagement()
    {
        tester.gotoPage("/500page.jsp");
        text.assertTextPresent(locator.page(), "External user management");
    }

    /*
     * JRA-14105 inserting some escaped HTML in the command name of an action will show up on the 500 page in the
     * stack trace - need to ensure this is escaped to prevent XSS attacks
     */
    public void testHtmlEscapingOfErrors() throws Exception
    {
        String badHtml = "<body onload=alert('XSSATTACK')>";
        String badHtmlEscaped = "%3Cbody%20onload=alert('XSSATTACK')%3E";

        GetMethodWebRequest request = new GetMethodWebRequest(getEnvironmentData().getBaseUrl() + "/secure/Dashboard!default" + badHtmlEscaped + ".jspa");
        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();

        // set this flag so that test doesn't blow up when we get a 500 error
        // note: no need to reset this flag as it is automatically reset for the next test
        webClient.setExceptionsThrownOnErrorStatus(false);

        final WebResponse response = webClient.sendRequest(request);

        String responseText = response.getText();
        assertFalse("Found bad HTML in the response", responseText.indexOf(badHtml) >= 0);
    }

    public void testAdministratorDoesntSeeContactYourAdmin()
    {
        // as admin
        tester.gotoPage("/500page.jsp");
        text.assertTextNotPresent(locator.page(), "If this problem persists, please contact your JIRA administrators.");
        text.assertTextSequence(locator.page(), "try submitting this problem via the", "Support Request Page");
    }

    public void testNonAdministratorSeesContactYourAdmin()
    {
        navigation.login(BOB_USERNAME, BOB_PASSWORD);
        tester.gotoPage("/500page.jsp");
        text.assertTextPresent(locator.page(), "If this problem persists, please contact your JIRA administrators.");
        text.assertTextNotPresent(locator.page(), "try submitting this problem via the");
        text.assertTextNotPresent(locator.page(), "Support Request Page");
    }

    public void testSystemAdministratorCanSeeSysAdminOnlyProperties()
    {
        tester.gotoPage("/500page.jsp");
        text.assertTextNotPresent(locator.page(), "Contact your System Administrator to discover file path information.");
        text.assertTextNotPresent(locator.page(), "Contact your System Administrator to discover this property value.");
        text.assertTextSequence(locator.page(), "Server ID", "ABN9-RZYJ-WI2T-37UF");

        // assert that they can see the step 3 file path
        text.assertTextSequence(locator.page(),"attach the application server log file (", "atlassian-jira.log", ")");

        text.assertTextSequence(locator.page(), "File Paths:", "entityengine.xml", "atlassian-jira.log");
        text.assertTextPresent(locator.page(), "JVM Input Arguments");
        if (!isBeforeJdk)
        {
            text.assertTextPresent(locator.page(), "-D");
        }
        text.assertTextPresent(locator.page(), "Current Working Directory");
    }

    public void testNonSystemAdministratorDoesntSeeFilePaths()
    {
        navigation.login(BOB_USERNAME, BOB_PASSWORD);
        tester.gotoPage("/500page.jsp");

        // assert that they CANT see the step 3 file path
        text.assertTextNotPresent(locator.page(), "attach the application server log file");
        text.assertTextNotPresent(locator.page(), "atlassian-jira.log");
        
        text.assertTextSequence(locator.page(),
                "Server ID", "Contact your Administrator to discover this property value.",
                "File Paths:", "Contact your System Administrator to discover file path information.",
                "Current Working Directory","Contact your System Administrator to discover this property value.",
                "JVM Input Arguments", "Contact your System Administrator to discover this property value."
        );

        text.assertTextNotPresent(locator.page(), "-Xmx"); // this shouldn't be present during tests for non sysadmin user
        navigation.login("admin_non_sysadmin", "admin_non_sysadmin");
        tester.gotoPage("/500page.jsp");
        text.assertTextSequence(locator.page(),
                "Server ID",
                "ABN9-RZYJ-WI2T-37UF", // admins can see server ids
                "File Paths:",
                "Contact your System Administrator to discover file path information.",
                "Current Working Directory",
                "Contact your System Administrator to discover this property value.",
                "JVM Input Arguments",
                "Contact your System Administrator to discover this property value.");
        text.assertTextNotPresent(locator.page(), "-Xmx"); // this shouldn't be present during tests for non sysadmin user
    }

    private void assertServiceHeaderPresent(String serviceName, String serviceClass, String delay)
    {
        text.assertTextSequence(locator.page(), "Services", serviceName, serviceClass, "Delay:", delay, "minutes");
    }

    private void assertServiceParamsNotVisible()
    {
        text.assertTextNotPresent(locator.page(), "usessl:");
        text.assertTextNotPresent(locator.page(), "No SSL");
        text.assertTextNotPresent(locator.page(), "popserver:");
        text.assertTextNotPresent(locator.page(), "fake server");
        text.assertTextNotPresent(locator.page(), "handler.params:");
        text.assertTextNotPresent(locator.page(), "project=hsp, issuetype=1, catchemail=sam@atlassian.com");
        text.assertTextNotPresent(locator.page(), "forwardEmail:");
        text.assertTextNotPresent(locator.page(), "fake@example.com");
        text.assertTextNotPresent(locator.page(), "handler:");
        text.assertTextNotPresent(locator.page(), "Create a new issue or add a comment to an existing issue");
    }
}
