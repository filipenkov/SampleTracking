package com.atlassian.upm;

import com.atlassian.upm.Interval.Ceiling;
import com.atlassian.upm.Interval.Floor;
import com.atlassian.upm.test.UpmMatchers;

import org.apache.commons.lang.ArrayUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.atlassian.upm.Interval.Bound.Type.EXCLUSIVE;
import static com.atlassian.upm.Interval.Bound.Type.INCLUSIVE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class IntervalTest
{
    private static final Floor<Integer>
        INCLUSIVE_FLOOR = new Floor<Integer>(1, INCLUSIVE),
        EXCLUSIVE_FLOOR = new Floor<Integer>(1, EXCLUSIVE);

    private static final Ceiling<Integer>
        INCLUSIVE_CEILING = new Ceiling<Integer>(3, INCLUSIVE),
        EXCLUSIVE_CEILING = new Ceiling<Integer>(3, EXCLUSIVE);

    private static final Interval<Integer>
        INCLUSIVE_TO_INCLUSIVE = new Interval<Integer>(INCLUSIVE_FLOOR, INCLUSIVE_CEILING),
        INCLUSIVE_TO_EXCLUSIVE = new Interval<Integer>(INCLUSIVE_FLOOR, EXCLUSIVE_CEILING),
        EXCLUSIVE_TO_INCLUSIVE = new Interval<Integer>(EXCLUSIVE_FLOOR, INCLUSIVE_CEILING),
        EXCLUSIVE_TO_EXCLUSIVE = new Interval<Integer>(EXCLUSIVE_FLOOR, EXCLUSIVE_CEILING),
        INCLUSIVE_TO_OPEN = new Interval<Integer>(INCLUSIVE_FLOOR, null),
        OPEN_TO_INCLUSIVE = new Interval<Integer>(null, INCLUSIVE_CEILING),
        OPEN_TO_OPEN = new Interval<Integer>(null, null);

    public static Matcher<? super Interval<Integer>> intervalContaining(int... items)
    {
        return UpmMatchers.intervalContaining(ArrayUtils.toObject(items));
    }

    @Test
    public void assertThatI2IWorks()
    {
        assertThat(INCLUSIVE_TO_INCLUSIVE, allOf(
            Matchers.<Interval<Integer>>hasToString("[1,3]"),
            intervalContaining(1, 2, 3),
            not(intervalContaining(0, 4))
        ));
    }

    @Test
    public void assertThatI2EWorks()
    {
        assertThat(INCLUSIVE_TO_EXCLUSIVE, allOf(
            Matchers.<Interval<Integer>>hasToString("[1,3)"),
            intervalContaining(1, 2),
            not(intervalContaining(0, 3))
        ));
    }

    @Test
    public void assertThatE2IWorks()
    {
        assertThat(EXCLUSIVE_TO_INCLUSIVE, allOf(
            Matchers.<Interval<Integer>>hasToString("(1,3]"),
            intervalContaining(2, 3),
            not(intervalContaining(1, 4))
        ));
    }

    @Test
    public void assertThatE2EWorks()
    {
        assertThat(EXCLUSIVE_TO_EXCLUSIVE, allOf(
            Matchers.<Interval<Integer>>hasToString("(1,3)"),
            intervalContaining(2),
            not(intervalContaining(1, 3))
        ));
    }

    @Test
    public void assertThatI2OWorks()
    {
        assertThat(INCLUSIVE_TO_OPEN, allOf(
            Matchers.<Interval<Integer>>hasToString("[1,_)"),
            intervalContaining(Integer.MAX_VALUE),
            not(intervalContaining(0))
        ));
    }

    @Test
    public void assertThatO2IWorks()
    {
        assertThat(OPEN_TO_INCLUSIVE, allOf(
            Matchers.<Interval<Integer>>hasToString("(_,3]"),
            intervalContaining(Integer.MIN_VALUE),
            not(intervalContaining(4))
        ));
    }

    @Test
    public void assertThatO2OWorks()
    {
        assertThat(OPEN_TO_OPEN, allOf(
            Matchers.<Interval<Integer>>hasToString("(_,_)"),
            intervalContaining(Integer.MAX_VALUE),
            intervalContaining(Integer.MIN_VALUE)
        ));
    }
}
