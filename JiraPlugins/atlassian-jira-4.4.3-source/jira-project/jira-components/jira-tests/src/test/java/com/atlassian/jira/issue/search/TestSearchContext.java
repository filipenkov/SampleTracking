package com.atlassian.jira.issue.search;

import com.atlassian.jira.local.LegacyJiraMockTestCase;

/**
 * Tests for the SearchContext.
 */
public class TestSearchContext extends LegacyJiraMockTestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Ensure that two searchContext instances are equal.
     */
    public void testEquals()
    {
        SearchContext searchContext1 = new SearchContextImpl();
        SearchContext searchContext2 = new SearchContextImpl();

        assertTrue("sc1 equal to sc2", searchContext1.equals(searchContext2));
        assertTrue("sc2 equal to sc1", searchContext2.equals(searchContext1));
        assertTrue("sc1 equal to sc1", searchContext1.equals(searchContext1));
        assertTrue("sc2 equal to sc2", searchContext2.equals(searchContext2));
    }

    public void testHashCode()
    {
        SearchContext searchContext1 = new SearchContextImpl();
        SearchContext searchContext2 = new SearchContextImpl();

        assertEquals("sc1.hashcode equal to sc2.hashcode", searchContext1.hashCode(), searchContext2.hashCode());
    }
}
