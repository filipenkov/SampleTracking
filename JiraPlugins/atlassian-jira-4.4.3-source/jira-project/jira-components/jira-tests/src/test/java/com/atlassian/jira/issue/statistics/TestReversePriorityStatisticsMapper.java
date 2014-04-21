package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.collect.MapBuilder;

public class TestReversePriorityStatisticsMapper extends ListeningTestCase
{

    @Test
    public void testComparator() throws Exception
    {
        // Test the comparator works as expected and uses "reverse" ordering
        PriorityStatisticsMapper mapper = new ReversePriorityStatisticsMapper(null);
        final Comparator<GenericValue> comparator = mapper.getComparator();

        assertEquals(0, comparator.compare(getPriorityGV(2L), getPriorityGV(2L)));
        assertTrue(comparator.compare(getPriorityGV(1L), getPriorityGV(2L)) > 0);
        assertTrue(comparator.compare(getPriorityGV(2L), getPriorityGV(1L)) < 0);
    }

    private GenericValue getPriorityGV(final Long sequence)
    {
        return new MockGenericValue("Priority", MapBuilder.newBuilder().add("sequence", sequence).toMap());
    }
}
