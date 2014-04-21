package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.directory.DirectoryProperties;
import com.atlassian.crowd.event.Events;
import com.atlassian.crowd.event.IncrementalSynchronisationNotAvailableException;
import com.atlassian.crowd.model.event.OperationEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestCase to cover Event based operations on the ApplicationManager
 */
public class ApplicationServiceEventsTestCase extends ApplicationServiceTestCase
{
    @Before
    public void setUp()
    {
        super.setUp();
    }

    @After
    public void tearDown()
    {
        super.tearDown();
    }

    @Test(expected = IncrementalSynchronisationNotAvailableException.class)
    public void testNoIncrementalSyncWithNonCachedDirectory() throws Exception
    {
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directory1.getValue(DirectoryProperties.CACHE_ENABLED)).thenReturn("false");
        final OperationEvent operationEvent = mock(OperationEvent.class);
        when(mockEventStore.getNewEvents(anyString())).thenReturn(new Events(Collections.singleton(operationEvent), "token"));

        applicationService.getNewEvents(application, "token");
    }
}
