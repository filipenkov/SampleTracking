package com.atlassian.labs.botkiller;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 */
public class BotKillerTest
{

    public static final Integer MAX_INACTIVE_INTERVAL = 10000;
    private static final int LOW_INACTIVE_TIMEOUT = 60;
    private static final int USER_LOW_INACTIVE_TIMEOUT = 10 * 60;


    private MockHttpServletRequest httpServletRequest;
    private BotKiller botKiller;

    @Before
    public void setUp() throws Exception
    {
        httpServletRequest = new MockHttpServletRequest();
        botKiller = new BotKiller(new MockUserManager(null));
    }

    @Test
    public void testNoSessionIsAccidentallyEquated() throws Exception
    {
        botKiller.processRequest(httpServletRequest);

        assertNull(httpServletRequest.getSession(false));
    }

    @Test
    public void testRequestHasUserGetsDifferentTimeout() throws Exception
    {
        httpServletRequest.setRemoteUser("bill");
        HttpSession session = httpServletRequest.getSession(true);// make a session
        session.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL);

        botKiller.processRequest(httpServletRequest);

        assertEquals(USER_LOW_INACTIVE_TIMEOUT, session.getMaxInactiveInterval());
        assertEquals(MAX_INACTIVE_INTERVAL, session.getAttribute(BotKiller.class.getName()));
    }


    @Test
    public void testWhenTheyHaveALowDefaultSessionTimeout()
    {
        httpServletRequest.setRemoteUser("bill");
        HttpSession session = httpServletRequest.getSession(true);// make a session
        session.setMaxInactiveInterval(5);

        botKiller.processRequest(httpServletRequest);

        assertEquals(5, session.getMaxInactiveInterval());
        assertNull(session.getAttribute(BotKiller.class.getName()));
    }

    @Test
    public void testNeverSeenThisSessionSoItsLowered() throws Exception
    {
        HttpSession session = httpServletRequest.getSession(true);// make a session
        session.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL);

        botKiller.processRequest(httpServletRequest);

        assertEquals(LOW_INACTIVE_TIMEOUT, session.getMaxInactiveInterval());
        assertEquals(MAX_INACTIVE_INTERVAL, session.getAttribute(BotKiller.class.getName()));

        // now have a second request
        botKiller.processRequest(httpServletRequest);
        assertEquals(MAX_INACTIVE_INTERVAL, Integer.valueOf(session.getMaxInactiveInterval()));
        assertEquals(MAX_INACTIVE_INTERVAL, session.getAttribute(BotKiller.class.getName()));

        // any future requests is still the same timeout on the session

        for (int i = 0; i < 10; i++)
        {
            botKiller.processRequest(httpServletRequest);
            assertEquals(MAX_INACTIVE_INTERVAL, Integer.valueOf(session.getMaxInactiveInterval()));
            assertEquals(MAX_INACTIVE_INTERVAL, session.getAttribute(BotKiller.class.getName()));

        }
    }

    @Test
    public void testErrorWhenCheckingUsernameDoesNotKillBotKiller()
    {
        botKiller = new BotKiller(new MockUserManager(null)
        {
            @Override
            public String getRemoteUsername(HttpServletRequest request)
            {
                throw new RuntimeException("a most unexpected error");
            }
        });

        httpServletRequest.setRemoteUser("bill");
        HttpSession session = httpServletRequest.getSession(true);// make a session
        session.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL);

        botKiller.processRequest(httpServletRequest);

        assertEquals(LOW_INACTIVE_TIMEOUT, session.getMaxInactiveInterval());
        assertEquals(MAX_INACTIVE_INTERVAL, session.getAttribute(BotKiller.class.getName()));
    }

}
