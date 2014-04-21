package com.atlassian.jira.util.collect;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.util.EasyList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.atlassian.jira.local.ListeningTestCase;

public class TestListOrderComparator extends ListeningTestCase
{
    @Test
    public void testSimpleGet() throws Exception
    {
        final List<String> list = EasyList.build("one", "four", "eight");
        final Comparator<String> comparator = new ListOrderComparator<String>(list);
        final List<String> result = EasyList.build("eight", "one", "four");

        Collections.sort(result, comparator);
        assertEquals(3, result.size());
        assertEquals("one", result.get(0));
        assertEquals("four", result.get(1));
        assertEquals("eight", result.get(2));
    }

    @Test
    public void testGetThrowsIllegalArg() throws Exception
    {
        final List<String> list = EasyList.build("one", "four", "eight");
        final Comparator<String> comparator = new ListOrderComparator<String>(list);
        final List<String> result = EasyList.build("eight", "one", "six");

        try
        {
            Collections.sort(result, comparator);
            fail("Should have thrown IllegalArg");
        }
        catch (final IllegalArgumentException ignore)
        {}
    }
}
