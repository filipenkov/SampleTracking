package com.atlassian.jira.user;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestCachingExternalEntityStore extends ListeningTestCase
{
    @Test
    public void shouldRetrieveFromCacheIfPresentAndOtherwiseDelegateToTheUnderlyingStore()
    {
        final String aTestUserName = "username";

        // Create a mock backing store for the cache that will only expect to be invoked one time, this
        // allows us to verify that the second time we call the method we are getting the value from the
        // cache and not the backing store.
        final ExternalEntityStore mockExternalEntityStore = mock(ExternalEntityStore.class);
        when(mockExternalEntityStore.createIfDoesNotExist(aTestUserName)).thenReturn(1L);

        final CachingExternalEntityStore entityStore = new CachingExternalEntityStore(mockExternalEntityStore);

        // Do it a second time to get the value from the underlying store
        assertEquals(new Long(1), entityStore.createIfDoesNotExist(aTestUserName));

        // Do it a second time to make sure we get the value from the cache
        assertEquals(new Long(1), entityStore.createIfDoesNotExist(aTestUserName));

        // Make sure we only retrieve from the store the first time.
        verify(mockExternalEntityStore, times(1)).createIfDoesNotExist(aTestUserName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateAnEntryForANullUserName()
    {
        CachingExternalEntityStore entityStore = new CachingExternalEntityStore(null);
        entityStore.createIfDoesNotExist(null);
    }
}
