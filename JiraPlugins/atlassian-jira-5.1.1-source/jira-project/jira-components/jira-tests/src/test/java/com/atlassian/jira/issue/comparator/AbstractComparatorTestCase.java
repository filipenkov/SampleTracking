package com.atlassian.jira.issue.comparator;

import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Comparator;

public abstract class AbstractComparatorTestCase extends ListeningTestCase
{
    /**
     * Executes compare method on given objects a and b using given comparator
     * and asserts that the result of comparison is negative.
     *
     * @param comparator comparator to use
     * @param a          object to compare
     * @param b          object to compare
     */
    public void assertLessThan(Comparator comparator, Object a, Object b)
    {
        assertTrue(comparator.compare(a, b) < 0);
    }

    /**
     * Executes compare method on given objects a and b using given comparator
     * and asserts that the result of comparison is positive..
     *
     * @param comparator comparator to use
     * @param a          object to compare
     * @param b          object to compare
     */
    public void assertGreaterThan(Comparator comparator, Object a, Object b)
    {
        assertTrue(comparator.compare(a, b) > 0);
    }

    /**
     * Executes compare method on given objects a and b using given comparator
     * and asserts that the result of comparison is zero.
     *
     * @param comparator comparator to use
     * @param a          object to compare
     * @param b          object to compare
     */
    public void assertEqualTo(Comparator comparator, Object a, Object b)
    {
        assertEquals(0, comparator.compare(a, b));
    }
}
