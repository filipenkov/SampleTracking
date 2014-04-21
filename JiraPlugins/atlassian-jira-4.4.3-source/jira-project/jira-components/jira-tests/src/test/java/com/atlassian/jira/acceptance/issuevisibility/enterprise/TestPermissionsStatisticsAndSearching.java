/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.acceptance.issuevisibility.enterprise;

import com.atlassian.jira.acceptance.WrapTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.jelly.tags.junit.JellyTestSuite;

public class TestPermissionsStatisticsAndSearching extends EnterpriseTestPermissionsStatisticsAndSearching
{
    public TestPermissionsStatisticsAndSearching()
    {
        super("TestPermissionsStatisticsAndSearchingEnterprise");
    }

    public static Test suite() throws Exception
    {
        TestPermissionsStatisticsAndSearching testPermissionsStatisticsAndSearching = new TestPermissionsStatisticsAndSearching();
        TestSuite suite = new WrapTestSuite(testPermissionsStatisticsAndSearching);
        suite.addTest(JellyTestSuite.createTestSuite(TestPermissionsStatisticsAndSearching.class, "test-permissions-statistics-and-searching.test.enterprise.jelly"));
        return suite;
    }

    protected String getOutputFileName()
    {
        return "enterprise.xml";
    }
}
