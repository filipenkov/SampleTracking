package com.atlassian.jira.webtest.qunit;

import com.atlassian.aui.test.runner.QUnitSeleniumHelper;
import com.atlassian.aui.test.runner.QUnitTestResult;
import com.atlassian.jira.webtest.selenium.harness.util.Navigator;
import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * Provides some utilities for running QUnit tests.
 *
 * @since v5.0
 */
public class QUnitHelper
{
    /**
     * Sets up the test output file.
     *
     * @return
     */
    public static File setUpOutputLocation()
    {
        String location = System.getProperty("jira.qunit.testoutput.location");
        if (StringUtils.isEmpty(location))
        {
            System.err.println("Writing result XML to tmp, jira.qunit.testoutput.location not defined");
            location = System.getProperty("java.io.tmpdir");
        }

        return new File(location);
    }

    /**
     * Runs all of the QUnit tests in JIRA.
     *
     * @param navigator
     * @param seleniumHelper
     * @param testOutputDir
     * @throws Exception
     */
    public static void runAllTests(final Navigator navigator, final QUnitSeleniumHelper seleniumHelper, final File testOutputDir) throws Exception
    {
        for (final String href : getAllTestLinks(navigator, seleniumHelper))
        {
            runTest(href, seleniumHelper, testOutputDir);
        }
    }

    /**
     * Runs all of the QUnit tests in the given suite.
     * 
     * @param suiteName
     * @param navigator
     * @param seleniumHelper
     * @param testOutputDir
     * @throws Exception
     */
    public static void runSuiteTests(final String suiteName, final Navigator navigator, final QUnitSeleniumHelper seleniumHelper, final File testOutputDir) throws Exception
    {
        for (final String href : getAllTestLinks(navigator, seleniumHelper))
        {
            if (href.contains("/" + suiteName + "/"))
            {
                runTest(href, seleniumHelper, testOutputDir);
            }
        }
    }

    private static String[] getAllTestLinks(final Navigator navigator, final QUnitSeleniumHelper seleniumHelper)
    {
        navigator.gotoPage("qunit/", true);
        return seleniumHelper.findAllTestLinksOnFrontPage();
    }

    private static void runTest(final String href, final QUnitSeleniumHelper seleniumHelper, final File testOutputDir) throws Exception
    {
        final QUnitTestResult results = seleniumHelper.runTestsAtUrl(href);
        results.write(testOutputDir);
    }
}
