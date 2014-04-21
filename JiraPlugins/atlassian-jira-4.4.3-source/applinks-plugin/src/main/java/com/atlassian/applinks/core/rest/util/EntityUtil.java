package com.atlassian.applinks.core.rest.util;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @since 3.0
 */
public class EntityUtil
{
    private EntityUtil()
    {

    }

    public static Set<String> getClassNames(final Iterable types)
    {
        final Set<String> type = new LinkedHashSet<String>();
        addClassNames(type, types);
        return type;
    }

    private static void addClassNames(final Set<String> target, final Iterable types)
    {
        Iterables.addAll(target, Iterables.transform(types, new Function<Class, String>()
        {
            public String apply(final Class from)
            {
                return from.getName();
            }
        }));
    }
}
