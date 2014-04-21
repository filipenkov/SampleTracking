package com.atlassian.upm.token;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class TokenManagerImplTest
{
    private TokenManager tokenManager;
    private static final String USERNAME = "admin";

    @Before
    public void before()
    {
        tokenManager = new TokenManagerImpl();
    }

    @Test
    public void assertThatGeneratedTokenForUserMatchesAtFirst() throws Exception
    {
        String token = tokenManager.getTokenForUser(USERNAME);
        assertTrue(tokenManager.attemptToMatchAndInvalidateToken(USERNAME, token));
    }

    @Test
    public void assertThatGeneratedTokenForUserMatchesExactlyOnce() throws Exception
    {
        String token = tokenManager.getTokenForUser(USERNAME);
        tokenManager.attemptToMatchAndInvalidateToken(USERNAME, token);
        assertFalse(tokenManager.attemptToMatchAndInvalidateToken(USERNAME, token));
    }

    @Test
    public void assertThatAttemptToMatchAndInvalidateTokenDoesNotThrowNullPointerExceptionWhenThereIsNoStoredTokenForUser() throws Exception
    {
        // Before, attempting to call TokenManager.attemptToMatchAndInvalidateToken() without calling
        // TokenManager.getTokenForUser() first will throw a NPE. This was fixed in UPM-1022, and this test ensures that
        // TokenManager.attemptToMatchAndInvalidateToken() will not throw a NPE.
        assertFalse(tokenManager.attemptToMatchAndInvalidateToken(USERNAME, "somerandomtoken"));
    }
}
