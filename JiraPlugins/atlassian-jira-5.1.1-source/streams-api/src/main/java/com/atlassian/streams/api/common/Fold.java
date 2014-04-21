package com.atlassian.streams.api.common;

/**
 * Provides methods for doing folds on collections of values.
 */
public final class Fold
{
    /**
     * Fold left on {@code xs}, using {@code init} as the initial value and {@code f} as the function to apply.
     * 
     * @param <A> type of the values being folded over
     * @param <B> type of value to build and return
     * @param xs values being folded over
     * @param init initial value
     * @param f {@code Function} to apply
     * @return value of applying {@code f} to {@code xs}
     */
    public static <A, B> B foldl(Iterable<A> xs, B init, Function2<A, B, B> f)
    {
        B intermediate = init;
        for (A x : xs)
        {
            intermediate = f.apply(x, intermediate);
        }
        return intermediate;
    }
}
