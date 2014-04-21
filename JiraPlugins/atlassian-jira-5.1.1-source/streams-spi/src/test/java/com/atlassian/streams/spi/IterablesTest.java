package com.atlassian.streams.spi;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.streams.api.common.Predicates.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class IterablesTest
{
    @Mock Evictor<String> evictor;

    @Test
    public void testFilterOrEvictFiltersCorrectly()
    {
        ImmutableList<String> values = ImmutableList.of("ab", "bc", "cd");

        assertThat(Iterables.filterOrEvict(evictor, values, containsString("b")), contains("ab", "bc"));
    }

    @Test
    public void testFilterOrEvictIsLazy()
    {
        ImmutableList<String> values = ImmutableList.of("foo", "bar", "baz", "shme");
        Iterables.filterOrEvict(evictor, values, containsString("ba")).iterator().next();

        verify(evictor, times(1)).apply(anyString());
    }
}
