package com.atlassian.upm.osgi;

import com.atlassian.upm.Interval.Bound.Type;
import com.atlassian.upm.Interval.Ceiling;
import com.atlassian.upm.Interval.Floor;
import com.atlassian.upm.test.UpmMatchers;

import org.hamcrest.Matcher;
import org.junit.Test;

import static com.atlassian.upm.Interval.Bound.Type.EXCLUSIVE;
import static com.atlassian.upm.Interval.Bound.Type.INCLUSIVE;
import static com.atlassian.upm.osgi.impl.Versions.fromString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class VersionRangeTest
{
    private static final String
        INCLUSIVE_TO_INCLUSIVE = "[1.0,2.0]",
        INCLUSIVE_TO_EXCLUSIVE = "[1.0,2.0)",
        EXCLUSIVE_TO_INCLUSIVE = "(1.0,2.0]",
        EXCLUSIVE_TO_EXCLUSIVE = "(1.0,2.0)",
        INCLUSIVE_TO_INFINITE = "1.0",
        WHITESPACE = "[  1.0  ,  2.0  ]";

    private static final Matcher<? super Type>
        inclusive = equalTo(INCLUSIVE),
        exclusive = equalTo(EXCLUSIVE);

    private static final Matcher<? super Version>
        v1 = equalTo(fromString("1")),
        v2 = equalTo(fromString("2"));

    private static final Matcher<? super Floor<Version>>
        inclusiveFloor = UpmMatchers.<Version, Floor<Version>> hasTypeAndValue("floor", inclusive, v1),
        exclusiveFloor = UpmMatchers.<Version, Floor<Version>> hasTypeAndValue("floor", exclusive, v1);

    private static final Matcher<? super Ceiling<Version>>
        inclusiveCeiling = UpmMatchers.<Version, Ceiling<Version>> hasTypeAndValue("ceiling", inclusive, v2),
        exclusiveCeiling = UpmMatchers.<Version, Ceiling<Version>> hasTypeAndValue("ceiling", exclusive, v2);

    @Test
    public void assertThatI2IIsParsedCorrectly()
    {
        assertThat(VersionRange.fromString(INCLUSIVE_TO_INCLUSIVE),
            UpmMatchers.<Version, VersionRange> hasBounds(inclusiveFloor, inclusiveCeiling));
    }

    @Test
    public void assertThatI2EIsParsedCorrectly()
    {
        assertThat(VersionRange.fromString(INCLUSIVE_TO_EXCLUSIVE),
            UpmMatchers.<Version, VersionRange> hasBounds(inclusiveFloor, exclusiveCeiling));
    }

    @Test
    public void assertThatE2IIsParsedCorrectly()
    {
        assertThat(VersionRange.fromString(EXCLUSIVE_TO_INCLUSIVE),
            UpmMatchers.<Version, VersionRange> hasBounds(exclusiveFloor, inclusiveCeiling));
    }

    @Test
    public void assertThatE2EIsParsedCorrectly()
    {
        assertThat(VersionRange.fromString(EXCLUSIVE_TO_EXCLUSIVE),
            UpmMatchers.<Version, VersionRange> hasBounds(exclusiveFloor, exclusiveCeiling));
    }

    @Test
    public void assertThatI2InfiniteIsParsedCorrectly()
    {
        assertThat(VersionRange.fromString(INCLUSIVE_TO_INFINITE),
            UpmMatchers.<Version, VersionRange> hasBounds(inclusiveFloor, nullValue()));
    }

    @Test
    public void assertThatWhitespaceIsParsedCorrectly()
    {
        assertThat(VersionRange.fromString(WHITESPACE),
            UpmMatchers.<Version, VersionRange> hasBounds(inclusiveFloor, inclusiveCeiling));
    }
}
