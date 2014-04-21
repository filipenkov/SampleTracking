package com.atlassian.jira.projectconfig.order;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class TestNamedDefaultComparator
{
    @Test
    public void testOrder()
    {
        NamedDefaultComparator comparator = new NamedDefaultComparator(String.CASE_INSENSITIVE_ORDER);

        SimpleNamedDefault def1 = new SimpleNamedDefault("zzz", true);
        SimpleNamedDefault def2 = new SimpleNamedDefault("abc", true);
        SimpleNamedDefault def3 = new SimpleNamedDefault("ABC", true);

        SimpleNamedDefault name1 = new SimpleNamedDefault("aaa", false);
        SimpleNamedDefault name2 = new SimpleNamedDefault("AAA", false);
        SimpleNamedDefault name3 = new SimpleNamedDefault("zzz", false);

        assertTrue(comparator.compare(def1, def1) == 0);
        assertTrue(comparator.compare(def1, def2) > 0);
        assertTrue(comparator.compare(def1, def3) > 0);
        assertTrue(comparator.compare(def1, name1) < 0);
        assertTrue(comparator.compare(def1, name2) < 0);
        assertTrue(comparator.compare(def1, name3) < 0);
        assertTrue(comparator.compare(def2, def1) < 0);
        assertTrue(comparator.compare(def3, def1) < 0);
        assertTrue(comparator.compare(name1, def1) > 0);
        assertTrue(comparator.compare(name2, def1) > 0);
        assertTrue(comparator.compare(name3, def1) > 0);

        assertTrue(comparator.compare(def2, def2) == 0);
        assertTrue(comparator.compare(def2, def3) == 0);
        assertTrue(comparator.compare(def2, name1) < 0);
        assertTrue(comparator.compare(def2, name2) < 0);
        assertTrue(comparator.compare(def2, name3) < 0);
        assertTrue(comparator.compare(def3, def2) == 0);
        assertTrue(comparator.compare(name1, def2) > 0);
        assertTrue(comparator.compare(name2, def2) > 0);
        assertTrue(comparator.compare(name3, def2) > 0);

        assertTrue(comparator.compare(def3, def3) == 0);
        assertTrue(comparator.compare(def3, name1) < 0);
        assertTrue(comparator.compare(def3, name2) < 0);
        assertTrue(comparator.compare(def3, name3) < 0);
        assertTrue(comparator.compare(name1, def3) > 0);
        assertTrue(comparator.compare(name2, def3) > 0);
        assertTrue(comparator.compare(name3, def3) > 0);

        assertTrue(comparator.compare(name1, name1) == 0);
        assertTrue(comparator.compare(name1, name2) == 0);
        assertTrue(comparator.compare(name1, name3) < 0);
        assertTrue(comparator.compare(name2, name1) == 0);
        assertTrue(comparator.compare(name3, name1) > 0);

        assertTrue(comparator.compare(name2, name2) == 0);
        assertTrue(comparator.compare(name2, name3) < 0);
        assertTrue(comparator.compare(name3, name2) > 0);

        assertTrue(comparator.compare(name3, name3) == 0);
    }
}
