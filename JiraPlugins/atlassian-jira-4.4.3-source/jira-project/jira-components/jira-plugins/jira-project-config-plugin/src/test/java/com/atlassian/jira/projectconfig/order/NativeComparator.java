package com.atlassian.jira.projectconfig.order;

import java.util.Comparator;

/**
 * @since v4.4
 */
public class NativeComparator<T extends Comparable<T>> implements Comparator<T>
{
    private static final NativeComparator<?> instance = new NativeComparator<String>();

    @Override
    public int compare(T o1, T o2)
    {
        return o1.compareTo(o2);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> NativeComparator<T> getInstance()
    {
        //This is fine as we conly ever call compareTo and we know T implements this.
        return (NativeComparator<T>) instance;
    }
}
