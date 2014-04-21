/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.acceptance.issuevisibility.enterprise;

import com.atlassian.jira.acceptance.WrapTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.jelly.tags.junit.JellyTestSuite;

public class TestPermissionsStatisticsAndSearchingWithFlush extends EnterpriseTestPermissionsStatisticsAndSearching
{
    public TestPermissionsStatisticsAndSearchingWithFlush()
    {
        super("TestPermissionsStatisticsAndSearchingEnterpriseWithFlush");
    }

    public static Test suite() throws Exception
    {
        TestPermissionsStatisticsAndSearchingWithFlush testPermissionsStatisticsAndSearching = new TestPermissionsStatisticsAndSearchingWithFlush();
        TestSuite suite = new WrapTestSuite(testPermissionsStatisticsAndSearching);
        suite.addTest(JellyTestSuite.createTestSuite(TestPermissionsStatisticsAndSearchingWithFlush.class, "test-permissions-statistics-and-searching.test.enterprise.jelly"));
        return suite;
    }

    protected String getOutputFileName()
    {
        return "enterprise-with-flush.xml";
    }
}
