package com.atlassian.streams.api.common;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Options.getValues;
import static com.google.common.collect.Iterables.*;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

public class OptionsTest
{
    @Test
    public void assertThatGetValuesWithAllNoneValuesReturnsEmptyIterable()
    {
        assertTrue(isEmpty(getValues(ImmutableList.of(none(), none()))));
    }

    @Test
    public void assertThatGetValuesWithSomeValuesReturnsIterableContainingValues()
    {
        assertThat(getValues(ImmutableList.of(some(4), some(5), some(6), none(Integer.class))), hasItems(4,5,6));
    }
}
