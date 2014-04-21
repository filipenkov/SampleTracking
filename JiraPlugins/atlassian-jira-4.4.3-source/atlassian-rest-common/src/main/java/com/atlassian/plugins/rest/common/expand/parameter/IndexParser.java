package com.atlassian.plugins.rest.common.expand.parameter;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import static java.util.Arrays.asList;
import java.util.SortedSet;
import java.util.regex.Pattern;

final class IndexParser
{
    private static final String INDEX = "-?\\d+";
    private static final String RANGE = "(?:" + INDEX + ")?:(?:" + INDEX + ")?";

    private static final Pattern INDEX_PATTERN = Pattern.compile(INDEX);
    private static final Pattern RANGE_PATTERN = Pattern.compile(RANGE);

    public final static Indexes ALL = new RangeIndexes(null, null);
    public final static Indexes EMPTY = new EmptyIndexes();

    private IndexParser()
    {
    }

    static Indexes parse(String indexes)
    {
        if (StringUtils.isBlank(indexes))
        {
            return ALL;
        }
        else if (INDEX_PATTERN.matcher(indexes).matches())
        {
            return new SimpleIndexes(Integer.parseInt(indexes));
        }
        else if (RANGE_PATTERN.matcher(indexes).matches())
        {
            final String leftAsString = StringUtils.substringBefore(indexes, ":");
            final String rightAsString = StringUtils.substringAfter(indexes, ":");
            return new RangeIndexes(
                    StringUtils.isNotBlank(leftAsString) ? Integer.parseInt(leftAsString) : null,
                    StringUtils.isNotBlank(rightAsString) ? Integer.parseInt(rightAsString) : null);
        }
        else
        {
            return EMPTY;
        }
    }

    static class SimpleIndexes implements Indexes
    {
        private final int index;

        SimpleIndexes(int index)
        {
            this.index = index;
        }

        public boolean isRange()
        {
            return false;
        }

        public int getMinIndex(int size)
        {
            return getIndex(size);
        }

        public int getMaxIndex(int size)
        {
            return getIndex(size);
        }

        private int getIndex(int size)
        {
            return isInBound(index, size) ? toPositiveIndex(index, size) : -1;
        }

        public boolean contains(int i, int size)
        {
            return isInBound(index, size) && toPositiveIndex(index, size) == i;
        }

        public SortedSet<Integer> getIndexes(int size)
        {
            return isInBound(index, size) ? Sets.newTreeSet(asList(toPositiveIndex(index, size))) : Sets.<Integer>newTreeSet();
        }
    }

    static class RangeIndexes implements Indexes
    {
        private final Integer left;
        private final Integer right;

        RangeIndexes(Integer left, Integer right)
        {
            this.left = left;
            this.right = right;
        }

        public boolean isRange()
        {
            return true;
        }

        public int getMinIndex(int size)
        {
            return actualLeft(size);
        }

        public int getMaxIndex(int size)
        {
            return actualRight(size);
        }

        public boolean contains(int index, int size)
        {
            if (!isInBound(index, size))
            {
                return false;
            }

            final int p = toPositiveIndex(index, size);
            return p >= actualLeft(size) && p <= actualRight(size);
        }

        public SortedSet<Integer> getIndexes(int size)
        {
            final SortedSet<Integer> allIndexes = Sets.newTreeSet();
            final int actualLeft = actualLeft(size);
            final int actualRight = actualRight(size);
            if (actualLeft != -1 && actualRight != -1)
            {
                for (int i = actualLeft; i <= actualRight; i++)
                {
                    allIndexes.add(i);
                }
            }
            return allIndexes;
        }

        /**
         * Will calculate the actual positive index matching {link #left}.
         * <ul>
         * <li>If the index is negative it will calculate a positive value,</li>
         * <li>If the size of the list is 0 it will return -1,</li>
         * <li>If the index is not specified or  less than 0, then 0 is returned</li>
         * <li>If the index is greater than the {@code size} of the list then -1 is returned</li>
         * <li>else the positive value of the index is returned.</li>
         * </ul>
         * @param size the size of the list to consider.
         * @return the actual 'left' index as defined.
         * @see #actualRight(int)
         */
        private int actualLeft(int size)
        {
            if (size == 0)
            {
                return -1;
            }

            if (left == null)
            {
                return 0;
            }

            final int positiveLeft = toPositiveIndex(left, size);
            if (positiveLeft < 0)
            {
                return 0;
            }
            else if (positiveLeft >= size)
            {
                return -1;
            }
            else
            {
                return positiveLeft;
            }
        }

        /**
         * Will calculate the actual positive index matching {link #right}.
         * <ul>
         * <li>If the index is negative it will calculate a positive value,</li>
         * <li>If the size of the list is 0 it will return -1,</li>
         * <li>If the index is not specified or greater than {@code size - 1}, then {@code size - 1} is returned</li>
         * <li>If the index is less than 0 then -1 is returned</li>
         * <li>else the positive value of the index is returned.</li>
         * </ul>
         * @param size the size of the list to consider.
         * @return the actual 'right' index as defined.
         * @see #actualLeft(int)
         */
        private int actualRight(int size)
        {
            if (size == 0)
            {
                return -1;
            }

            if (right == null)
            {
                return size - 1;
            }

            final int positiveRight = toPositiveIndex(right, size);
            if (positiveRight < 0)
            {
                return -1;
            }
            else if (positiveRight >= size - 1)
            {
                return size - 1;
            }
            else
            {
                return positiveRight;
            }
        }
    }

    private static class EmptyIndexes implements Indexes
    {
        public boolean isRange()
        {
            return false;
        }

        public int getMinIndex(int size)
        {
            return -1;
        }

        public int getMaxIndex(int size)
        {
            return -1;
        }

        public boolean contains(int index, int size)
        {
            return false;
        }

        public SortedSet<Integer> getIndexes(int size)
        {
            return Sets.newTreeSet();
        }

    }

    private static int toPositiveIndex(int i, int size)
    {
        return i < 0 ? i + size : i;
    }

    private static boolean isInBound(int i, int size)
    {
        final int p = toPositiveIndex(i, size);
        return p >= 0 && p < size;
    }
}
