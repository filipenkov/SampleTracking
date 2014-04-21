package com.atlassian.security.auth.trustedapps.seraph.filter;

import com.atlassian.security.auth.trustedapps.filter.AuthenticationListener;
import com.atlassian.security.auth.trustedapps.filter.Authenticator;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.filter.BaseLoginFilter;
import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpServletResponse;
import com.mockobjects.servlet.MockHttpSession;
import junit.framework.TestCase;

import java.security.Principal;

/**
 * Tests for {@link com.atlassian.security.auth.trustedapps.seraph.filter.SeraphAuthenticationListener}
 */
public class TestSeraphAuthenticationListener extends TestCase
{
    private AuthenticationListener authenticationListener;

    protected void setUp() throws Exception
    {
        authenticationListener = new SeraphAuthenticationListener();
    }

    protected void tearDown() throws Exception
    {
        authenticationListener = null;
    }

    public void testAuthenticationSuccess()
    {
        final Principal principal = new Principal()
        {
            public String getName()
            {
                return "some principal";
            }
        };

        final Authenticator.Result result = new Authenticator.Result.Success(
            principal);


        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpSession session = new MockHttpSession();

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);

        session.setExpectedAttribute(DefaultAuthenticator.LOGGED_IN_KEY, principal);
        session.setExpectedAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);

        request.addExpectedSetAttribute(BaseLoginFilter.OS_AUTHSTATUS_KEY, BaseLoginFilter.LOGIN_SUCCESS);


        authenticationListener.authenticationSuccess(result, request, response);

        session.verify();
        request.verify();
    }
}
