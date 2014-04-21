package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.cache.UserAuthorisationCache;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.application.GroupMapping;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CachingApplicationService}.
 *
 * @since v2.2
 */
public class CachingApplicationServicePrincipalTestCase extends ApplicationServiceTestCase
{
    private UserAuthorisationCache userAuthorisationCache;
    private CachingApplicationService cachingApplicationService;

    @Before
    @Override
    public void setUp()
    {
        super.setUp();
        userAuthorisationCache = mock(UserAuthorisationCache.class);
        cachingApplicationService = new CachingApplicationService(applicationService, userAuthorisationCache);
    }

    /**
     * Tests that {@link ApplicationService#isUserAuthorised(com.atlassian.crowd.model.application.Application, String)}
     * returns <tt>true</tt> when the authorisation result has not been cached, and the user belongs to a directory withallowAllToAuthenticate set to <tt>true</tt>.
     */
    @Test
    public void testIsUserAuthorised_SuccessNoCacheAllowAllToAuthenticate() throws Exception
    {
        when(userAuthorisationCache.isPermitted(USER1_NAME, applicationName)).thenReturn(null);
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(application.getDirectoryMapping(DIRECTORY1_ID)).thenReturn(directoryMapping1);
        when(directoryMapping1.isAllowAllToAuthenticate()).thenReturn(true);

        assertTrue(cachingApplicationService.isUserAuthorised(application, USER1_NAME));
        verify(userAuthorisationCache).setPermitted(USER1_NAME, applicationName, true);
    }

    /**
     * Tests that {@link ApplicationService#isUserAuthorised(com.atlassian.crowd.model.application.Application, String)}
     * returns <tt>true</tt> when the authorisation results has not been cached, and the user belongs to an authorised
     * group.
     */
    @Test
    public void testIsUserAuthorised_SuccessNoCacheIsNested() throws Exception
    {
        final String GROUP_NAME = "authorisedGroup";
        final GroupMapping groupMapping = mock(GroupMapping.class);

        when(userAuthorisationCache.isPermitted(USER1_NAME, applicationName)).thenReturn(null);
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(application.getDirectoryMapping(DIRECTORY1_ID)).thenReturn(directoryMapping1);
        when(directoryMapping1.isAllowAllToAuthenticate()).thenReturn(false);
        when(directoryMapping1.getAuthorisedGroups()).thenReturn(Sets.newHashSet(groupMapping));
        when(groupMapping.getGroupName()).thenReturn(GROUP_NAME);
        when(directoryManager.isUserNestedGroupMember(DIRECTORY1_ID, USER1_NAME, GROUP_NAME)).thenReturn(true);

        assertTrue(cachingApplicationService.isUserAuthorised(application, USER1_NAME));
        verify(userAuthorisationCache).setPermitted(USER1_NAME, applicationName, true);
    }

    /**
     * Tests that {@link ApplicationService#isUserAuthorised(com.atlassian.crowd.model.application.Application, String)}
     * returns <tt>true</tt> if the successful user authorisation is already in the cache.
     */
    @Test
    public void testIsUserAuthorised_SuccessCache() throws Exception
    {
        when(userAuthorisationCache.isPermitted(USER1_NAME, applicationName)).thenReturn(true);

        assertTrue(cachingApplicationService.isUserAuthorised(application, USER1_NAME));
        verify(userAuthorisationCache, never()).setPermitted(eq(USER1_NAME), eq(applicationName), anyBoolean());
    }

    /**
     * Tests that {@link ApplicationService#isUserAuthorised(com.atlassian.crowd.model.application.Application, String)}
     * returns <tt>false</tt> if the unsuccessful user authorisation is already in the cache.
     */
    @Test
    public void testIsUserAuthorised_UnsuccessfulCache() throws Exception
    {
        when(userAuthorisationCache.isPermitted(USER1_NAME, applicationName)).thenReturn(false);

        assertFalse(cachingApplicationService.isUserAuthorised(application, USER1_NAME));
        verify(userAuthorisationCache, never()).setPermitted(eq(USER1_NAME), eq(applicationName), anyBoolean());
    }

    /**
     * Tests that {@link ApplicationService#isUserAuthorised(com.atlassian.crowd.model.application.Application, String)}
     * returns <tt>false</tt> when the user does not belong to an authorised group.
     */
    @Test
    public void testIsUserAuthorised_UnsuccessfulNoCacheNotNested() throws Exception
    {
        final String GROUP_NAME = "authorisedGroup";
        final GroupMapping groupMapping = mock(GroupMapping.class);

        when(userAuthorisationCache.isPermitted(USER1_NAME, applicationName)).thenReturn(null);
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenReturn(principal1);
        when(application.getDirectoryMapping(DIRECTORY1_ID)).thenReturn(directoryMapping1);
        when(directoryMapping1.isAllowAllToAuthenticate()).thenReturn(false);
        when(directoryMapping1.getAuthorisedGroups()).thenReturn(Sets.newHashSet(groupMapping));
        when(groupMapping.getGroupName()).thenReturn(GROUP_NAME);
        when(directoryManager.isUserNestedGroupMember(DIRECTORY1_ID, USER1_NAME, GROUP_NAME)).thenReturn(false);

        assertFalse(cachingApplicationService.isUserAuthorised(application, USER1_NAME));
        // only the positive results are cached
        verify(userAuthorisationCache, never()).setPermitted(eq(USER1_NAME), eq(applicationName), anyBoolean());
    }

    /**
     * Tests that {@link ApplicationService#isUserAuthorised(com.atlassian.crowd.model.application.Application, String)}
     * returns <tt>false</tt> if the user could not be found.
     */
    @Test
    public void testIsUserAuthorised_UnsuccessfulNoCacheUserNotFound() throws Exception
    {
        when(userAuthorisationCache.isPermitted(USER1_NAME, applicationName)).thenReturn(null);
        when(application.getDirectoryMappings()).thenReturn(Arrays.asList(directoryMapping1));
        when(directoryManager.findUserByName(DIRECTORY1_ID, USER1_NAME)).thenThrow(new UserNotFoundException(USER1_NAME));

        assertFalse(cachingApplicationService.isUserAuthorised(application, USER1_NAME));
        verify(userAuthorisationCache, never()).setPermitted(eq(USER1_NAME), eq(applicationName), anyBoolean());
    }
}
