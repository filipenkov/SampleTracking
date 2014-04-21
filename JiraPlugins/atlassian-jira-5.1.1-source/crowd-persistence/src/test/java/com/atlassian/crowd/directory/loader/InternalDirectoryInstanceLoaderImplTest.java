package com.atlassian.crowd.directory.loader;

import com.atlassian.crowd.directory.InternalDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.event.directory.DirectoryUpdatedEvent;
import com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InternalDirectoryInstanceLoaderImplTest
{
    InstanceFactory instanceFactory = null;
    EventPublisher eventPublisher = null;

    @Before
    public void setUp() throws Exception
    {
        instanceFactory = new MockInstanceFactory();
        eventPublisher = mock(EventPublisher.class);
    }    

    @Test
    public void testCanLoad() throws Exception
    {
        InternalDirectoryInstanceLoaderImpl loader = new InternalDirectoryInstanceLoaderImpl(instanceFactory, eventPublisher);
        assertFalse(loader.canLoad(""));
        assertFalse(loader.canLoad("xxx"));
        assertTrue(loader.canLoad("com.atlassian.crowd.directory.InternalDirectory"));
        assertTrue(loader.canLoad(InternalDirectory.class.getName()));
        assertFalse(loader.canLoad(RemoteDirectory.class.getName()));
        assertFalse(loader.canLoad("com.atlassian.crowd.directory.ApacheDS"));

        try
        {
            loader.canLoad(null);
            fail("NullPointerException expected");
        }
        catch (NullPointerException ex)
        {
            // expected
        }
    }

    @Test
    public void testCachingSimple() throws Exception
    {
        final DirectoryImpl directory1 = new DirectoryImpl(new InternalEntityTemplate(1L, "directory1", true, null, null));

        InternalDirectoryInstanceLoaderImpl loader = new InternalDirectoryInstanceLoaderImpl(instanceFactory, eventPublisher);
        RemoteDirectory remoteDirectory;
        // First call to getDirectory - new instance
        remoteDirectory = loader.getDirectory(directory1);
        assertEquals("Instance 1", remoteDirectory.getDescriptiveName());
        // Second call to getDirectory - cached instance
        remoteDirectory = loader.getDirectory(directory1);
        assertEquals("Instance 1", remoteDirectory.getDescriptiveName());

        // Send an event to flush the cache
        loader.handleEvent(new XMLRestoreFinishedEvent(null));
        // Next call to getDirectory - next instance
        remoteDirectory = loader.getDirectory(directory1);
        assertEquals("Instance 2", remoteDirectory.getDescriptiveName());
    }

    @Test
    public void testRawDirectoryDoesNotCache() throws Exception
    {
        final DirectoryImpl directory1 = new DirectoryImpl(new InternalEntityTemplate(1L, "directory1", true, null, null));

        InternalDirectoryInstanceLoaderImpl loader = new InternalDirectoryInstanceLoaderImpl(instanceFactory, eventPublisher);
        RemoteDirectory remoteDirectory;
        // First call to getDirectory - new instance
        remoteDirectory = loader.getDirectory(directory1);
        assertEquals("Instance 1", remoteDirectory.getDescriptiveName());
        // Raw Directory should get a new Instance
        remoteDirectory = loader.getRawDirectory(1L, null, null);
        assertEquals("Instance 2", remoteDirectory.getDescriptiveName());
        // Second call to getDirectory - cached instance
        remoteDirectory = loader.getDirectory(directory1);
        assertEquals("Instance 1", remoteDirectory.getDescriptiveName());
        // Raw Directory should get a new Instance
        remoteDirectory = loader.getRawDirectory(1L, null, null);
        assertEquals("Instance 3", remoteDirectory.getDescriptiveName());
    }

    @Test
    public void testCachingWith2Directories() throws Exception
    {
        final DirectoryImpl directory1 = new DirectoryImpl(new InternalEntityTemplate(1L, "directory1", true, null, null));
        final DirectoryImpl directory2 = new DirectoryImpl(new InternalEntityTemplate(2L, "directory2", true, null, null));

        InternalDirectoryInstanceLoaderImpl loader = new InternalDirectoryInstanceLoaderImpl(instanceFactory, eventPublisher);
        RemoteDirectory remoteDirectory;
        // First call to getDirectory - new instance
        remoteDirectory = loader.getDirectory(directory1);
        assertEquals("Instance 1", remoteDirectory.getDescriptiveName());
        // First call to getDirectory for D2 - new instance
        remoteDirectory = loader.getDirectory(directory2);
        assertEquals("Instance 2", remoteDirectory.getDescriptiveName());
        // Second call to getDirectory D1 - cached instance
        remoteDirectory = loader.getDirectory(directory1);
        assertEquals("Instance 1", remoteDirectory.getDescriptiveName());
        // Second call to getDirectory D2 - cached instance
        remoteDirectory = loader.getDirectory(directory2);
        assertEquals("Instance 2", remoteDirectory.getDescriptiveName());

        // Send an event that directory 1 is updated
        loader.handleEvent(new DirectoryUpdatedEvent(this, directory1));
        // Next call to getDirectory D1 - next instance
        remoteDirectory = loader.getDirectory(directory1);
        assertEquals("Instance 3", remoteDirectory.getDescriptiveName());
        // Directory 2 should still be cached
        remoteDirectory = loader.getDirectory(directory2);
        assertEquals("Instance 2", remoteDirectory.getDescriptiveName());
    }

    private class MockInstanceFactory implements InstanceFactory
    {
        int counter = 0;

        public Object getInstance(final String className) throws ClassNotFoundException
        {
            counter++;
            InternalDirectory remoteDirectory = mock(InternalDirectory.class);
            when(remoteDirectory.getDescriptiveName()).thenReturn("Instance " + counter);
            return remoteDirectory;
        }

        public Object getInstance(final String className, final ClassLoader classLoader) throws ClassNotFoundException
        {
            throw new UnsupportedOperationException();
        }

        public <T> T getInstance(final Class<T> clazz)
        {
            throw new UnsupportedOperationException();
        }
    }
}
