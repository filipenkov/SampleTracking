package com.atlassian.jira.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A replacement for UtilMisc.toList().
 * <p/>
 * Most methods here are not null safe
 */
public class EasyList
{
    public static <T> List<T> build(final T o1)
    {
        final List<T> list = new ArrayList<T>(1);
        list.add(o1);
        return list;
    }

    public static <T> List<T> build(final T o1, final T... o2)
    {
        final List<T> list = new ArrayList<T>((1 + o2.length));

        list.add(o1);
        for (final T t : o2)
        {
            list.add(t);
        }
        return list;
    }
}
