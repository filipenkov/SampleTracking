package com.atlassian.crowd.event;

import com.atlassian.crowd.embedded.api.Directory;

import org.junit.Test;

import static org.mockito.Mockito.mock;

public class DirectoryEventTest
{
    @Test(expected = NullPointerException.class)
    public void directoryMayNotBeNull()
    {
        new ConcreteDirectoryEvent(null, null);
    }
    
    @Test
    public void sourceMayBeNull()
    {
        new ConcreteDirectoryEvent(null, mock(Directory.class));
    }

    private static class ConcreteDirectoryEvent extends DirectoryEvent
    {
        public ConcreteDirectoryEvent(Object source, Directory directory)
        {
            super(source, directory);
        }
    }
}
