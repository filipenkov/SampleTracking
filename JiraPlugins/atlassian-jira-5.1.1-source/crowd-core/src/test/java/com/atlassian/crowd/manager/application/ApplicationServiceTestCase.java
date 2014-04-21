package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.cache.UserAuthorisationCache;
import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.event.EventStore;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.manager.permission.PermissionManager;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.application.DirectoryMapping;
import com.atlassian.crowd.model.user.User;
import com.atlassian.event.api.EventPublisher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Parent class for the ApplicationManager test cases
 */
public abstract class ApplicationServiceTestCase
{
    protected static final long DIRECTORY1_ID = 1L;
    protected static final long DIRECTORY2_ID = 2L;
    protected static final long DIRECTORY3_ID = 3L;
    protected static final long INACTIVE_DIRECTORY_ID = 300L;

    protected static final String DIRECTORY1_NAME = "Test Directory 1";
    protected static final String DIRECTORY2_NAME = "Test Directory 2";
    protected static final String DIRECTORY3_NAME = "Test Directory 3";
    protected static final String INACTIVE_DIRECTORY_NAME = "Inactive Directory";

    protected static final String USER1_NAME = "USERNAME 1";
    protected static final String USER2_NAME = "USERNAME 2";
    protected static final String USER3_NAME = "USERNAME 3";
    protected static final String INACTIVE_USER_NAME = "INACTIVE USERNAME";

    protected ApplicationServiceGeneric applicationService;
    protected PermissionManager permissionManager;
    protected DirectoryManager directoryManager;
    protected DirectoryInstanceLoader directoryInstanceLoader;
    protected EventPublisher mockEventPublisher;
    protected UserAuthorisationCache userAuthorisationCache;
    protected EventStore mockEventStore;

    protected Application application;
    protected DirectoryMapping directoryMapping1;
    protected DirectoryMapping directoryMapping2;
    protected DirectoryMapping directoryMapping3;
    protected DirectoryMapping inactiveDirectoryMapping;

    protected Directory directory1;
    protected Directory directory2;
    protected Directory directory3;
    protected Directory inactiveDirectory;
    protected User principal1;
    protected User principal2;
    protected User principal3;
    protected User inactivePrincipal;


    protected final String applicationName = "My Test App";

    protected void setUp()
    {
        // Mock objects used by the application manager
        permissionManager = mock(PermissionManager.class);
        directoryManager = mock(DirectoryManager.class);
        directoryInstanceLoader = mock(DirectoryInstanceLoader.class);
        mockEventPublisher = mock(EventPublisher.class);
        mockEventStore = mock(EventStore.class);

        // Objects used by all tests
        application = mock(Application.class);
        when(application.getName()).thenReturn(applicationName);
        when(application.getType()).thenReturn(ApplicationType.GENERIC_APPLICATION);
        when(application.getCredential()).thenReturn(PasswordCredential.unencrypted("myPassword"));
        when(application.getId()).thenReturn(1L);
        when(application.isActive()).thenReturn(true);
        when(application.getDescription()).thenReturn("desc");

        directory1 = mock(Directory.class);
        when(directory1.getId()).thenReturn(DIRECTORY1_ID);
        when(directory1.getName()).thenReturn(DIRECTORY1_NAME);
        when(directory1.isActive()).thenReturn(true);
        directoryMapping1 = mock(DirectoryMapping.class);
        when(directoryMapping1.getApplication()).thenReturn(application);
        when(directoryMapping1.getDirectory()).thenReturn(directory1);
        when(directoryMapping1.isAllowAllToAuthenticate()).thenReturn(false);

        directory2 = mock(Directory.class);
        when(directory2.getId()).thenReturn(DIRECTORY2_ID);
        when(directory2.getName()).thenReturn(DIRECTORY2_NAME);
        when(directory2.isActive()).thenReturn(true);
        directoryMapping2 = mock(DirectoryMapping.class);
        when(directoryMapping2.getApplication()).thenReturn(application);
        when(directoryMapping2.getDirectory()).thenReturn(directory2);
        when(directoryMapping2.isAllowAllToAuthenticate()).thenReturn(false);

        directory3 = mock(Directory.class);
        when(directory3.getId()).thenReturn(DIRECTORY3_ID);
        when(directory3.getName()).thenReturn(DIRECTORY3_NAME);
        when(directory3.isActive()).thenReturn(true);
        directoryMapping3 = mock(DirectoryMapping.class);
        when(directoryMapping3.getApplication()).thenReturn(application);
        when(directoryMapping3.getDirectory()).thenReturn(directory3);
        when(directoryMapping3.isAllowAllToAuthenticate()).thenReturn(false);

        inactiveDirectory = mock(Directory.class);
        when(inactiveDirectory.getId()).thenReturn(INACTIVE_DIRECTORY_ID);
        when(inactiveDirectory.getName()).thenReturn(INACTIVE_DIRECTORY_NAME);
        when(inactiveDirectory.isActive()).thenReturn(false);
        inactiveDirectoryMapping = mock(DirectoryMapping.class);
        when(inactiveDirectoryMapping.getApplication()).thenReturn(application);
        when(inactiveDirectoryMapping.getDirectory()).thenReturn(inactiveDirectory);
        when(inactiveDirectoryMapping.isAllowAllToAuthenticate()).thenReturn(false);

        principal1 = mock(User.class);
        when(principal1.getName()).thenReturn(USER1_NAME);
        when(principal1.getDirectoryId()).thenReturn(DIRECTORY1_ID);

        principal2 = mock(User.class);
        when(principal2.getName()).thenReturn(USER2_NAME);
        when(principal2.getDirectoryId()).thenReturn(DIRECTORY2_ID);

        principal3 = mock(User.class);
        when(principal3.getName()).thenReturn(USER3_NAME);
        when(principal3.getDirectoryId()).thenReturn(DIRECTORY3_ID);

        inactivePrincipal = mock(User.class);
        when(inactivePrincipal.getName()).thenReturn(INACTIVE_USER_NAME);
        when(inactivePrincipal.getDirectoryId()).thenReturn(INACTIVE_DIRECTORY_ID);

        userAuthorisationCache = mock(UserAuthorisationCache.class);

        applicationService = new ApplicationServiceGeneric(directoryManager, permissionManager, directoryInstanceLoader, mockEventPublisher, mockEventStore);
    }

    protected void tearDown()
    {
        applicationService = null;
        permissionManager = null;
        directoryManager = null;
        directoryInstanceLoader = null;
        mockEventPublisher = null;
        application = null;
        directoryMapping1 = null;
        directoryMapping2 = null;
        directoryMapping3 = null;
        inactiveDirectoryMapping = null;

        directory1 = null;
        directory2 = null;
        directory3 = null;
        inactiveDirectory = null;
        principal1 = null;
        principal2 = null;
        principal3 = null;
        inactivePrincipal = null;
    }
}
