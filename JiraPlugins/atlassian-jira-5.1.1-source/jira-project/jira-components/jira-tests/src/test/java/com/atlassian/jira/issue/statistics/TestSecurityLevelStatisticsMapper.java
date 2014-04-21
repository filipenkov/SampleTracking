/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.mockobjects.dynamic.Mock;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;

public class TestSecurityLevelStatisticsMapper extends ListeningTestCase
{
    @Test
    public void testEquals()
    {
        SecurityLevelStatisticsMapper sorter = new SecurityLevelStatisticsMapper(null);
        assertEquals(sorter, sorter);
        assertEquals(sorter.hashCode(), sorter.hashCode());

        Mock mockIssueSecurityLevelManager = new Mock(IssueSecurityLevelManager.class);
        mockIssueSecurityLevelManager.setStrict(true);

        SecurityLevelStatisticsMapper sorter2 = new SecurityLevelStatisticsMapper((IssueSecurityLevelManager) mockIssueSecurityLevelManager.proxy());
        assertEquals(sorter, sorter2);
        assertEquals(sorter.hashCode(), sorter2.hashCode());
        mockIssueSecurityLevelManager.verify();

        assertFalse(sorter.equals(null));
        assertFalse(sorter.equals(new Object()));
        assertFalse(sorter.equals(new IssueKeyStatisticsMapper()));
    }
}
