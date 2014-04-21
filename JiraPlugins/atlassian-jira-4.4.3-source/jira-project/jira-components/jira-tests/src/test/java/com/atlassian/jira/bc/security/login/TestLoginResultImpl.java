package com.atlassian.jira.bc.security.login;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

/**
 */
public class TestLoginResultImpl extends ListeningTestCase
{
    @Test
    public void testConstruction()
    {
        LoginInfo loginInfo = new LoginInfoImpl(1L,2L,3L,4L,5L,5L,6L,true);

        final LoginResultImpl loginResult = new LoginResultImpl(LoginReason.OK, loginInfo, "userName");
        assertEquals(LoginReason.OK, loginResult.getReason());
        assertEquals("userName", loginResult.getUserName());
        assertSame(loginInfo, loginResult.getLoginInfo());
    }
}
