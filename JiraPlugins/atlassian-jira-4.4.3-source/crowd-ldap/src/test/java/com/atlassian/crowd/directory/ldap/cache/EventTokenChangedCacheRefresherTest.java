package com.atlassian.crowd.directory.ldap.cache;

import com.atlassian.crowd.directory.RemoteCrowdDirectory;
import com.atlassian.crowd.directory.SynchronisableDirectoryProperties;
import com.atlassian.crowd.event.Events;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.model.event.OperationEvent;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventTokenChangedCacheRefresherTest
{
    private EventTokenChangedCacheRefresher cacheRefresher;

    @Mock
    private RemoteCrowdDirectory remoteCrowdDirectory;

    @Before
    public void setUp() throws Exception
    {
        when(remoteCrowdDirectory.isRolesDisabled()).thenReturn(true);
        when(remoteCrowdDirectory.getValue(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED)).thenReturn("true");
        when(remoteCrowdDirectory.getMemberships()).thenReturn(
                Collections.<Membership>emptyList());

        cacheRefresher = new EventTokenChangedCacheRefresher(remoteCrowdDirectory);
    }

    @Test
    public void testSynchroniseAllSynchronisesWhenEventTokenNotAvailable() throws Exception
    {
        final DirectoryCache directoryCache = mock(DirectoryCache.class);

        when(remoteCrowdDirectory.getCurrentEventToken()).thenThrow(new OperationFailedException());

        cacheRefresher.synchroniseAll(directoryCache);

        verify(remoteCrowdDirectory).searchGroups(any(EntityQuery.class));
    }

    @Test
    public void testSynchroniseAllUpdatesEventToken() throws Exception
    {
        final DirectoryCache directoryCache = mock(DirectoryCache.class);

        when(remoteCrowdDirectory.getCurrentEventToken()).thenReturn("token");

        cacheRefresher.synchroniseAll(directoryCache);

        when(remoteCrowdDirectory.getNewEvents("token")).thenReturn(new Events(Collections.<OperationEvent>emptyList(), "token2"));

        assertTrue(cacheRefresher.synchroniseChanges(directoryCache));
    }

    @Test
    public void testIncrementalSyncCanBeDisabled() throws Exception
    {
        final DirectoryCache directoryCache = mock(DirectoryCache.class);

        when(remoteCrowdDirectory.getCurrentEventToken()).thenReturn("token");

        cacheRefresher.synchroniseAll(directoryCache);

        when(remoteCrowdDirectory.getValue(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED)).thenReturn("false");

        assertFalse(cacheRefresher.synchroniseChanges(directoryCache));
    }

    @Test
    public void testSynchroniseChangesFailsWithNoEventToken() throws Exception
    {
        assertFalse(cacheRefresher.synchroniseChanges(null));
    }
}
