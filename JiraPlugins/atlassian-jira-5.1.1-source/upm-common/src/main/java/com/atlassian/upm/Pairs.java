package com.atlassian.upm;

import javax.annotation.Nullable;

public interface Pairs
{
    public static interface Pair<T1, T2>
    {
        @Nullable T1 getFirst();
        @Nullable T2 getSecond();
    }

    public static final class ImmutablePair<T1, T2> implements Pair<T1, T2>
    {
        private final @Nullable T1 first;
        private final @Nullable T2 second;

        private ImmutablePair(@Nullable T1 first, @Nullable T2 second)
        {
            this.first = first;
            this.second = second;
        }

        public static <T1, T2> ImmutablePair<T1, T2> pair(@Nullable T1 first, @Nullable T2 second)
        {
            return new ImmutablePair<T1, T2>(first, second);
        }

        @Nullable
        public T1 getFirst()
        {
            return first;
        }

        @Nullable
        public T2 getSecond()
        {
            return second;
        }

        public boolean equals(@Nullable Object other)
        {
            if (this == other)
            {
                return true;
            }

            if (other == null || getClass() != other.getClass())
            {
                return false;
            }

            Pair<?, ?> otherPair = (Pair<?, ?>) other;
            return (first == null ? otherPair.getFirst() == null : first.equals(otherPair.getFirst())) &&
                (second == null ? otherPair.getSecond() == null : second.equals(otherPair.getSecond()));
        }

        public int hashCode()
        {
            return (first == null ? 0 : first.hashCode()) +
                (second == null ? 0 : 31 * second.hashCode());
        }
    }

    public static final class MutablePair<T1, T2> implements Pair<T1, T2>
    {
        private @Nullable T1 first;
        private @Nullable T2 second;

        private MutablePair(@Nullable T1 first, @Nullable T2 second)
        {
            this.first = first;
            this.second = second;
        }

        public static <T1, T2> MutablePair<T1, T2> pair(@Nullable T1 first, @Nullable T2 second)
        {
            return new MutablePair<T1, T2>(first, second);
        }

        @Nullable
        public T1 getFirst()
        {
            return first;
        }

        public void setFirst(@Nullable T1 first)
        {
            this.first = first;
        }

        @Nullable
        public T2 getSecond()
        {
            return second;
        }

        public void setSecond(@Nullable T2 second)
        {
            this.second = second;
        }
    }

}
