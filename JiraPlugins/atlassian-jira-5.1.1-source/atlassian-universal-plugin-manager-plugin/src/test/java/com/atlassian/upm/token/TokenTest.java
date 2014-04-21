package com.atlassian.upm.token;

import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenTest
{
    private static final String TOKEN_STRING = "44357894";

    /**
     * this test could fail if it somehow took >5min to check if the token is expired after creating it, but that shouldn't happen
     */
    @Test
    public void testNewlyCreatedTokenIsNotExpired() throws Exception
    {
        Token token = new Token(TOKEN_STRING, new Date());
        assertFalse(token.isExpired());
    }

    @Test
    public void testTokenFromTenMinutesAgoIsExpired() throws Exception
    {
        Date currentDate = new Date();
        Date tenMinutesAgoDate = new Date(currentDate.getTime() - 10 * 60 * 1000);
        Token token = new Token(TOKEN_STRING, tenMinutesAgoDate);
        assertTrue(token.isExpired());
    }
}
