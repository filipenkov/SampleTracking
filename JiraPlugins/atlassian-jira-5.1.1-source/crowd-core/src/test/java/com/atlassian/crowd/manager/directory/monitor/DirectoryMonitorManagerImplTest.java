package com.atlassian.crowd.manager.directory.monitor;

import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.SynchronisableDirectory;
import com.atlassian.crowd.directory.monitor.poller.DirectoryPoller;
import com.atlassian.crowd.manager.directory.monitor.poller.DirectoryPollerManager;
import com.atlassian.event.api.EventPublisher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryMonitorManagerImplTest
{
    @Mock
    private DirectoryPollerManager directoryPollerManager;

    @Mock
    private EventPublisher eventPublisher;

    @Test
    public void pollerIsAddedForSynchronisableDirectory() throws Exception
    {
        DirectoryMonitorManagerImpl impl = new DirectoryMonitorManagerImpl(directoryPollerManager, null, eventPublisher);

        RemoteDirectory dir = mock(SynchronisableDirectory.class);
        impl.addMonitor(dir);
        verify(directoryPollerManager).addPoller(Mockito.<DirectoryPoller>any());
    }

    @Test(expected = DirectoryMonitorAlreadyRegisteredException.class)
    public void monitorRegistrationIsDetectedWithWriteLockHeld() throws Exception
    {
        DirectoryMonitorManagerImpl impl = new DirectoryMonitorManagerImpl(directoryPollerManager, null, eventPublisher);

        /* The first check with the read lock, the second with the write lock */
        when(directoryPollerManager.hasPoller(0)).thenReturn(false).thenReturn(true);
        Mockito.doThrow(new RuntimeException()).when(directoryPollerManager).addPoller(Mockito.<DirectoryPoller>any());

        RemoteDirectory dir = mock(SynchronisableDirectory.class);
        impl.addMonitor(dir);
    }
}
