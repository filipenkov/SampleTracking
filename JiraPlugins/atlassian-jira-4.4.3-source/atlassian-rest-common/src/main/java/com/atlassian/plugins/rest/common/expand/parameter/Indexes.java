package com.atlassian.plugins.rest.common.expand.parameter;

import java.util.SortedSet;

/**
 * <p>Represents indexes as used in expand parameters. The size of the whole collection is required on most of this
 * class methods as it can handle indexes specified as: <ul> <li>[i]</li> <li>[-i]</li> <li>[i:j]</li> </ul> where
 * {@code i} and {@code j} are positive integers.</p>
 */
public interface Indexes
{
    /**
     * Tells whether {@code this} represents a contiguous range. If not a range it is a single index, and {@link
     * #getMinIndex(int)} and {@link #getMaxIndex(int)} will return the same value.
     *
     * @return {@code true} if this is a range.
     */
    boolean isRange();

    /**
     * Gets the minimum index in the range, given the total size of the collection to consider.
     *
     * @param size the size of the collection to consider.
     * @return the min index, -1 if the min index is out of bounds.
     */
    int getMinIndex(int size);

    /**
     * Gets the maximum index in the range, given the total size of the collection to consider.
     *
     * @param size the size of the collection to consider.
     * @return the max index, -1 if the max index is out of bounds.
     */
    int getMaxIndex(int size);

    /**
     * Checks whether a given index is contained within this collection of indexes
     *
     * @param index the index to look for.
     * @param size the size of the overall collection to consider.
     * @return {@code true} if it does contain the given index, {@code false} otherwise.
     */
    boolean contains(int index, int size);

    /**
     * Gets a sorted set of all indexes, negative indexes having been translated into their positive counter part.
     *
     * @param size the size of the overall collection to consider.
     * @return a set of positive indexes.
     */
    SortedSet<Integer> getIndexes(int size);
}
