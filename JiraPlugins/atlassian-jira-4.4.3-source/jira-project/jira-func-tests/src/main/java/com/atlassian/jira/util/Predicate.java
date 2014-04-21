package com.atlassian.jira.util;

/**
 * Evaluate an input and return true or false. Useful for filtering.
 */
public interface Predicate<T>
{
    boolean evaluate(T input);
}
