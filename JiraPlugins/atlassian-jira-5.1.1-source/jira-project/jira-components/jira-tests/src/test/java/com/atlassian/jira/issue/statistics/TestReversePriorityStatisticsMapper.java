package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.priority.MockPriority;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestReversePriorityStatisticsMapper extends ListeningTestCase
{
    @Test
    public void testComparator() throws Exception
    {
        // Test the comparator works as expected and uses "reverse" ordering
        PriorityStatisticsMapper mapper = new ReversePriorityStatisticsMapper(null);
        final Comparator<IssueConstant> comparator = mapper.getComparator();

        assertEquals(0, comparator.compare(getPriority(2L), getPriority(2L)));
        assertTrue(comparator.compare(getPriority(1L), getPriority(2L)) > 0);
        assertTrue(comparator.compare(getPriority(2L), getPriority(1L)) < 0);
    }

    private IssueConstant getPriority(final Long sequence)
    {
        MockPriority p = new MockPriority("MockId", "MockName");
        p.setSequence(sequence);
        return p;
    }
}
