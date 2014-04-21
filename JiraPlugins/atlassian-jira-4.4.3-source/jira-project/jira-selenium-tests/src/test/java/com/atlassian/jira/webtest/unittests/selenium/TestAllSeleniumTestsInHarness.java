package com.atlassian.jira.webtest.unittests.selenium;

import com.atlassian.jira.functest.config.MissingTestFinder;
import com.atlassian.jira.webtest.selenium.JiraSeleniumConfiguration;
import com.atlassian.jira.webtest.selenium.SeleniumAcceptanceTestHarness;
import com.atlassian.jira.webtests.util.TestClassUtils;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SeleniumConfiguration;
import com.google.common.collect.Sets;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

/**
 * Finds any Selenium Tests that are missing from {@link com.atlassian.jira.webtest.selenium.SeleniumAcceptanceTestHarness}.
 *
 * @since v4.2
 */
public class TestAllSeleniumTestsInHarness extends TestCase
{
    private final MissingTestFinder missingTestFinder = new MissingTestFinder();
    private static final String SELENIUM_TESTS_PACKAGE = "com.atlassian.jira.webtest.selenium";
    private static final String SELENIUM_TESTS_HARNESS_NAME = "SeleniumAcceptanceTestHarness";

    public void testFindTestsMissingFromTestHarness() throws Exception
    {
        // Some tests are skipped based on the @SkipInBrowser annotation and need to be added to the ignore list
        Set<Class<? extends TestCase>> allTests = new HashSet<Class<? extends TestCase>>(TestClassUtils.getJUnit3TestClasses(SELENIUM_TESTS_PACKAGE));
        Set<Class<? extends TestCase>> testsToIgnore = getSkippedAndIgnored(allTests);

        missingTestFinder.assertAllTestsInTestHarness(SELENIUM_TESTS_PACKAGE, SELENIUM_TESTS_HARNESS_NAME,
                SeleniumAcceptanceTestHarness.SUITE.getAllTests(), testsToIgnore);
    }

    /**
     * Finds tests which were skipped due to the @SkipInBrowser annotation
     *
     * @param allTests a Set of test classes to filter
     * @return the set of tests which were skipped due to the @SkipInBrowser annotation
     */
    private Set<Class<? extends TestCase>> getSkippedAndIgnored(Set<Class<? extends TestCase>> allTests)
    {
        final Set<Class<? extends TestCase>> skippedTests = Sets.newHashSet();
        final SeleniumConfiguration config = new JiraSeleniumConfiguration(null);
        final Browser currentBrowser = Browser.typeOf(config.getBrowserStartString());

        for (Class<? extends TestCase> testClass : allTests)
        {
            if (!SeleniumAcceptanceTestHarness.shouldAddTest(testClass, currentBrowser))
            {
                skippedTests.add(testClass);
            }
        }

        return skippedTests;
    }
}