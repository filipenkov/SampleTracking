package com.atlassian.gadgets.directory.internal;

import java.net.URI;

import com.atlassian.gadgets.GadgetSpecProvider;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.directory.internal.GadgetSpecProviderHelper.containsSpecUri;
import static com.atlassian.gadgets.directory.internal.GadgetSpecProviderHelper.toEntries;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetSpecProviderHelperTest
{
    static final URI MONKEY_URI = URI.create("http://example.com/monkey.xml");
    
    @Mock GadgetSpecProvider provider;
    
    @Test
    public void assertThatContainsSpecUriPredicateEvaluatesToTrueIfProviderContainsSpecUri()
    {
        when(provider.contains(MONKEY_URI)).thenReturn(true);
        assertTrue(containsSpecUri(MONKEY_URI).apply(provider));
    }
    
    @Test
    public void assertThatContainsSpecUriPredicateEvaluatesToFalseIfProviderDoesNotContainSpecUri()
    {
        when(provider.contains(MONKEY_URI)).thenReturn(false);  // this isn't really necessary, but it makes it more explicit
        assertFalse(containsSpecUri(MONKEY_URI).apply(provider));
    }
    
    @Test
    public void assertThatToEntriesFunctionReturnsEntriesInTheProvider()
    {
        when(provider.entries()).thenReturn(ImmutableList.of(MONKEY_URI));
        assertThat(toEntries().apply(provider), contains(MONKEY_URI));
    }
}
