package com.atlassian.jira.matchers;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Some moar iterable matchers.
 *
 * @since 5.1
 */
public final class IterableMatchers
{

    private IterableMatchers()
    {
        throw new AssertionError("Don't instantiate me");
    }


    public static <E> Matcher<Iterable<E>> emptyIterable(Class<E> elementType)
    {
        return Matchers.emptyIterable();
    }

    public static <E> Matcher<Iterable<E>> iterableWithSize(int expected, Class<E> elementType)
    {
        return Matchers.iterableWithSize(expected);
    }

    public static <E> Matcher<Iterable<E>> hasItems(Class<E> itemType, E... items)
    {
        return Matchers.hasItems(items);
    }
}
