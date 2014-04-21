package com.atlassian.streams.api.common;

import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import static com.google.common.base.Functions.forPredicate;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * A disjoint union type typically used as an alternative to {@code Option} where by convention the
 * left value contains an error and the right is akin to "some".
 * @param <L> the type of value stored left
 * @param <R> the type of value stored right
 */
public abstract class Either<L, R>
{
    public static <L, R> Either<L, R> left(final L left)
    {
        return new Left<L, R>(left);
    }

    public static <L, R> Either<L, R> right(final R right)
    {
        return new Right<L, R>(right);
    }

    private Either() {}

    public abstract <Z> Z fold(Function<L, Z> l, Function<R, Z> r);

    public final LeftProjection<L, R> left()
    {
        return new LeftProjection<L, R>(this);
    }

    public static final class LeftProjection<L, R> implements Iterable<L>
    {
        private final Either<L, R> e;

        LeftProjection(Either<L, R> e)
        {
            this.e = e;
        }

        public L get()
        {
            return e.fold(Functions.<L>identity(), Either.<R, L>throwNoSuchElementException("Either.left().get() on Right"));
        }

        public Option<L> toOption()
        {
           return e.fold(Options.<L>asSome(), Options.<R, L>asNone());
        }

        @Override
        public Iterator<L> iterator()
        {
            return toOption().iterator();
        }
    }

    public final RightProjection<L, R> right()
    {
        return new RightProjection<L, R>(this);
    }

    public static final class RightProjection<L, R> implements Iterable<R>
    {
        private final Either<L, R> e;

        public RightProjection(Either<L, R> e)
        {
            this.e = e;
        }

        public R get()
        {
            return e.fold(Either.<L, R>throwNoSuchElementException("Either.right().get() on Left"), Functions.<R>identity());
        }

        public Option<R> toOption()
        {
            return e.fold(Options.<L, R>asNone(), Options.<R>asSome());
        }

        @Override
        public Iterator<R> iterator()
        {
            return toOption().iterator();
        }
    }

    private static <A, B> Function<A, B> throwNoSuchElementException(final String message)
    {
        return new Function<A, B>()
        {
            public B apply(A a)
            {
                throw new UnsupportedOperationException(message);
            }
        };
    }

    public final boolean isLeft()
    {
        return fold(forPredicate(Predicates.<L>alwaysTrue()), forPredicate(Predicates.<R>alwaysFalse()));
    }

    public final boolean isRight()
    {
        return fold(forPredicate(Predicates.<L>alwaysFalse()), forPredicate(Predicates.<R>alwaysTrue()));
    }

    /**
     * Left side
     *
     * @param <L> the type Left encapsulates
     * @param <R> the type Left is not
     */
    static final class Left<L, R> extends Either<L, R>
    {
        private final L value;

        Left(final L value)
        {
            this.value = checkNotNull(value);
        }

        @Override
        public <Z> Z fold(Function<L, Z> l, Function<R, Z> r)
        {
            return l.apply(value);
        }

        @Override
        public String toString()
        {
            return String.format("left(%s)", value);
        }
    }

    static final class Right<L, R> extends Either<L, R>
    {
        private final R value;

        Right(final R value)
        {
            this.value = checkNotNull(value);
        }

        @Override
        public <Z> Z fold(Function<L, Z> l, Function<R, Z> r)
        {
            return r.apply(value);
        }

        @Override
        public String toString()
        {
            return String.format("right(%s)", value);
        }
    }

    public static <L, R> Iterable<L> getLefts(Iterable<Either<L, R>> all)
    {
        return transform(filter(all, new Predicate<Either<L, R>>()
        {
            public boolean apply(Either<L, R> either)
            {
                return either.isLeft();
            }
        }), new Function<Either<L, R>, L>()
        {
            public L apply(Either<L, R> either)
            {
                return either.left().get();
            }
        });
    }

    public static <L, R> Iterable<R> getRights(Iterable<Either<L, R>> all)
    {
        return transform(filter(all, new Predicate<Either<L, R>>()
        {
            public boolean apply(Either<L, R> either)
            {
                return either.isRight();
            }
        }), new Function<Either<L, R>, R>()
        {
            public R apply(Either<L, R> either)
            {
                return either.right().get();
            }
        });
    }
}