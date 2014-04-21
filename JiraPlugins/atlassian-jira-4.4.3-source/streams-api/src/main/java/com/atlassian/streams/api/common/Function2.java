package com.atlassian.streams.api.common;

/**
 * A function of arity-2.
 * 
 * @param <T1> type of the first parameter
 * @param <T2> type of the second parameter
 * @param <R> type of the return value
 */
public interface Function2<T1, T2, R>
{
    public R apply(T1 v1, T2 v2);
}
