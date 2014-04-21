package com.atlassian.streams.api.common;

import com.google.common.base.Predicate;

import static com.google.common.collect.Iterables.concat;

/**
 * Useful methods for working with {@code Option}s
 */
public final class Options
{
    private Options() {}

    /**
     * Projects out any {@link Option#none()} values from its input iterable,
     * returning the resulting iterable
     * @param as An iterable of optional values
     * @param <A> the type of values
     * @return An iterable containing the elements of {@code as} that were not {@link Option#none()}
     */
    public static <A> Iterable<A> getValues(Iterable<Option<A>> as)
    {
        return concat(as);
    }

    public static <A> Predicate<Option<A>> isDefined()
    {
        return new IsDefined<A>();
    }

    private static final class IsDefined<A> implements Predicate<Option<A>>
    {
        public boolean apply(Option<A> a)
        {
            return a.isDefined();
        }
    }
}
