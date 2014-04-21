package com.atlassian.crowd.embedded.core;

import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.ApplicationFactory;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.impl.DefaultConnectionPoolProperties;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.application.ImmutableApplication;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CrowdDirectoryServiceImplTest
{
    private CrowdDirectoryService crowdDirectoryService;

    @Mock private ApplicationFactory applicationFactory;
    @Mock private DirectoryInstanceLoader directoryInstanceLoader;
    @Mock private DirectoryManager directoryManager;
    @Mock private ApplicationManager applicationManager;

    @Before
    public void setUp()
    {
        initMocks(this);
        crowdDirectoryService = new CrowdDirectoryServiceImpl(applicationFactory, directoryInstanceLoader, directoryManager, applicationManager);
    }

    @Test
    public void testSetConnectionPoolPropertiesOnImmutableApplication() throws Exception
    {
        Application application = ImmutableApplication.builder("crowd", ApplicationType.CROWD).build();
        when(applicationFactory.getApplication()).thenReturn(application);

        try
        {
            crowdDirectoryService.setConnectionPoolProperties(new DefaultConnectionPoolProperties());
        }
        catch (UnsupportedOperationException e)
        {
            fail("Shouldn't be trying to modify immutable map");
        }
    }
}
