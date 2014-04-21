package com.atlassian.streams.api.common;

/**
 * A marker interface type for iterables that must contain at least one element. There are no additional operations
 * defined on a {@code NonEmptyIterable}; however, some utility functions for working with {@code NonEmptyIterable}s are defined
 * in the companion {@link NonEmptyIterables} class.
 *
 * See also {@link ImmutableNonEmptyList}
 *
 * @param <T> the type of values stored in the iterable
 */
public interface NonEmptyIterable<T> extends Iterable<T>
{
}
