/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.util;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.mockobjects.dynamic.Mock;

public class TestDatabaseIssuesIterable extends ListeningTestCase
{
    private Mock mockDelegator;
    private Mock mockIssueFactory;
    private DatabaseIssuesIterable issueIterable;

    @Before
    public void setUp() throws Exception
    {
        mockDelegator = new Mock(OfBizDelegator.class);
        mockDelegator.setStrict(true);

        mockIssueFactory = new Mock(IssueFactory.class);
        mockIssueFactory.setStrict(true);

        issueIterable = new DatabaseIssuesIterable((OfBizDelegator) mockDelegator.proxy(), (IssueFactory) mockIssueFactory.proxy());
    }

    @Test
    public void testToString()
    {
        assertEquals(DatabaseIssuesIterable.class.getName() + ": All issues in the database.", issueIterable.toString());

        verifyMocks();
    }

    private void verifyMocks()
    {
        mockDelegator.verify();
        mockIssueFactory.verify();
    }
}
