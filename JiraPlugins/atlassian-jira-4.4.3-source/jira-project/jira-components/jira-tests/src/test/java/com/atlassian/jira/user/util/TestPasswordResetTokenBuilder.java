package com.atlassian.jira.user.util;

import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.util.ConstantClock;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.core.util.StaticCrowdServiceFactory;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.opensymphony.user.User;
import com.atlassian.jira.local.ListeningTestCase;
import mock.user.MockOSUser;

import java.util.Date;

/**
 */
public class TestPasswordResetTokenBuilder extends ListeningTestCase
{

    private static final String PASSWORD_RESET_REQUEST_TOKEN = "password.reset.request.token";
    private static final String PASSWORD_RESET_REQUEST_EXPIRY = "password.reset.request.expiry";

    private User fred;
    private ConstantClock constantClock;
    private long constantTime;
    private CrowdService crowdService;

    @Before
    public void setUp() throws Exception
    {

        fred = new MockOSUser("fred");

        crowdService = new MockCrowdService();
        crowdService.addUser(fred, "-");
        constantClock = new ConstantClock(new Date());
        constantTime = constantClock.getCurrentDate().getTime();
    }

    @Test
    public void test_generateToken_nulluser()
    {
        try
        {
            new PasswordResetTokenBuilder(crowdService).generateToken(null);
            fail("Should have barfed above");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void test_generateToken()
    {
        long expectedTime = constantTime + 24 * 60 * 60 * 1000;
        final UserUtil.PasswordResetToken token = new PasswordResetTokenBuilder(constantClock, crowdService).generateToken(fred);

        assertNotNull(token);
        assertNotNull(token.getToken());
        assertEquals(fred, token.getUser());
        assertEquals(expectedTime, token.getExpiryTime());
        assertEquals(24, token.getExpiryHours());
    }

    @Test
    public void test_validateToken_noTokenRecorded()
    {
        final UserUtil.PasswordResetTokenValidation.Status status = new PasswordResetTokenBuilder(constantClock, crowdService).validateToken(fred, "123456");
        assertEquals(UserUtil.PasswordResetTokenValidation.Status.EXPIRED, status);
    }

    @Test
    public void test_validateToken_expiredTokenPresented() throws OperationNotPermittedException
    {
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_TOKEN, "123456");
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_EXPIRY, "1");

        final UserUtil.PasswordResetTokenValidation.Status status = new PasswordResetTokenBuilder(constantClock, crowdService).validateToken(fred, "123456");
        assertEquals(UserUtil.PasswordResetTokenValidation.Status.EXPIRED, status);
    }

    @Test
    public void test_validateToken_invalidTokenPresented() throws OperationNotPermittedException
    {
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_TOKEN, "ABCDEF");
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_EXPIRY, String.valueOf(constantTime + 1000));

        final UserUtil.PasswordResetTokenValidation.Status status = new PasswordResetTokenBuilder(constantClock, crowdService).validateToken(fred, "123456");
        assertEquals(UserUtil.PasswordResetTokenValidation.Status.UNEQUAL, status);
    }

    @Test
    public void test_validateToken_OK() throws OperationNotPermittedException
    {
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_TOKEN, "123456");
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_EXPIRY, String.valueOf(constantTime + 1000));

        final UserUtil.PasswordResetTokenValidation.Status status = new PasswordResetTokenBuilder(constantClock, crowdService).validateToken(fred, "123456");
        assertEquals(UserUtil.PasswordResetTokenValidation.Status.OK, status);
    }

    @Test
    public void test_resetToken() throws OperationNotPermittedException
    {
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_TOKEN, "123456");
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_EXPIRY, String.valueOf(constantTime + 1000));

        new PasswordResetTokenBuilder(constantClock, crowdService).resetToken(fred);

        assertNull(crowdService.getUserWithAttributes(fred.getName()).getValue(PASSWORD_RESET_REQUEST_TOKEN));
        assertNull(crowdService.getUserWithAttributes(fred.getName()).getValue(PASSWORD_RESET_REQUEST_EXPIRY));

    }
}
