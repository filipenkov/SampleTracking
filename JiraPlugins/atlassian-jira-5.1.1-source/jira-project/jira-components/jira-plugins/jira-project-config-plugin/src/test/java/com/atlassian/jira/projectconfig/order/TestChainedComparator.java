package com.atlassian.jira.projectconfig.order;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class TestChainedComparator
{
    @Test
    public void testComparator() throws Exception
    {
        ChainedComparator<String> comparator = new ChainedComparator<String>(of(String.CASE_INSENSITIVE_ORDER));
        assertEquals(0, comparator.compare("one", "one"));
        assertEquals(0, comparator.compare("abc", "ABC"));
        assertTrue(comparator.compare("abc", "ZZZ") < 0);
        assertTrue(comparator.compare("ABC", "zzz") < 0);
        assertTrue(comparator.compare("ZZZ", "ABC") > 0);
        assertTrue(comparator.compare("ZZZ", "abc") > 0);

        ArrayList<String> strings = newArrayList("zzz", "ZZZ", "ABC", "abc");
        Collections.sort(strings, comparator);
        assertEquals(newArrayList("ABC", "abc", "zzz", "ZZZ"), strings);


        comparator = ChainedComparator.of(String.CASE_INSENSITIVE_ORDER, new StringComparator());
        assertEquals(0, comparator.compare("one", "one"));
        assertTrue(comparator.compare("abc", "ABC") > 0);
        assertTrue(comparator.compare("abc", "ZZZ") < 0);
        assertTrue(comparator.compare("ABC", "zzz") < 0);
        assertTrue(comparator.compare("ZZZ", "ABC") > 0);
        assertTrue(comparator.compare("ZZZ", "abc") > 0);

        strings = newArrayList("zzz", "ZZZ", "ABC", "abc");
        Collections.sort(strings, comparator);
        assertEquals(newArrayList("ABC", "abc", "ZZZ", "zzz"), strings);
    }

    private static class StringComparator implements Comparator<String>
    {
        @Override
        public int compare(String o1, String o2)
        {
            return o1.compareTo(o2);
        }
    }
}
