/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira;

import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.util.ImportUtils;

public class TestManagerFactory extends LegacyJiraMockTestCase
{
    public TestManagerFactory(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        ManagerFactory.quickRefresh();
    }

    public void testGetCorrectIndexManagerImplementation()
    {
        IssueIndexManager indexManager = ManagerFactory.getIndexManager();
        //normal implementation
        assertTrue(indexManager.toString(), indexManager.toString().indexOf("DefaultIndexManager") != -1);

        //bulk only implementation
        ImportUtils.setIndexIssues(false);
        assertTrue(indexManager.toString(), indexManager.toString().indexOf("BulkOnlyIndexManager") != -1);

        //normal implementation again
        ImportUtils.setIndexIssues(true);
        assertTrue(indexManager.toString(), indexManager.toString().indexOf("DefaultIndexManager") != -1);
    }
}
