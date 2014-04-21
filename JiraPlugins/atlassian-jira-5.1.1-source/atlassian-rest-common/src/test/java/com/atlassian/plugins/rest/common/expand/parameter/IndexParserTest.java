package com.atlassian.plugins.rest.common.expand.parameter;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.SortedSet;

public class IndexParserTest
{
    @Test
    public void testParseEmptyIncludesAllIndexes()
    {
        Indexes indexes = IndexParser.parse("");
        assertEquals(0, indexes.getMinIndex(1));
        assertEquals(0, indexes.getMaxIndex(1));
        assertEquals(1, indexes.getIndexes(1).size());

        assertEquals(0, indexes.getMinIndex(5));
        assertEquals(4, indexes.getMaxIndex(5));
        assertEquals(5, indexes.getIndexes(5).size());

        assertEquals(0, indexes.getMinIndex(100));
        assertEquals(99, indexes.getMaxIndex(100));
        assertEquals(100, indexes.getIndexes(100).size());
    }

    @Test
    public void testGetMinMaxIndexesInRange()
    {
        Indexes indexes = IndexParser.parse("0:50");
        assertEquals(0, indexes.getMinIndex(1));
        assertEquals(0, indexes.getMaxIndex(1));
        assertEquals(1, indexes.getIndexes(1).size());
        assertEquals(0, indexes.getIndexes(1).iterator().next().intValue());

        assertEquals(0, indexes.getMinIndex(5));
        assertEquals(4, indexes.getMaxIndex(5));
        assertEquals(5, indexes.getIndexes(5).size());

        assertEquals(0, indexes.getMinIndex(100));
        assertEquals(50, indexes.getMaxIndex(100));
        assertEquals(51, indexes.getIndexes(100).size());
    }

    @Test
    public void testGetIndexesForRange()
    {
        Indexes indexes = IndexParser.parse("0:5");
        SortedSet<Integer> set = indexes.getIndexes(10);
        assertEquals(6, set.size());
        int val = 0;
        for (Integer index : set)
        {
            assertEquals((Integer) val++, index);
        }

        indexes = IndexParser.parse("12:");
        set = indexes.getIndexes(10);
        assertTrue(set.isEmpty());
    }

    @Test
    public void testIndexesOnEmptyList()
    {
        Indexes indexes = IndexParser.parse("0:50");
        assertEquals(-1, indexes.getMinIndex(0));
        assertEquals(-1, indexes.getMaxIndex(0));

        assertFalse(indexes.contains(-1,0));
        assertFalse(indexes.contains(0,0));
        assertFalse(indexes.contains(1,0));
        assertFalse(indexes.contains(2,0));
    }

    @Test
    public void testSinglePositiveIndex()
    {
        Indexes indexes = IndexParser.parse("0");
        assertFalse(indexes.isRange());
        assertTrue(indexes.contains(0, 2));
        assertFalse(indexes.contains(1, 2));

        assertEquals(-1, indexes.getMinIndex(0));
        assertEquals(-1, indexes.getMaxIndex(0));

        assertEquals(0, indexes.getMinIndex(1));
        assertEquals(0, indexes.getMaxIndex(1));

        indexes = IndexParser.parse("1");
        assertFalse(indexes.contains(0, 2));
        assertTrue(indexes.contains(1, 2));

        assertEquals(-1, indexes.getMinIndex(0));
        assertEquals(-1, indexes.getMaxIndex(0));

        assertEquals(-1, indexes.getMinIndex(1));
        assertEquals(-1, indexes.getMaxIndex(1));
    }

    @Test
    public void testSingleNegativeIndex()
    {
        Indexes indexes = IndexParser.parse("-1");
        assertFalse(indexes.contains(0, 2));
        assertTrue(indexes.contains(1, 2));

        indexes = IndexParser.parse("-2");
        assertTrue(indexes.contains(0, 2));
        assertFalse(indexes.contains(1, 2));
    }

    @Test
    public void testSimpleRange()
    {
        Indexes indexes = IndexParser.parse("0:3");
        assertTrue(indexes.isRange());
        assertTrue(indexes.contains(0, 3));
        assertTrue(indexes.contains(1, 3));
        assertTrue(indexes.contains(2, 3));
        assertTrue(indexes.contains(3, 4));
    }

    @Test
    public void testRangeNonZeroStart()
    {
        Indexes indexes = IndexParser.parse("1:2");
        assertTrue(indexes.isRange());
        assertFalse(indexes.contains(0, 3));
        assertTrue(indexes.contains(1, 3));
        assertTrue(indexes.contains(2, 3));
        assertFalse(indexes.contains(3, 4));
    }

    @Test
    public void testRangeSameStartEnd()
    {
        Indexes indexes = IndexParser.parse("2:2");
        assertTrue(indexes.isRange());
        assertFalse(indexes.contains(0, 3));
        assertFalse(indexes.contains(1, 3));
        assertTrue(indexes.contains(2, 3));
        assertFalse(indexes.contains(3, 4));
    }

    @Test
    public void testRangeWithNoStart()
    {
        Indexes indexes = IndexParser.parse(":2");
        assertTrue(indexes.isRange());
        assertFalse(indexes.contains(0, 0));
        assertEquals(-1,indexes.getMinIndex(0));
        assertEquals(0,indexes.getMinIndex(1));
        assertEquals(0,indexes.getMinIndex(2));
        assertEquals(0,indexes.getMinIndex(99));
        assertEquals(-1,indexes.getMaxIndex(0));
        assertEquals(0,indexes.getMaxIndex(1));
        assertEquals(1,indexes.getMaxIndex(2));
        assertEquals(2,indexes.getMaxIndex(99));
        assertTrue(indexes.contains(0, 3));
        assertTrue(indexes.contains(1, 3));
        assertTrue(indexes.contains(2, 3));
        assertFalse(indexes.contains(3, 4));
    }

    @Test
    public void testRangeWithNoEnd()
    {
        Indexes indexes = IndexParser.parse("2:");
        assertTrue(indexes.isRange());
        assertEquals(-1,indexes.getMaxIndex(0));
        assertEquals(-1,indexes.getMinIndex(0));
        assertFalse(indexes.contains(0, 3));
        assertFalse(indexes.contains(1, 3));
        assertTrue(indexes.contains(2, 3));
        assertTrue(indexes.contains(3, 4));
    }
}
