package com.atlassian.security.auth.trustedapps.filter;

import java.security.Principal;
import java.security.PublicKey;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.security.auth.trustedapps.ApplicationCertificate;
import com.atlassian.security.auth.trustedapps.CurrentApplication;
import com.atlassian.security.auth.trustedapps.DefaultApplicationCertificate;
import com.atlassian.security.auth.trustedapps.EncryptedCertificate;
import com.atlassian.security.auth.trustedapps.InvalidCertificateException;
import com.atlassian.security.auth.trustedapps.InvalidRemoteAddressException;
import com.atlassian.security.auth.trustedapps.InvalidRequestUrlException;
import com.atlassian.security.auth.trustedapps.InvalidXForwardedForAddressException;
import com.atlassian.security.auth.trustedapps.RequestConditions;
import com.atlassian.security.auth.trustedapps.SystemException;
import com.atlassian.security.auth.trustedapps.TrustedApplication;
import com.atlassian.security.auth.trustedapps.TrustedApplicationUtils;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.security.auth.trustedapps.UserResolver;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestTrustedAppAuthenticatorImpl extends TestCase
{
    private static final String USER = "blad-de-blah-blah";
    private final PublicKey publicKey = new PublicKey()
    {
        public String getAlgorithm()
        {
            return "algy";
        }

        public byte[] getEncoded()
        {
            return new byte[] { 4, 3, 2, 1 };
        }

        public String getFormat()
        {
            return "format";
        }
    };

    private final TrustedApplicationsManager manager = new TrustedApplicationsManager()
    {
        final CurrentApplication me = new CurrentApplication()
        {
            public String getID()
            {
                return "me-id";
            }

            public PublicKey getPublicKey()
            {
                return publicKey;
            }

            public EncryptedCertificate encode(final String userName)
            {
                EncryptedCertificate cert = mock(EncryptedCertificate.class);
                when(cert.getCertificate()).thenReturn(userName);
                when(cert.getID()).thenReturn("me-id");
                when(cert.getMagicNumber()).thenReturn("1");
                when(cert.getProtocolVersion()).thenReturn(Integer.valueOf(1));
                when(cert.getSecretKey()).thenReturn("secret");
                return cert;
            }
            
            public EncryptedCertificate encode(String userName, String urlToSign)
            {
                throw new UnsupportedOperationException();
            }
        };

        public CurrentApplication getCurrentApplication()
        {
            return me;
        }

        public TrustedApplication getTrustedApplication(String id)
        {
            if (id.equals("me-id"))
            {
                return new TrustedApplication()
                {
                    public ApplicationCertificate decode(EncryptedCertificate certificate, HttpServletRequest request) throws InvalidCertificateException
                    {
                        if (request.getHeader("ip-mismatch") != null)
                        {
                            throw new InvalidCertificateException(new InvalidRemoteAddressException(request.getRemoteAddr()));
                        }
                        if (request.getHeader("forward-mismatch") != null)
                        {
                            throw new InvalidCertificateException(new InvalidXForwardedForAddressException(request.getRemoteAddr()));
                        }
                        if (request.getHeader("url-mismatch") != null)
                        {
                            throw new InvalidCertificateException(new InvalidRequestUrlException(request.getPathInfo()));
                        }
                        if ("bad-cert".equals(certificate.getCertificate()))
                        {
                            throw new InvalidCertificateException(new SystemException("bad-cert", new NullPointerException("what a bad certificate!")));
                        }
                        return new DefaultApplicationCertificate("me-id", certificate.getCertificate(), System.currentTimeMillis());
                    }

                    public PublicKey getPublicKey()
                    {
                        return publicKey;
                    }

                    public String getID()
                    {
                        return "me-id";
                    }

                    public RequestConditions getRequestConditions()
                    {
                        return null;
                    }

                    public String getName()
                    {
                        return null;
                    }
                };
            }
            return null;
        }
    };

    private final Principal principal = new Principal()
    {
        public String getName()
        {
            return USER;
        }
    };

    private final UserResolver resolver = new UserResolver()
    {
        public Principal resolve(ApplicationCertificate certificate)
        {
            return (certificate.getUserName().equals(principal.getName())) ? principal : null;
        };
    };

    private final boolean[] canLogin = new boolean[] { true };
    private final AuthenticationController authenticationController = new AuthenticationController()
    {
        public boolean shouldAttemptAuthentication(HttpServletRequest request)
        {
            return true;
        }

        public boolean canLogin(Principal user, HttpServletRequest request)
        {
            return canLogin[0];
        }
    };

    private final Authenticator authenticator = new TrustedApplicationFilterAuthenticator(manager, resolver, authenticationController)
    {
        public Result authenticate(HttpServletRequest request, HttpServletResponse response)
        {
            return super.authenticate(ImmutableRequest.wrap(request), response);
        }
    };

    protected void setUp() throws Exception
    {
        canLogin[0] = true;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // tests
    // -----------------------------------------------------------------------------------------------------------------

    public void testKnownAppProtocolVersion0() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode(USER);
        MockRequest request = new MockTrustedAppRequestV0(cert);

        Authenticator.Result result = authenticator.authenticate(request, new MockResponse());

        assertNotNull(result);
        assertEquals(Authenticator.Result.Status.SUCCESS, result.getStatus());
    }

    public void testKnownAppProtocolVersion1() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode(USER);
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);

        Authenticator.Result result = authenticator.authenticate(request, new MockResponse());

        assertNotNull(result);
        assertEquals(Authenticator.Result.Status.SUCCESS, result.getStatus());
        assertNotNull(result.getUser());
    }

    public void testUnknownApp() throws Exception
    {
        EncryptedCertificate cert = mock(EncryptedCertificate.class);
        when(cert.getCertificate()).thenReturn("cert");
        when(cert.getID()).thenReturn("appId");
        when(cert.getMagicNumber()).thenReturn("majick");
        when(cert.getSecretKey()).thenReturn("dis-is-a-sekrit");
        
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);
        // CurrentApplication me = manager.getCurrentApplication();

        Authenticator.Result result = authenticator.authenticate(request, new MockResponse());

        assertNotNull(result);
        assertEquals(Authenticator.Result.Status.FAILED, result.getStatus());
        assertEquals("APP_UNKNOWN;\tUnknown Application: {0};\t[\"appId\"]", result.getMessage());
        assertNull(result.getUser());
    }

    public void testBlankAppId() throws Exception
    {
        EncryptedCertificate cert = mock(EncryptedCertificate.class);
        when(cert.getCertificate()).thenReturn("cert");
        when(cert.getMagicNumber()).thenReturn("majick");
        when(cert.getSecretKey()).thenReturn("dis-is-a-sekrit");
        
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);
        // CurrentApplication me = manager.getCurrentApplication();

        Authenticator.Result result = authenticator.authenticate(request, new MockResponse());

        assertNotNull(result);
        assertEquals(Authenticator.Result.Status.ERROR, result.getStatus());
        assertEquals("APP_ID_NOT_FOUND;\tApplication ID not found in request;\t[]", result.getMessage());
        assertNull(result.getUser());
    }

    public void testBadProtocolVersion() throws Exception
    {
        EncryptedCertificate cert = mock(EncryptedCertificate.class);
        when(cert.getCertificate()).thenReturn("cert");
        when(cert.getID()).thenReturn("appId");
        when(cert.getMagicNumber()).thenReturn("majick");
        when(cert.getSecretKey()).thenReturn("dis-is-a-sekrit");
        
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);
        request.addHeader(TrustedApplicationUtils.Header.Request.VERSION, "a-dodgy-version");

        Authenticator.Result result = authenticator.authenticate(request, new MockResponse());

        assertNotNull(result);
        assertEquals(Authenticator.Result.Status.ERROR, result.getStatus());
        assertEquals("BAD_PROTOCOL_VERSION;\tBad protocol version: {0};\t[\"a-dodgy-version\"]", result.getMessage());
        assertNull(result.getUser());
    }

    public void testMissingSecretKey() throws Exception
    {
        EncryptedCertificate cert = mock(EncryptedCertificate.class);
        when(cert.getCertificate()).thenReturn("cert");
        when(cert.getID()).thenReturn("appId");
        when(cert.getMagicNumber()).thenReturn("majick");
        when(cert.getSecretKey()).thenReturn("");
        
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);

        Authenticator.Result result = authenticator.authenticate(request, new MockResponse());

        assertNotNull(result);
        assertEquals(Authenticator.Result.Status.ERROR, result.getStatus());
        assertEquals("SECRET_KEY_NOT_FOUND;\tSecret Key not found in request;\t[]", result.getMessage());
        assertNull(result.getUser());
    }

    public void testMissingMagicNumber() throws Exception
    {
        EncryptedCertificate cert = mock(EncryptedCertificate.class);
        when(cert.getCertificate()).thenReturn("cert");
        when(cert.getID()).thenReturn("appId");
        when(cert.getMagicNumber()).thenReturn(null);
        when(cert.getSecretKey()).thenReturn("dis-is-a-sekrit");
        when(cert.getProtocolVersion()).thenReturn(Integer.valueOf(1));
        
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);

        Authenticator.Result result = authenticator.authenticate(request, new MockResponse());

        assertNotNull(result);
        assertEquals(Authenticator.Result.Status.ERROR, result.getStatus());
        assertEquals("MAGIC_NUMBER_NOT_FOUND;\tMagic Number not found in request;\t[]", result.getMessage());
        assertNull(result.getUser());
    }

    public void testBadCertificate() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode(USER);
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);
        request.addHeader(TrustedApplicationUtils.Header.Request.CERTIFICATE, "bad-cert");

        Authenticator.Result result = authenticator.authenticate(request, new MockResponse());

        assertNotNull(result);
        assertEquals(result.getMessage(), Authenticator.Result.Status.ERROR, result.getStatus());
        assertEquals("SYSTEM;\tException: {0} occurred serving request for application: {1};\t[\"java.lang.NullPointerException: what a bad certificate!\",\"bad-cert\"]", result.getMessage());
        assertNull(result.getUser());
    }

    public void testPrincipalNotFound() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode("unknown-principal");
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);

        Authenticator.Result result = authenticator.authenticate(request, new MockResponse());

        assertNotNull(result);
        assertEquals(result.getMessage(), Authenticator.Result.Status.FAILED, result.getStatus());
        assertEquals("USER_UNKNOWN;\tUnknown User: {0};\t[\"unknown-principal\"]", result.getMessage());
        assertNull(result.getUser());
    }

    public void testPrincipalLoginDenied() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode(USER);
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);

        canLogin[0] = false;
        Authenticator.Result result = authenticator.authenticate(request, new MockResponse());

        assertNotNull(result);
        assertEquals(result.getMessage(), Authenticator.Result.Status.FAILED, result.getStatus());
        assertEquals("PERMISSION_DENIED;\tPermission Denied;\t[]", result.getMessage());
        assertNull(result.getUser());
    }

    public void testBadRequestIp() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode(USER);
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);
        request.addHeader("ip-mismatch", "true");

        Authenticator.Result result = authenticator.authenticate(request, new MockResponse());

        assertNotNull(result);
        assertEquals(result.getMessage(), Authenticator.Result.Status.ERROR, result.getStatus());
        assertEquals("BAD_REMOTE_IP;\tRequest not allowed from IP address: {0};\t[\"i.am.a.teapot\"]", result.getMessage());
        assertNull(result.getUser());
    }

    public void testBadXForwardIp() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode(USER);
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);
        request.addHeader("forward-mismatch", "true");

        Authenticator.Result result = authenticator.authenticate(request, new MockResponse());

        assertNotNull(result);
        assertEquals(Authenticator.Result.Status.ERROR, result.getStatus());
        assertEquals("BAD_XFORWARD_IP;\tRequest not allowed from IP address: {0};\t[\"i.am.a.teapot\"]", result.getMessage());
        assertNull(result.getUser());
    }

    public void testBadRequestUrl() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode(USER);
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1("/jira/secure/DeleteProject.jspa", cert);
        request.addHeader("url-mismatch", "true");

        Authenticator.Result result = authenticator.authenticate(request, new MockResponse());

        assertNotNull(result);
        assertEquals(Authenticator.Result.Status.ERROR, result.getStatus());
        assertEquals("BAD_URL;\tRequest not allowed to access URL: {0};\t[\"/jira/secure/DeleteProject.jspa\"]", result.getMessage());
        assertNull(result.getUser());
    }
}
