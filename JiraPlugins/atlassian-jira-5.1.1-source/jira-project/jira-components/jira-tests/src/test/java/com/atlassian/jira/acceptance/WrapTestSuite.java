/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.acceptance;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import com.atlassian.jira.acceptance.issuevisibility.AbstractTestPermissionsStatisticsAndSearching;

import java.util.Enumeration;

public class WrapTestSuite extends TestSuite
{
    private AbstractTestPermissionsStatisticsAndSearching outerTest;

    public WrapTestSuite(AbstractTestPermissionsStatisticsAndSearching abstractTestPermissionsStatisticsAndSearching)
    {
        this.outerTest = abstractTestPermissionsStatisticsAndSearching;
    }

    /**
     * This function is used to run the setup and tear down methods of a jelly test.
     * This should only be used for a test suite added from the the JellyTestSuite class
     * @param testResult
     */
    public void run(TestResult testResult)
    {
        try
        {
            runTests(this, testResult);
        }
        catch (Exception e)
        {
            e.printStackTrace(); //To change body of catch statement use Options | File Templates.
        }
    }

    private void runTests(TestSuite test, TestResult testResult) throws Exception
    {
        Enumeration tests = test.tests();
        while (tests.hasMoreElements())
        {
            Test innerTest = (Test) tests.nextElement();
            if (innerTest instanceof TestSuite)
            {
                runTests((TestSuite) innerTest, testResult);
            }
            else
            {
                outerTest.setUp();
                innerTest.run(testResult);
                outerTest.tearDown();
            }
        }
    }
}
