package com.atlassian.streams.api.common;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.atlassian.streams.api.common.Option.some;
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

    public L left()
    {
        throw new UnsupportedOperationException("Cannot access left on a right");
    }

    public R right()
    {
        throw new UnsupportedOperationException("Cannot access right on a left");
    }

    public Option<L> asLeft()
    {
        return Option.none();
    }

    public Option<R> asRight()
    {
        return Option.none();
    }

    public boolean isLeft()
    {
        return false;
    }

    public boolean isRight()
    {
        return false;
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
        public boolean isLeft()
        {
            return true;
        }

        @Override
        public L left()
        {
            return value;
        }

        @Override
        public Option<L> asLeft()
        {
            return some(value);
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
        public boolean isRight()
        {
            return true;
        }

        @Override
        public R right()
        {
            return value;
        }

        @Override
        public Option<R> asRight()
        {
            return some(value);
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
                return either.left();
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
                return either.right();
            }
        });
    }
}