package com.atlassian.crowd.integration.http;

import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.service.AuthenticatorUserCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CacheAwareCrowdHttpAuthenticatorTest
{
    private static final String USERNAME = "joe";

    @Mock private CrowdHttpAuthenticator delegate;
    @Mock private AuthenticatorUserCache userCache;
    @Mock private User user;

    private CacheAwareCrowdHttpAuthenticator cacheAwareCrowdHttpAuthenticator;

    @Before
    public void setUp()
    {
        when(user.getName()).thenReturn(USERNAME);
        cacheAwareCrowdHttpAuthenticator = new CacheAwareCrowdHttpAuthenticator(delegate, userCache);
    }

    @Test
    public void testGetUser() throws Exception
    {
        when(delegate.getUser(null)).thenReturn(user);

        final User returnedUser = cacheAwareCrowdHttpAuthenticator.getUser(null);

        verify(userCache).fetchInCache(USERNAME);
        assertEquals(user, returnedUser);
    }

    @Test
    public void testAuthenticate() throws Exception
    {
        when(delegate.authenticate(null, null, USERNAME, null)).thenReturn(user);

        final User returnedUser = cacheAwareCrowdHttpAuthenticator.authenticate(null, null, USERNAME, null);

        verify(userCache).fetchInCache(USERNAME);
        assertEquals(user, returnedUser);
    }

    @Test
    public void testAuthenticateWithoutValidatingPassword() throws Exception
    {
        when(delegate.authenticateWithoutValidatingPassword(null, null, USERNAME)).thenReturn(user);

        final User returnedUser = cacheAwareCrowdHttpAuthenticator.authenticateWithoutValidatingPassword(null, null, USERNAME);

        verify(userCache).fetchInCache(USERNAME);
        assertEquals(user, returnedUser);
    }

    @Test
    public void testIsAuthenticated() throws Exception
    {
        when(delegate.isAuthenticated(null, null)).thenReturn(true);

        final boolean isAuthenticated =  cacheAwareCrowdHttpAuthenticator.isAuthenticated(null, null);

        verify(userCache, never()).fetchInCache(USERNAME);
        assertTrue(isAuthenticated);
    }

    @Test
    public void logout() throws Exception
    {
        cacheAwareCrowdHttpAuthenticator.logout(null, null);

        verify(delegate).logout(null, null);
        verify(userCache, never()).fetchInCache(USERNAME);
    }

    @Test
    public void getToken() throws Exception
    {
        when(delegate.getToken(null)).thenReturn("token");

        assertEquals("token", cacheAwareCrowdHttpAuthenticator.getToken(null));

        verify(userCache, never()).fetchInCache(USERNAME);
    }
}
