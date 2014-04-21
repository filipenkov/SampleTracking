package com.atlassian.security.auth.trustedapps.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

/**
 */
public class MockTrustedApplicationsFilter extends TrustedApplicationsFilter
{
    private final MockCertificateServer certificateServer;
    private final MockAuthenticator mockAuthenticator;

    public MockTrustedApplicationsFilter()
    {
        this(new MockCertificateServer(), new MockAuthenticator());
    }

    private MockTrustedApplicationsFilter(MockCertificateServer certificateServer, MockAuthenticator mockAuthenticator)
    {
        super(certificateServer, mockAuthenticator, new MockAuthenticationController(), new MockAuthenticationListener());
        this.certificateServer = certificateServer;
        this.mockAuthenticator = mockAuthenticator;
    }

    public MockCertificateServer getMockCertificateServer()
    {
        return certificateServer;
    }

    public MockAuthenticator getMockAuthenticator()
    {
        return mockAuthenticator;
    }

    private static class MockAuthenticationController implements AuthenticationController
    {
        public boolean shouldAttemptAuthentication(HttpServletRequest request)
        {
            return true;
        }

        public boolean canLogin(Principal principal, HttpServletRequest request)
        {
            return true;
        }
    }

    private static class MockAuthenticationListener implements AuthenticationListener
    {
        public void authenticationSuccess(Authenticator.Result result, HttpServletRequest request, HttpServletResponse response)
        {
        }

        public void authenticationFailure(Authenticator.Result result, HttpServletRequest request, HttpServletResponse response)
        {
        }

        public void authenticationError(Authenticator.Result result, HttpServletRequest request, HttpServletResponse response)
        {
        }

        public void authenticationNotAttempted(HttpServletRequest request, HttpServletResponse response)
        {
        }
    }
}
