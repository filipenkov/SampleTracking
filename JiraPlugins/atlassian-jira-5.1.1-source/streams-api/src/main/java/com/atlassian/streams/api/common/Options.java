package com.atlassian.streams.api.common;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

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
    public static <A> Iterable<A> catOptions(Iterable<Option<A>> as)
    {
        return concat(as);
    }

    /**
     * Returns the first element in the input iterable that is not {@link Option#none()}.
     * @param as An iterable of optional values
     * @param <A> the type of values
     * @return The first element for which {@link #isDefined()} was true; {@link Option#none()} if
     *   all of the elements were {@link Option#none()}, or if the iterable was empty
     */
    public static <A> Option<A> find(Iterable<Option<A>> as)
    {
        for (Option<A> a: as)
        {
            if (a.isDefined())
            {
                return a;
            }
        }
        return Option.<A>none();
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

    public static <A, B> Function<A, Option<B>> asNone()
    {
        return new AsNone<A, B>();
    }

    private static final class AsNone<A, B> implements Function<A, Option<B>>
    {
        public Option<B> apply(A a)
        {
            return Option.<B>none();
        }
    }

    public static <A> Function<A, Option<A>> asSome()
    {
        return new AsSome<A>();
    }

    private static final class AsSome<A>  implements Function<A, Option<A>>
    {
        public Option<A> apply(A a)
        {
            return Option.<A>some(a);
        }
    }
    
    public static <T> Supplier<Option<T>> noneSupplier()
    {
        return new NoneSupplier<T>();
    }
    
    private static class NoneSupplier<T> implements Supplier<Option<T>>
    {
        @Override
        public Option<T> get()
        {
            return Option.<T>none();
        }
    }
}
