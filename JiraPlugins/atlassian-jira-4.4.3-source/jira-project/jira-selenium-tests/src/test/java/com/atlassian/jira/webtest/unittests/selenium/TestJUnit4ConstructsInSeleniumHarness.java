package com.atlassian.jira.webtest.unittests.selenium;

import com.atlassian.jira.functest.config.BlankIgnoresFinder;
import com.atlassian.jira.functest.config.JUnit4Suppressor;
import com.atlassian.jira.webtest.selenium.SeleniumAcceptanceTestHarness;
import junit.framework.TestCase;

/**
 * Test that tests in {@link com.atlassian.jira.webtest.selenium.SeleniumAcceptanceTestHarness} do not contain any JUnit4 annotations.
 *
 * @since v4.3
 */
public class TestJUnit4ConstructsInSeleniumHarness extends TestCase
{
    public void testNoJUnit4() throws Exception
    {
        new JUnit4Suppressor(SeleniumAcceptanceTestHarness.SUITE.getAllTests()).killJUnit4();
    }

    public void testIgnoredTestsShouldBeProvidedWithReason()
    {
        new BlankIgnoresFinder("Selenium Acceptance Test Suite", SeleniumAcceptanceTestHarness.SUITE.getAllTests())
                .assertNoIgnoresWithoutReason();
    }
}
