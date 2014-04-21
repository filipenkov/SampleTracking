package com.atlassian.jira.mock.matcher;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.Comparator;

/**
 * A adaptor class that converts a {@link Comparable} into an {@link ArgumentMatcher}.
 *
 * @since v4.0
 */
final class ComparatorAdaptorMatcher<T> implements ArgumentMatcher<T>
{
    private final Comparator<? super T> comparator;

    ComparatorAdaptorMatcher(final Comparator<? super T> comparator)
    {
        this.comparator = notNull("comparator", comparator);
    }

    public boolean match(final T expected, final T acutal)
    {
        if (expected == acutal)
        {
            return true;
        }
        else if (expected == null || acutal == null)
        {
            return false;
        }
        else
        {
            return comparator.compare(expected, acutal) == 0;
        }
    }

    public String toString(final T object)
    {
        return String.valueOf(object);
    }

    @Override
    public String toString()
    {
        return "Comparator Argument Matcher [" + comparator + "]";
    }
}
