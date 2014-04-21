package com.atlassian.jira.webtest.selenium;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.dump.ArtifactDumper;
import com.atlassian.jira.functest.framework.dump.TestInformationKit;
import com.atlassian.jira.functest.framework.log.FuncTestLoggerImpl;
import com.atlassian.jira.functest.framework.log.FuncTestOut;
import com.atlassian.jira.functest.framework.log.LogOnBothSides;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.atlassian.jira.functest.framework.util.testcase.TestCaseKit;
import com.atlassian.jira.functest.framework.xmlbackup.XmlBackupCopier;
import com.atlassian.jira.webtest.framework.impl.selenium.core.DefaultTimeouts;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.GlobalPages;
import com.atlassian.jira.webtest.selenium.framework.SeleniumClosure;
import com.atlassian.jira.webtest.selenium.framework.Window;
import com.atlassian.jira.webtest.selenium.harness.SeleniumTestSuiteBuilder;
import com.atlassian.jira.webtest.selenium.harness.util.Administration;
import com.atlassian.jira.webtest.selenium.harness.util.AdministrationImpl;
import com.atlassian.jira.webtest.selenium.harness.util.Navigator;
import com.atlassian.jira.webtest.selenium.harness.util.NavigatorImpl;
import com.atlassian.jira.webtest.selenium.harness.util.UserPreferences;
import com.atlassian.jira.webtest.selenium.harness.util.UserPreferencesImpl;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.LicenseKeys;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SeleniumConfiguration;
import com.atlassian.selenium.SeleniumTest;
import com.thoughtworks.selenium.SeleniumException;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class JiraSeleniumTest extends SeleniumTest implements EnvironmentAware
{
    private static final Logger log = Logger.getLogger(JiraSeleniumTest.class);

    public final Logger logger = Logger.getLogger(JiraSeleniumTest.class);

    protected static final int PRESENT_TO_VISIBLE_DELAY = 2500;
    protected static final int DROP_DOWN_WAIT = 5000;

    public static final String VK_DOWN = "\\40";
    public static final String VK_UP = "\\38";
    public static final String VK_ESC = "\\27";

    public static final String ADMIN_USERNAME = FunctTestConstants.ADMIN_USERNAME;
    public static final String ADMIN_PASSWORD = FunctTestConstants.ADMIN_PASSWORD;

    protected static final String HSP_1 = "HSP-1";
    protected static final String MKY_1 = "MKY-1";

    private static boolean maximisedWindow = false;

    /**
     * A member variable back to the Selenium interface
     */
    //protected Selenium client;
    protected JIRAEnvironmentData environmentData;

    /**
     * Utility classes to help with test runs.
     */
    private Administration administration;
    private Navigator navigator;
    private UserPreferences userPreferences;

    private SeleniumContext context;
    private JiraSeleniumTestListener listener = new SafeJiraSeleniumTestListener();

    private SeleneseJIRAWebTest seleneseJiraWebTest;
    private final XmlBackupCopier xmlBackupCopier;

    protected static final long PAGE_LOAD_WAIT_TIME = 100000;
    protected static final String PAGE_LOAD_WAIT = Long.toString(PAGE_LOAD_WAIT_TIME);


    public JiraSeleniumTest()
    {
        this(SeleniumAcceptanceTestHarness.getLocalTestEnvironmentData());
    }

    public JiraSeleniumTest(JIRAEnvironmentData environmentData)
    {
        this.environmentData = environmentData;
        this.xmlBackupCopier = new XmlBackupCopier(environmentData.getBaseUrl());
    }

    public void log(String msg)
    {
        logger.info(msg);
    }

    public void onSetUp()
    {
        internalSetup();
    }

    protected void internalSetup()
    {
        initUtilityClasses();

        //Maximise the client window but only do it once.
        if (!maximisedWindow)
        {
            client.windowMaximize();
            maximisedWindow = true;
        }

        //this is a bit of a hack since chooseOkOnNextConfirmation() doesn't work when
        // confirmation is onload onunload or beforeunload
        client.getEval("this.browserbot.getCurrentWindow().atlassianSeleniumTesting = {}");
        log("Running : " + getName());
    }

    private void captureScreenshot()
    {
        try
        {
            FuncTestOut.log("Current browser location: '" + client.getLocation() + "'");
            String captureDirectory = EnvironmentUtils.getMavenAwareOutputDir();
            final String testName = TestCaseKit.getFullName(this);
            final String fileName = captureDirectory + "/" + testName + ".png";
            final String htmlFilename = captureDirectory + "/" + testName + ".html";
            log("Trying to capture screenshot and saving in file + " + fileName);
            client.captureEntirePageScreenshot(fileName, "");
            String html = client.getHtmlSource();
            final File out = new File(htmlFilename);
            FileUtils.writeStringToFile(out, html);

            ArtifactDumper.openBrowser(environmentData.getProperty("imageviewer.path"), new File(fileName), new FuncTestLoggerImpl());
        }
        catch (RuntimeException ex)
        {
            logger.warn("Error on captureScreenshot.", ex);
        }
        catch (IOException e)
        {
            logger.warn("Error when writing HTML source.", e);
        }
    }

    /**
     * The outer most edge of a JUnit Test.  All things start and end here.
     *
     * @see junit.framework.TestCase#runBare()
     */
    public void runBare() throws Throwable
    {
        long startTime = System.currentTimeMillis();
        LogOnBothSides.log(environmentData, TestInformationKit.getStartMsg(this, environmentData.getTenant()));
        try
        {
            runBareTestCase();
            LogOnBothSides.log(environmentData, TestInformationKit.getEndMsg(this, environmentData.getTenant(), System.currentTimeMillis() - startTime));
        }
        catch (Throwable throwable)
        {
            LogOnBothSides.log(environmentData, TestInformationKit.getEndMsg(this, environmentData.getTenant(), System.currentTimeMillis() - startTime, throwable));
            throw throwable;
        }
        finally
        {
            client = null;
            environmentData = null;
            administration = null;
            navigator = null;
            userPreferences = null;
            context = null;
        }
    }

    /**
     * This is a basicly copy and paste of TestCase.runBare(), except we capture a screenshot if the execution of the
     * test fails.
     *
     * @throws Throwable on error
     */
    protected void runBareTestCase() throws Throwable
    {
        setUp();
        try
        {
            runTest();
        }
        catch (Throwable throwable)
        {
            captureScreenshot();
            throw throwable;
        }
        finally
        {
            tearDown();
        }
    }

    protected void restoreData(String file)
    {
        restoreData(environmentData.getXMLDataLocation().getAbsolutePath(), file);
    }

    protected final void restoreDataWithPluginsReload(String file)
    {
        restoreData(environmentData.getXMLDataLocation().getAbsolutePath(), file, true);
    }

    protected final void restoreData(String path, String file)
    {
        restoreData(path, file, false);
    }

    protected void restoreData(String path, String file, boolean reloadPlugins)
    {
        doRestore(path + "/" + file, reloadPlugins);
        try
        {
            assertThat.textPresent("Your project has been successfully imported");
        }
        catch (AssertionFailedError e)
        {
            //The following are assertions of possible error messages to display the cause of failure to import
            //instead of having to check HTML dump manually. Please add/modify new error messages not included already.
            assertCauseOfError("The xml data you are trying to import seems to be from a newer version of JIRA. This will not work.");
            assertCauseOfError("You must enter the location of an XML file.");
            assertCauseOfError("Could not find file at this location.", file);
            assertCauseOfError("Invalid license key specified.");
            assertCauseOfError("The current license is too old to install this version of JIRA");
            assertCauseOfError("Invalid license type for this edition of JIRA. License should be of type Standard.");
            assertCauseOfError("Invalid license type for this edition of JIRA. License should be of type Professional.");
            assertCauseOfError("Invalid license type for this edition of JIRA. License should be of type Enterprise.");
            assertCauseOfError("You must specify an index for the restore process.");
            assertCauseOfError("Error parsing export file. Your export file is invalid.");
            fail("Your JIRA data failed to restore successfully. See logs for details");
        }

        if (!reloadPlugins)
        {
            getNavigator().disableWebSudo();
        }

        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    protected void restoreI18nData(String file)
    {
        doRestore(environmentData.getXMLDataLocation().getAbsolutePath() + "/" + file);
        try
        {
            //check login button is there.
            assertThat.elementPresent("//*[@id=\"login\"]");
        }
        catch (AssertionFailedError e)
        {
            fail("Your JIRA data failed to restore successfully. See logs for details");
        }

        getNavigator().disableWebSudo();
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    protected void restoreDataWithReplacedTokens(String file, Map<String, String> replacements)
    {
        final String resource = environmentData.getXMLDataLocation().getAbsolutePath() + "/" + file;
        File newData = null;
        try
        {
            String xml = FileUtils.readFileToString(new File(resource));
            xml = replaceTokens(xml, replacements);

            // write new data to temp file
            newData = File.createTempFile(file, ".xml");
            final FileWriter of = new FileWriter(newData);
            of.write(xml);
            of.close();

            restoreData(newData.getParent(), newData.getName());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (newData != null)
            {
                if (newData.exists())
                {
                    assert newData.delete();
                }
            }
        }
    }

    protected void restoreBlankInstance()
    {
        restoreData("blankprojects.xml");
    }

    protected void restoreUnsetupJIRA()
    {
        doRestore(environmentData.getXMLDataLocation().getAbsolutePath() + "/TestEmpty.xml");
    }

    private void doRestore(final String filename)
    {
        doRestore(filename, false);
    }

    private void doRestore(String filename, boolean reloadPluginsSystem)
    {
        File file = new File(filename);
        File jiraImportDirectory = new File(getWebUnitTest().getAdministration().getJiraHomeDirectory(), "import");
        File destinationPath = new File(jiraImportDirectory, file.getName());
        if (file.getAbsolutePath().endsWith(".zip"))
        {
            try
            {
                FileUtils.copyFile(file, destinationPath);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Could not copy file " + file.getAbsolutePath() + " to the import directory in jira home " + jiraImportDirectory, e);
            }
        }
        else
        {
            boolean wasReplaced = xmlBackupCopier.copyXmlBackupTo(file.getAbsolutePath(), destinationPath.getAbsolutePath());
            if (!wasReplaced)
            {
                log.info(String.format("Unable to replace base URL in XML backup '%s'", destinationPath));
            }
        }

        if (seleneseJiraWebTest == null)
        {
            //ensures JIRA will be setup before doing the first restore of data.
            getWebUnitTest();
        }

        waitForOtherRequestsToDie();

        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().disableWebSudo();
        getNavigator().gotoPage("secure/admin/XmlRestore!default.jspa", true);
        listener.startRestore(this, filename);
        client.type("filename", file.getName());
        client.type("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        if (!reloadPluginsSystem)
        {
            client.check("quickImport", "true");
        }
        client.click("//*[@id=\"restore_submit\"]");
        client.waitForPageToLoad(PAGE_LOAD_WAIT_TIME);

        waitForRestore();
    }

    protected void waitForRestore()
    {
        //wait for result page to come up
        while(client.isElementPresent("refresh_submit"))
        {
            try
            {
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
            }
            client.refresh();
            client.waitForPageToLoad(PAGE_LOAD_WAIT_TIME);
        }
    }

    /**
     * Here we loop until all other requests have died.  If it takes too long (30 seconds) we throw an exception.
     *
     */
    private void waitForOtherRequestsToDie()
    {
        int retryCount = 0;
        while(!isLoggedInAndOnlyOneRequest())
        {
            log.info("Waiting on multiple requests to finish. Attempt:" + retryCount++);
            if (retryCount > 60) // 30 secs
            {
                // TODO DEVSPEED-110 - we should trigger a server thread dump when this happens
                throw new RuntimeException("Timed out with mystery request in background.");
            }
            try
            {
                // lets have a nap and try again.
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

        }

    }

    /**
     * Here we assert that there are no other requests happening and that we are logged in.
     *
     * @return true, if concurrent requests = 1, false other wise.
     */
    private boolean isLoggedInAndOnlyOneRequest()
    {
        try
        {
            getNavigator().gotoPage("rest/func-test/1.0/concurrentRequestInfo", true);
            try
            {
                assertThat.elementPresent("id=jiraConcurrentRequests");
            }
            catch (AssertionFailedError e)
            {
                //if we're not logged in, login!
                getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
                getNavigator().gotoPage("rest/func-test/1.0/concurrentRequestInfo", true);
            }
            assertThat.elementPresent("id=jiraConcurrentRequests");
            Assert.assertEquals("More than one background request still running","1",client.getValue("id=jiraConcurrentRequests"));
        }
        catch (AssertionFailedError e)
        {
            return false;
        }

        return true;
    }

    /**
     * Check that the errorMessage is not present, if it is present the error message is the cause of the failure.
     * params is just to provide additional info that may be helpful in understanding the cause of the failure. Eg.
     * displaying the import file name when the import fails to find the file
     *
     * @param errorMessage the error message to check for. If found on the page the test will fail.
     * @param params any extra information to include with the failure.
     */
    private void assertCauseOfError(String errorMessage, String... params)
    {
        try
        {
            assertThat.textNotPresent(errorMessage);
        }
        catch (AssertionFailedError e)
        {
            fail("Failed to restore JIRA data. Cause: " + errorMessage + (params != null ? " [" + Arrays.toString(params) + "]" : ""));
        }
    }

    private String replaceTokens(String source, final Map<String, String> replacements)
    {
        for (final Map.Entry<String, String> entry : replacements.entrySet())
        {
            final Matcher matcher = Pattern.compile(entry.getKey(), Pattern.LITERAL).matcher(source);
            if (!matcher.find())
            {
                Assert.fail("Replacement token '" + entry.getKey() + "' not found");
            }
            source = matcher.replaceAll(Matcher.quoteReplacement(entry.getValue()));
        }
        return source;
    }

    /**
     * If you put a class in your test unit class to this then IDEA can run the test in one suite rather than setting it
     * up and pulling it dfown completely for each test method.  This will make running Selenium tests in IDEA and order
     * of magnitude faster.
     * <p/>
     * <code> public static Test suite() { return suiteFor(YourSeleniumUnitTest.class); } </code>
     *
     * @param testSuiteClass the unit test class to run
     * @return a Test suit built by {@link com.atlassian.jira.webtest.selenium.harness.SeleniumTestSuiteBuilder }
     */
    protected static Test suiteFor(Class testSuiteClass)
    {
        return SeleniumTestSuiteBuilder.getTest(testSuiteClass);
    }

    /**
     * Checks the current JDK version from the JIRA system information page.
     * <p/>
     * NOTE: This will move the current page to the JIRA system informatoin page.
     *
     * @return true if the JDK version is before 1.5
     */
    protected boolean isBeforeJdk15()
    {
        return getWebUnitTest().isBeforeJdk15();
    }

    public JIRAEnvironmentData getEnvironmentData()
    {
        return environmentData;
    }

    public void setEnvironmentData(JIRAEnvironmentData environmentData)
    {
        this.environmentData = environmentData;
    }

    public void setSeleniumTestListener(JiraSeleniumTestListener listener)
    {
        this.listener = new SafeJiraSeleniumTestListener(listener);
    }

    protected JIRAWebTest getWebUnitTest()
    {
        if (seleneseJiraWebTest == null)
        {
            seleneseJiraWebTest = new SeleneseJIRAWebTest(getName());
            seleneseJiraWebTest.setEnvironmentData(environmentData);
            seleneseJiraWebTest.init();
        }
        return seleneseJiraWebTest;
    }

    /**
     * non-abstract implementation.
     */
    private class SeleneseJIRAWebTest extends JIRAWebTest
    {

        public SeleneseJIRAWebTest(String name)
        {
            super(name);
        }
    }

    private void initUtilityClasses()
    {
        context = new SeleniumContext(client, assertThat, config, environmentData);
        administration = new AdministrationImpl(context());
        navigator = new NavigatorImpl(context());
        userPreferences = new UserPreferencesImpl(client, environmentData, navigator, assertThat);
    }

    public Administration getAdministration()
    {
        return administration;
    }

    public Navigator getNavigator()
    {
        return navigator;
    }

    public UserPreferences getUserPreferences()
    {
        return userPreferences;
    }

    protected final DefaultTimeouts timeouts()
    {
        return context.timeouts();
    }

    protected final SeleniumContext context()
    {
        return context;
    }

    protected final GlobalPages globalPages()
    {
        return context.globalPages();
    }

    public void turnOnProfiling()
    {
        getNavigator().gotoPage("secure/admin/jira/ViewLogging!enableProfiling.jspa", true);
    }

    public void turnOffProfiling()
    {
        getNavigator().gotoPage("secure/admin/jira/ViewLogging!disableProfiling.jspa", true);
    }

    public boolean isUserAgentFirefox()
    {
        return Browser.typeOf(config.getBrowserStartString()) == Browser.FIREFOX;
    }

    /**
     * Waits for the specified number of milliseconds.  A glorified Thread.sleep().  Use this sparingly! If you want to
     * assert that something is shown after a timeout, use {@link com.atlassian.selenium.SeleniumAssertions} instead!
     * <p/>
     * This method should only be used when asserting the negative case (something doesn't show up after a timeout)
     *
     * @param millis - how long to wait
     * @deprecated there is always something on the page to wait for (incl. non-existence of something)!!!
     * usage of this method will lead to slow tests with intermittent failures
     * @see com.atlassian.jira.webtest.framework.core.condition.TimedCondition
     */
    @Deprecated
    public void waitFor(int millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Thread was interrupted", e);
        }
    }

    public SeleniumConfiguration getSeleniumConfiguration()
    {
        return new JiraSeleniumConfiguration(getEnvironmentData());
    }

    /**
     * Use this instead of visibleByTimeout when waiting for an ajax call to complete. This is similar to
     * assertThat.visibleByTimeout, except that instead of (selenium.isElementPresent(element) &&
     * selenium.isVisible(element)), elementPresentByTimeout is used first, then a delay (See {@link
     * #PRESENT_TO_VISIBLE_DELAY}), before visibleByTimeout is called.
     *
     * @param locator Selenium element locator
     * @param timeout timeout to use for present and visible timeouts
     */
    protected void visibleByTimeoutWithDelay(String locator, long timeout)
    {
        assertThat.elementPresentByTimeout(locator, timeout);

        // There seems to be a small delay between when selenium detects an element is present
        // and when it realises the element is visible, so we want to wait for some time
        // if the element isn't already visible. Unfortunately, the act of checking whether
        // an element is visible will throw an exception if the element hasn't appeared yet.
        //
        // So, we check if the element is visible. If it is, we're all good. If it's not visible
        // we catch the thrown exception and wait.
        boolean elementVisible = false;
        try
        {
            elementVisible = client.isVisible(locator);
        }
        catch (SeleniumException se)
        {
            // Couldn't find the element, or element not visible
        }
        if (!elementVisible)
        {
            waitFor(PRESENT_TO_VISIBLE_DELAY);
            assertThat.visibleByTimeout(locator, timeout);
        }
        // Element was visible, good to go
    }

    public static interface Check extends Callable<Boolean>
    {

    }

    private void assertState(String message, Check assertion, Boolean state, int timeout)
    {
        long targetMillies = System.currentTimeMillis() + timeout;

        while (targetMillies > System.currentTimeMillis())
        {
            try
            {
                if (assertion.call() == state)
                {
                    return;
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        fail(message);
    }

    public void assertFalse(String message, Check assertion)
    {
        assertState(message, assertion, false, 1000);
    }

    public void assertTrue(String message, Check assertion)
    {
        assertState(message, assertion, true, 1000);
    }

    public void assertFalse(String message, Check assertion, int timeout)
    {
        assertState(message, assertion, false, timeout);
    }

    public void assertTrue(String message, Check assertion, int timeout)
    {
        assertState(message, assertion, true, timeout);
    }

    public String getXsrfToken()
    {
        return client.getAttribute("//meta[@id='atlassian-token']@content");
    }

    protected final void backgroundLogout() throws Exception
    {
        final String xsrf = getXsrfToken();
        Window.withNewWindow(client, "", "logout", new SeleniumClosure()
        {
            public void execute() throws Exception
            {
                getNavigator().logout(xsrf);
            }
        });
    }

    protected final void selectMainWindow()
    {
        client.selectWindow(null);
        client.windowFocus();
    }

    private static class SafeJiraSeleniumTestListener implements JiraSeleniumTestListener
    {
        private final JiraSeleniumTestListener delegate;

        public SafeJiraSeleniumTestListener()
        {
            this(null);
        }

        public SafeJiraSeleniumTestListener(final JiraSeleniumTestListener delegate)
        {
            this.delegate = delegate;
        }

        public void startRestore(final JiraSeleniumTest test, final String file)
        {
            if (delegate != null)
            {
                try
                {
                    delegate.startRestore(test, file);
                }
                catch (Exception e)
                {
                    log.error("Error occurred while handling 'startRestore' event.", e);
                }
            }
        }
    }
}
