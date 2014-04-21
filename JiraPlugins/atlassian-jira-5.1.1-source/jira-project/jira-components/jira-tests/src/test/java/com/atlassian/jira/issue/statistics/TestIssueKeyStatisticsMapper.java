/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

public class TestIssueKeyStatisticsMapper extends ListeningTestCase
{

    @Test
    public void testEquals()
    {
        IssueKeyStatisticsMapper mapper = new IssueKeyStatisticsMapper();
        assertTrue(mapper.equals(mapper));
        assertEquals(mapper.hashCode(), mapper.hashCode());

        IssueKeyStatisticsMapper mapper2 = new IssueKeyStatisticsMapper();
        assertTrue(mapper.equals(mapper2));
        assertEquals(mapper.hashCode(), mapper2.hashCode());

        assertTrue(mapper.equals(new IssueKeyStatisticsMapper()));
        assertEquals(mapper.hashCode(), new IssueKeyStatisticsMapper().hashCode());

        assertFalse(mapper.equals(null));
        assertFalse(mapper.equals(new Object()));
        assertFalse(mapper.equals(new IssueTypeStatisticsMapper(null)));
    }
}
