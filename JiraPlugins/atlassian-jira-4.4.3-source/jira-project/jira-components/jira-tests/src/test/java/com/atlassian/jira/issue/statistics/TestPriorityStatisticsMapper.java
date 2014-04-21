/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.mockobjects.dynamic.Mock;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import java.util.Comparator;

import org.ofbiz.core.entity.GenericValue;

public class TestPriorityStatisticsMapper extends ListeningTestCase
{
    @Test
    public void testEquals()
    {
        PriorityStatisticsMapper mapper = new PriorityStatisticsMapper(null);
        assertEquals(mapper, mapper);
        assertEquals(mapper.hashCode(), mapper.hashCode());

        PriorityStatisticsMapper mapper2 = new PriorityStatisticsMapper(null);
        assertEquals(mapper, mapper2);

        Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);

        assertTrue(mapper.equals(new PriorityStatisticsMapper((ConstantsManager) mockConstantsManager.proxy())));
        assertEquals(mapper.hashCode(), new PriorityStatisticsMapper((ConstantsManager) mockConstantsManager.proxy()).hashCode());
        mockConstantsManager.verify();

        // The following line looks silly, and even gives compiler warnings, however it is testing the equals() method.
        assertFalse(mapper.equals(null));
        assertFalse(mapper.equals(new Object()));
        assertFalse(mapper.equals(new IssueKeyStatisticsMapper()));
        // Mappers of different issue "constants" are never equal to each other
        assertFalse(mapper.equals(new IssueTypeStatisticsMapper(null)));
        assertFalse(mapper.equals(new StatusStatisticsMapper(null)));
        assertFalse(mapper.equals(new ResolutionStatisticsMapper(null)));
    }

    @Test
    public void testComparator() throws Exception
    {
        // Test the comparator works as expected and uses "normal" ordering (there exists a ReversePriorityStatisticsMapper taht uses reverse ordering).
        PriorityStatisticsMapper mapper = new PriorityStatisticsMapper(null);
        final Comparator comparator = mapper.getComparator();

        assertEquals(0, comparator.compare(getPriorityGV(2L), getPriorityGV(2L)));
        assertTrue(comparator.compare(getPriorityGV(1L), getPriorityGV(2L)) < 0);
        assertTrue(comparator.compare(getPriorityGV(2L), getPriorityGV(1L)) > 0);
    }

    private GenericValue getPriorityGV(final Long sequence)
    {
        return new MockGenericValue("Priority", MapBuilder.newBuilder().add("sequence", sequence).toMap());
    }
}
