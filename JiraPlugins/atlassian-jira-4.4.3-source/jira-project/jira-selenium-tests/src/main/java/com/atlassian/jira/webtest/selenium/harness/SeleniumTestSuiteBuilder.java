package com.atlassian.jira.webtest.selenium.harness;

import com.atlassian.jira.webtest.selenium.SeleniumAcceptanceTestHarness;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * ONe problem with running Selenium test in IDEA is that it setups and tears down the browser every time for each test method
 * This will help prevent this and make runing tests in IDEA more efficient.
 */
public class SeleniumTestSuiteBuilder
{

    private static final SeleniumTestSuiteBuilder INSTANCE = new SeleniumTestSuiteBuilder();

    private SeleniumTestSuiteBuilder()
    {
    }

    private TestSuite getSuite(Collection testSuiteClasses)
    {
        TestSuite suite = new JiraSeleniumTestSuite(SeleniumAcceptanceTestHarness.getLocalTestEnvironmentData());

        for (Iterator iterator = testSuiteClasses.iterator(); iterator.hasNext();)
        {
            Class testSuiteClass = (Class) iterator.next();
            suite.addTestSuite(testSuiteClass);
        }

        return suite;

    }

    /**
     * Builds a Selenium based Test to can run unit test classes in IDEA as a one shot test.
     *
     * @param testSuiteClass the class of the Test to run
     * @return a Test that can be launched by a TestRunner like IDEA
     */
    public static Test getTest(Class testSuiteClass)
    {
        Collection tests = new ArrayList();
        tests.add(testSuiteClass);
        return INSTANCE.getSuite(tests);
    }

    /**
     * Builds a Selenium based Test to can run unit test classes in IDEA as a one shot test.
     *
     * @param testSuiteClasses a collection of unit test classes to add in the Test suite
     * @return a Test that can be launched by a TestRunner like IDEA
     */
    public static Test getTest(Collection /*<Class>*/ testSuiteClasses)
    {
        return INSTANCE.getSuite(testSuiteClasses);
    }

}
