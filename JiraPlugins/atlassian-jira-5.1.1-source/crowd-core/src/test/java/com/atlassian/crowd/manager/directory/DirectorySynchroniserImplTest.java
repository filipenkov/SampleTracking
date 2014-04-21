package com.atlassian.crowd.manager.directory;

import com.atlassian.crowd.directory.SynchronisableDirectory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.event.directory.RemoteDirectorySynchronisedEvent;
import com.atlassian.crowd.manager.lock.DirectoryLockManager;
import com.atlassian.event.api.EventPublisher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.concurrent.locks.Lock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Tests {@link DirectorySynchroniserImpl}.
 */
public class DirectorySynchroniserImplTest
{
    private static final long DIRECTORY_ID = 1L;

    @Mock
    private DirectoryLockManager directoryLockManager;
    @Mock
    private DirectoryDao directoryDao;
    @Mock
    private DirectorySynchroniserHelper directorySynchroniserHelper;
    @Mock
    private SynchronisableDirectory synchronisableDirectory;
    @Mock
    private Directory directory;
    @Mock
    private Lock lock;
    @Mock
    private SynchronisationStatusManager synchronisationStatusManager;
    @Mock
    private EventPublisher eventPublisher;

    private DirectorySynchroniserImpl directorySynchroniser;

    @Before
    public void setUp() throws Exception
    {
        initMocks(this);
        directorySynchroniser = new DirectorySynchroniserImpl(directoryLockManager, directoryDao, directorySynchroniserHelper, synchronisationStatusManager, eventPublisher);
    }

    @After
    public void tearDown() throws Exception
    {
        directorySynchroniser = null;
        lock = null;
        directory = null;
        synchronisableDirectory = null;
        directorySynchroniserHelper = null;
        directoryDao = null;
        directoryLockManager = null;
    }

    /**
     * Tests that the synchronisation occurs when the directory is active and the lock is able to be acquired.
     */
    @Test
    public void testSynchronise() throws Exception
    {
        when(synchronisableDirectory.getDirectoryId()).thenReturn(DIRECTORY_ID);
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(directory.getName()).thenReturn("directory");
        when(directory.getType()).thenReturn(DirectoryType.CONNECTOR);
        when(directory.getImplementationClass()).thenReturn("implementation-class");
        when(directory.isActive()).thenReturn(true);
        when(directoryLockManager.getLock(DIRECTORY_ID)).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        directorySynchroniser.synchronise(synchronisableDirectory, SynchronisationMode.FULL);
        verify(directorySynchroniserHelper).updateSyncStartTime(eq(synchronisableDirectory));
        verify(synchronisableDirectory).synchroniseCache(SynchronisationMode.FULL, synchronisationStatusManager);
        verify(directorySynchroniserHelper).updateSyncEndTime(eq(synchronisableDirectory));
        verify(eventPublisher).publish(any(RemoteDirectorySynchronisedEvent.class));
    }

    /**
     * Tests that the synchronisation does not occur when <tt>lock.tryLock()</tt> fails.
     */
    @Test
    public void testSynchronise_TryLockFails() throws Exception
    {
        when(synchronisableDirectory.getDirectoryId()).thenReturn(DIRECTORY_ID);
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(directory.isActive()).thenReturn(true);
        when(directoryLockManager.getLock(DIRECTORY_ID)).thenReturn(lock);
        when(lock.tryLock()).thenReturn(false);
        directorySynchroniser.synchronise(synchronisableDirectory, SynchronisationMode.FULL);
        verify(directorySynchroniserHelper, never()).updateSyncStartTime(eq(synchronisableDirectory));
        verify(synchronisableDirectory, never()).synchroniseCache(SynchronisationMode.FULL, synchronisationStatusManager);
        verify(directorySynchroniserHelper, never()).updateSyncEndTime(eq(synchronisableDirectory));
        verify(eventPublisher, never()).publish(any(RemoteDirectorySynchronisedEvent.class));
    }

    /**
     * Tests that the synchronisation does not occur when the directory is inactive.
     */
    @Test
    public void testSynchronise_DirectoryInactive() throws Exception
    {
        when(synchronisableDirectory.getDirectoryId()).thenReturn(DIRECTORY_ID);
        when(directoryDao.findById(DIRECTORY_ID)).thenReturn(directory);
        when(directory.isActive()).thenReturn(false);
        directorySynchroniser.synchronise(synchronisableDirectory, SynchronisationMode.FULL);
        verify(directoryLockManager, never()).getLock(anyLong());
        verify(lock, never()).tryLock();
        verify(directorySynchroniserHelper, never()).updateSyncStartTime(eq(synchronisableDirectory));
        verify(synchronisableDirectory, never()).synchroniseCache(SynchronisationMode.FULL, synchronisationStatusManager);
        verify(directorySynchroniserHelper, never()).updateSyncEndTime(eq(synchronisableDirectory));
        verify(eventPublisher, never()).publish(any(RemoteDirectorySynchronisedEvent.class));
    }
}
