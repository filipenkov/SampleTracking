package com.atlassian.jira.projectconfig.order;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @since v4.4
 */
class ChainedComparator<T> implements Comparator<T>
{
    private final Iterable<? extends Comparator<? super T>> comparators;

    ChainedComparator(Iterable<? extends Comparator<? super T>> comparators)
    {
        this.comparators = comparators;
    }

    @Override
    public int compare(T o1, T o2)
    {
        for (final Comparator<? super T> comparator : comparators)
        {
            int result = comparator.compare(o1, o2);
            if (result != 0)
            {
                return result;
            }
        }
        return 0;
    }

    static <T> ChainedComparator<T> of(Comparator<? super T>... comparators)
    {
        return new ChainedComparator<T>(Arrays.asList(comparators));
    }
}
