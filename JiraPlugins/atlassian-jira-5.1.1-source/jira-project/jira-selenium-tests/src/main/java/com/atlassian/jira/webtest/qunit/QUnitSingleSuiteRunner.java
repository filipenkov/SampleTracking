package com.atlassian.jira.webtest.qunit;

import com.atlassian.aui.test.runner.QUnitSeleniumHelper;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.SeleniumClient;

import java.io.File;

/**
 * Runs and reports on the QUnit tests in a single suite. Set the suite name in the system property
 * jira.qunit.single.suitename.
 *
 * @since v5.0
 */
public class QUnitSingleSuiteRunner extends JiraSeleniumTest
{
    private File testOutputDir;

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        testOutputDir = QUnitHelper.setUpOutputLocation();
        restoreBlankInstance();
    }

    public void testSingleSuite() throws Exception
    {
        final String suiteName = System.getProperty("jira.qunit.single.suitename");
        assertNotNull("Missing system property jira.qunit.single.suitename", suiteName);

        final SeleniumClient client = getSeleniumClient();
        final QUnitSeleniumHelper seleniumHelper = new QUnitSeleniumHelper(client, assertThat);
        QUnitHelper.runSuiteTests(suiteName, getNavigator(), seleniumHelper, testOutputDir);
    }
}
