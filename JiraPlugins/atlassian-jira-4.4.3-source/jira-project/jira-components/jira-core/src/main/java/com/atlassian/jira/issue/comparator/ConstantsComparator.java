package com.atlassian.jira.issue.comparator;

import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

/**
 * Compares GenericValue constants like Priority, Status, Resolution.
 */
public class ConstantsComparator implements Comparator<GenericValue>
{
    public static final ConstantsComparator COMPARATOR = new ConstantsComparator();

    public int compare(GenericValue constant1, GenericValue constant2)
    {
        if (constant1 == null && constant2 == null)
            return 0;
        if (constant1 == null)
            return -1;
        if (constant2 == null)
            return 1;

        Long key1 = constant1.getLong("sequence");
        Long key2 = constant2.getLong("sequence");

        if (key1 == null && key2 == null)
            return 0;
        else if (key1 == null)
            return 1;
        else if (key2 == null)
            return -1;

        return key1.compareTo(key2);
    }
}
