package com.atlassian.jira.projectconfig.order;

import com.atlassian.jira.projectconfig.beans.SimpleIssueType;
import com.atlassian.jira.projectconfig.beans.SimpleIssueTypeImpl;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class TestSimpleIssueTypeComparator
{
    @Test
    public void testOrder() throws Exception
    {
        SimpleIssueTypeComparator comparator = new SimpleIssueTypeComparator(String.CASE_INSENSITIVE_ORDER);

        SimpleIssueType defaultIssueType = createIssueType("zzz", false, true);
        SimpleIssueType defaultIssueTypeOther = createIssueType("zzzz", false, true);

        SimpleIssueType abc = createIssueType("abc", false, false);
        SimpleIssueType def = createIssueType("def", false, false);

        SimpleIssueType subtask = createIssueType("aaaaaaa", true, false);
        SimpleIssueType subtaskOther = createIssueType("AAAAAAA", true, false);
        SimpleIssueType subtaskLast = createIssueType("zzzzzzzzzzzzzzzzzz", true, false);

        assertTrue(comparator.compare(defaultIssueType, abc) < 0);
        assertTrue(comparator.compare(defaultIssueType, def) < 0);
        assertTrue(comparator.compare(defaultIssueType, subtask) < 0);
        assertTrue(comparator.compare(defaultIssueType, subtaskOther) < 0);

        assertTrue(comparator.compare(abc, defaultIssueTypeOther) > 0);
        assertTrue(comparator.compare(def, defaultIssueTypeOther) > 0);
        assertTrue(comparator.compare(subtask, defaultIssueTypeOther) > 0);
        assertTrue(comparator.compare(subtaskOther, defaultIssueTypeOther) > 0);

        assertTrue(comparator.compare(defaultIssueType, defaultIssueType) == 0);
        assertTrue(comparator.compare(defaultIssueType, defaultIssueTypeOther) < 0);
        assertTrue(comparator.compare(defaultIssueTypeOther, defaultIssueType) > 0);

        assertTrue(comparator.compare(abc, abc) == 0);
        assertTrue(comparator.compare(def, subtask) < 0);
        assertTrue(comparator.compare(def, subtaskOther) < 0);
        assertTrue(comparator.compare(def, subtaskOther) < 0);

        assertTrue(comparator.compare(subtask, subtask) == 0);
        assertTrue(comparator.compare(subtask, subtaskOther) == 0);
        assertTrue(comparator.compare(subtask, subtaskLast) < 0);
    }

    private static SimpleIssueType createIssueType(final String name, final boolean subTask, final boolean defaultIssueType)
    {
        return new SimpleIssueTypeImpl(name, name, name, null, subTask, defaultIssueType);
    }
}
