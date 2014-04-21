package com.atlassian.jira.util;

import javax.annotation.concurrent.Immutable;

/**
 * Evaluate an input and return true or false. Useful for filtering.
 */
@Immutable
public interface Predicate<T>
{
    boolean evaluate(T input);
}
