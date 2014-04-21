package com.atlassian.jira.issue.search;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;
import com.atlassian.jira.local.ListeningTestCase;

public class TestSearchSort extends ListeningTestCase
{
    @Test
    public void testConstructorBad() throws Exception
    {
        try
        {
            new SearchSort("", (String)null);
        }
        catch (IllegalArgumentException e) { }

        try
        {
            new SearchSort(null, SortOrder.DESC);
        }
        catch (IllegalArgumentException e) { }

        try
        {
            new SearchSort("something", (SortOrder) null);
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testStringConstructor()
    {
        SearchSort sort = new SearchSort("foo", "bar");
        assertEquals("bar", sort.getField());
        assertEquals(SortOrder.ASC.name(), sort.getOrder());
    }

    @Test
    public void testSearchSortParseString() throws Exception
    {
        assertSame(null, SortOrder.parseString(null));
        assertSame(null, SortOrder.parseString(""));
        
        assertSame(SortOrder.DESC, SortOrder.parseString("deSC"));
        assertSame(SortOrder.ASC, SortOrder.parseString("ASC"));
        assertSame(SortOrder.ASC, SortOrder.parseString("BLAHH"));
    }
}
