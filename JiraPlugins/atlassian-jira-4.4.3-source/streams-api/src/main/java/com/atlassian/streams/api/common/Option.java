package com.atlassian.streams.api.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterators;

import static com.google.common.base.Functions.compose;
import static com.google.common.base.Functions.forPredicate;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@code Option<T>} is a wrapper for a value of type {@code T}.  It can be either a {@link Option#} or a {@link Option#some(Object)}}
 * @param <A>
 */
public abstract class Option<A> implements Iterable<A>
{
    private Option() {}

    /**
     * If this is a some value apply the some function, otherwise get the none value.
     *
     * @param <B> the result type
     * @param none the supplier of the None type
     * @param some the function to apply if we are a some
     * @return the appropriate value
     */
    public abstract <B> B fold(Supplier<B> none, Function<A, B> some);

    /**
     * @return the wrapped value
     * @throws NoSuchElementException if this is a none
     */
    public final A get()
    {
        return fold(Option.<A>throwNoSuchElementException(), com.google.common.base.Functions.<A>identity());
    }

    @SuppressWarnings("unchecked")
    private static <A> Supplier<A> throwNoSuchElementException()
    {
        return (Supplier<A>) ThrowNoSuchElementException.INSTANCE;
    }

    @SuppressWarnings("rawtypes")
    private enum ThrowNoSuchElementException implements Supplier
    {
        INSTANCE;

        public Object get()
        {
            throw new NoSuchElementException();
        }
    }

    /**
     * @return the wrapped value if this is a {@code some}, otherwise return the value supplied from the {@code Supplier}
     */
    public final A getOrElse(Supplier<A> supplier)
    {
        return fold(supplier, com.google.common.base.Functions.<A>identity());
    }

    /**
     * Returns wrapped value if this is a {@code some}, otherwise returns {@code other}.
     * @param other value to return if this is a {@code none}
     * @return wrapped value if this is a {@code some}, otherwise returns {@code other}
     */
    public final <B extends A> A getOrElse(B other)
    {
        return fold(Suppliers.<A>ofInstance(other), com.google.common.base.Functions.<A>identity());
    }

    /**
     * Apply {@code f} to the wrapped value.
     *
     * @param <B> return type of {@code f}
     * @param f function to apply to wrapped value
     * @return new wrapped value
     */
    public final <B> Option<B> map(final Function<A, B> f)
    {
        return flatMap(compose(Option.<B>option(), f));
    }

    /**
     * Apply {@code f} to the wrapped value.
     *
     * @param <B> return type of {@code f}
     * @param f function to apply to wrapped value
     * @return value returned from {@code f}
     */
    public final <B> Option<B> flatMap(final Function<A, Option<B>> f)
    {
        return fold(Option.<B>noneSupplier(), f);
    }

    /**
     * @return {@code true} if this is a {@code some}, {@code false} otherwise.
     */
    public final boolean isDefined()
    {
        return fold(Suppliers.ofInstance(false), forPredicate(Predicates.<A>alwaysTrue()));
    }

    public final Iterator<A> iterator()
    {
        return fold(Suppliers.<Iterator<A>>ofInstance(Iterators.<A>emptyIterator()), Option.<A>singletonIterator());
    }

    private static <A> Function<A, Iterator<A>> singletonIterator()
    {
        return new Function<A, Iterator<A>>()
        {
            public Iterator<A> apply(A a)
            {
                return Iterators.singletonIterator(a);
            }
        };
    }

    @Override
    public final int hashCode()
    {
        return fold(noneHashCode(), someHashCode());
    }

    @SuppressWarnings("unchecked")
    private Function<A, Integer> someHashCode()
    {
        return (Function<A, Integer>) SomeHashCode.INSTANCE;
    }

    @SuppressWarnings("rawtypes")
    private enum SomeHashCode implements Function
    {
        INSTANCE;

        public Integer apply(Object a)
        {
            return a.hashCode();
        }
    }

    private Supplier<Integer> noneHashCode()
    {
        return NoneHashCode.INSTANCE;
    }

    private enum NoneHashCode implements Supplier<Integer>
    {
        INSTANCE;

        public Integer get()
        {
            return 31;
        }
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        @SuppressWarnings("unchecked")
        final Option<Object> other = (Option<Object>) obj;
        return other.fold(isDefined() ? SupplyFalse.INSTANCE : SupplyTrue.INSTANCE, valuesEqual());
    }

    private Function<Object, Boolean> valuesEqual()
    {
        return new Function<Object, Boolean>()
        {
            public Boolean apply(Object obj)
            {
                return get().equals(obj);
            }
        };
    }

    private enum SupplyTrue implements Supplier<Boolean>
    {
        INSTANCE;

        public Boolean get()
        {
            return true;
        }
    }

    private enum SupplyFalse implements Supplier<Boolean>
    {
        INSTANCE;

        public Boolean get()
        {
            return false;
        }
    }

    @Override
    public final String toString()
    {
        return fold(noneString(), someString());
    }

    @SuppressWarnings("unchecked")
    private Function<A, String> someString()
    {
        return (Function<A, String>) SomeString.INSTANCE;
    }

    @SuppressWarnings("rawtypes")
    private enum SomeString implements Function
    {
        INSTANCE;

        public String apply(Object obj)
        {
            return String.format("some(%s)", obj);
        }
    }

    private Supplier<String> noneString()
    {
        return NoneString.INSTANCE;
    }

    private enum NoneString implements Supplier<String>
    {
        INSTANCE;

        public String get()
        {
            return "none()";
        }
    }

    public static <A> Option<A> some(final A value)
    {
        return new Some<A>(value);
    }

    private static final class Some<A> extends Option<A>
    {
        private final A value;

        private Some(final A value)
        {
            this.value = checkNotNull(value, "value");
        }

        @Override
        public <B> B fold(final Supplier<B> none, final Function<A, B> f)
        {
            return f.apply(value);
        }
    }

    @SuppressWarnings("unchecked")
    public static <A> Option<A> none()
    {
        return (Option<A>) NONE;
    }

    @SuppressWarnings("unchecked")
    public static <A> Option<A> none(final Class<A> type)
    {
        return (Option<A>) NONE;
    }

    private static final class NoneSupplier<A> implements Supplier<Option<A>>
    {
        public Option<A> get()
        {
            return none();
        }
    };

    public static <A> Supplier<Option<A>> noneSupplier()
    {
        return new NoneSupplier<A>();
    }

    private static final Option<Object> NONE = new Option<Object>()
    {
        @Override
        public <B> B fold(final Supplier<B> none, final Function<Object, B> some)
        {
            return none.get();
        }
    };

    public static <A> Option<A> option(final A a)
    {
        if (a == null)
        {
            return none();
        }
        else
        {
            return some(a);
        }
    }

    public static <A> Function<A, Option<A>> option()
    {
        return new ToOption<A>();
    }

    private static class ToOption<A> implements Function<A, Option<A>>
    {
        public Option<A> apply(final A from)
        {
            return option(from);
        }
    }
}
