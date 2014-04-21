package com.atlassian.gadgets.dashboard;

import java.util.Iterator;

import com.atlassian.gadgets.dashboard.DashboardState.ColumnIndex;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;

public class ColumnIndexTest
{

    @Test
    public void assertThatColumnIndexRangeIncludesOnlyExpectedElements()
    {
        Iterable<ColumnIndex> range = ColumnIndex.range(ColumnIndex.ZERO, ColumnIndex.ONE);
        Iterator<ColumnIndex> columnIt = range.iterator();
        assertThat(columnIt.next(), is(equalTo(ColumnIndex.ZERO)));
        assertThat(columnIt.next(), is(equalTo(ColumnIndex.ONE)));
        assertFalse("column iterator should have no more elements", columnIt.hasNext());
    }
}
