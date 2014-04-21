package com.atlassian.jira.util;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Legacy utility class for instantiating lists. We now prefer to use <code>com.google.common.collect.Lists</code> from
 * google-collections / guava.
 *
 * @deprecated Use {@link com.google.common.collect.Lists} instead. Since v5.0.
 */
@Deprecated
public class EasyList
{
    /**
     * @deprecated Use {@link com.google.common.collect.Lists#newArrayList(Object[])} instead. Since v5.0.
     */
    @Deprecated
    public static <T> List<T> build(final T elem)
    {
        return Lists.newArrayList(elem);
    }

    /**
     * @deprecated Use {@link com.google.common.collect.Lists#newArrayList(Object[])} instead. Since v5.0.
     */
    @Deprecated
    public static <T> List<T> build(final T... elems)
    {
        return Lists.newArrayList(elems);
    }
}
