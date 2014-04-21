package com.atlassian.security.auth.trustedapps.filter;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.security.auth.trustedapps.ApplicationCertificate;
import com.atlassian.security.auth.trustedapps.BouncyCastleEncryptionProvider;
import com.atlassian.security.auth.trustedapps.CurrentApplication;
import com.atlassian.security.auth.trustedapps.DefaultCurrentApplication;
import com.atlassian.security.auth.trustedapps.DefaultTrustedApplication;
import com.atlassian.security.auth.trustedapps.EncryptedCertificate;
import com.atlassian.security.auth.trustedapps.EncryptionProvider;
import com.atlassian.security.auth.trustedapps.InvalidCertificateException;
import com.atlassian.security.auth.trustedapps.InvalidRemoteAddressException;
import com.atlassian.security.auth.trustedapps.InvalidRequestException;
import com.atlassian.security.auth.trustedapps.InvalidRequestUrlException;
import com.atlassian.security.auth.trustedapps.InvalidXForwardedForAddressException;
import com.atlassian.security.auth.trustedapps.RequestConditions;
import com.atlassian.security.auth.trustedapps.TrustedApplication;
import com.atlassian.security.auth.trustedapps.TrustedApplicationUtils;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.security.auth.trustedapps.UserResolver;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This test is really an integration test as it tests the production behaviour of the authenticate method
 */
public class TestTrustedApplicationsFilterAuthenticate extends TestCase
{
    // -----------------------------------------------------------------------------------------------------------------
    // members
    // -----------------------------------------------------------------------------------------------------------------

    private final EncryptionProvider provider = new BouncyCastleEncryptionProvider();
    private final TrustedApplicationsManager manager = new TrustedApplicationsManager()
    {
        final KeyPair pair;
        final CurrentApplication me;

        {
            try
            {
                pair = provider.generateNewKeyPair();
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new RuntimeException(e);
            }
            catch (NoSuchProviderException e)
            {
                throw new RuntimeException(e);
            }
            me = new DefaultCurrentApplication(pair.getPublic(), pair.getPrivate(), "me");
        }

        public CurrentApplication getCurrentApplication()
        {
            return me;
        }

        public TrustedApplication getTrustedApplication(String id)
        {
            if (id.equals("me"))
            {
                return new DefaultTrustedApplication(provider, pair.getPublic(), "me", RequestConditions.builder().setCertificateTimeout(1000L).build())
                {
                    @Override
                    protected void checkRequest(HttpServletRequest request) throws InvalidCertificateException
                    {
                        try
                        {
                            if (request.getHeader("ip-mismatch") != null)
                            {
                                throw new InvalidRemoteAddressException(request.getRemoteAddr());
                            }
                            if (request.getHeader("forward-mismatch") != null)
                            {
                                throw new InvalidXForwardedForAddressException(request.getRemoteAddr());
                            }
                            if (request.getHeader("url-mismatch") != null)
                            {
                                throw new InvalidRequestUrlException(request.getPathInfo());
                            }
                        }
                        catch (final InvalidRequestException e)
                        {
                            throw new InvalidCertificateException(e);
                        }
                    }
                };
            }
            return null;
        }
    };

    private Principal principal = null;
    private final UserResolver userResolver = new UserResolver()
    {
        public Principal resolve(ApplicationCertificate certificate)
        {
            return principal;
        }
    };
    private final TrustedApplicationsFilter filter = new TrustedApplicationsFilter(manager, userResolver, new AuthenticationController()
    {
        public boolean canLogin(Principal user, HttpServletRequest request)
        {
            return true;
        }

        public boolean shouldAttemptAuthentication(HttpServletRequest request)
        {
            return true;
        }

    }, mock(AuthenticationListener.class));

    protected void setUp() throws Exception
    {
        principal = null;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // tests
    // -----------------------------------------------------------------------------------------------------------------

    public void testNoTrustedAppAttempt() throws Exception
    {
        MockRequest request = new MockRequest("/some/path");
        MockResponse response = new MockResponse();
        assertFalse(filter.authenticate(request, response));
        // TODO: add assertion about listener
        final String error = (String) response.getHeaders().get(TrustedApplicationUtils.Header.Response.ERROR);
        assertNull(error, error);
    }

    public void testKnownAppProtocolVersion0() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        principal = new Principal()
        {
            public String getName()
            {
                return "blah-de-blah-blah";
            }
        };
        EncryptedCertificate cert = me.encode("blah-de-blah-blah");
        MockRequest request = new MockTrustedAppRequestV0(cert);
        MockResponse response = new MockResponse();
        assertTrue(filter.authenticate(request, response));
        // TODO: add assertion about listener
        final String error = (String) response.getHeaders().get(TrustedApplicationUtils.Header.Response.ERROR);
        assertNull(error, error);
    }

    public void testKnownAppProtocolVersion1() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        principal = new Principal()
        {
            public String getName()
            {
                return "blah-de-blah-blah";
            }
        };
        EncryptedCertificate cert = me.encode("blah-de-blah-blah");
        MockRequest request = new MockTrustedAppRequestV1(cert);

        MockResponse response = new MockResponse();
        assertTrue(filter.authenticate(request, response));
        // TODO: add assertion about listener
        final String error = (String) response.getHeaders().get(TrustedApplicationUtils.Header.Response.ERROR);
        assertNull(error, error);
    }

    public void testUnknownApp() throws Exception
    {
        EncryptedCertificate cert = mock(EncryptedCertificate.class);
        when(cert.getCertificate()).thenReturn("cert");
        when(cert.getID()).thenReturn("appId");
        when(cert.getMagicNumber()).thenReturn("majick");
        when(cert.getSecretKey()).thenReturn("dis-is-a-sekrit");
        
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);
        assertFailed("Unknown Application:", request);
    }

    public void testBadSecretKey() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode("blad-de-blah-blah");
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);
        request.addHeader(TrustedApplicationUtils.Header.Request.SECRET_KEY, "0123981237123827234842374");
        assertError("BAD_MAGIC;\tUnable to decrypt certificate {0} for application {1};\t[\"secret key\",\"me\"]", request);
    }

    public void testBadCertificate() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode("blad-de-blah-blah");
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);
        request.addHeader(TrustedApplicationUtils.Header.Request.CERTIFICATE, "0123981237123827234842374");
        assertError("BAD_MAGIC;\tUnable to decrypt certificate {0} for application {1};\t[\"secret key\",\"me\"]", request);
    }

    public void testBadPublicKey() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode("blad-de-blah-blah");
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);
        request.addHeader(TrustedApplicationUtils.Header.Request.MAGIC, "123798211233723187217");
        assertError("BAD_MAGIC;\tUnable to decrypt certificate {0} for application {1};\t[\"public key\",\"me\"]", request);
    }

    public void testBadRequestIp() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode("blad-de-blah-blah");
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);
        request.addHeader("ip-mismatch", "true");
        assertError("BAD_REMOTE_IP;\tRequest not allowed from IP address: {0};\t[\"i.am.a.teapot\"]", request);
    }

    public void testBadXForwardIp() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode("blad-de-blah-blah");
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1(cert);
        request.addHeader("forward-mismatch", "true");
        assertError("BAD_XFORWARD_IP;\tRequest not allowed from IP address: {0};\t[\"i.am.a.teapot\"]", request);
    }

    public void testBadRequestUrl() throws Exception
    {
        CurrentApplication me = manager.getCurrentApplication();
        EncryptedCertificate cert = me.encode("blad-de-blah-blah");
        MockTrustedAppRequestV1 request = new MockTrustedAppRequestV1("/jira/secure/DeleteProject.jspa", cert);
        request.addHeader("url-mismatch", "true");
        assertError("BAD_URL;\tRequest not allowed to access URL: {0};\t[\"/jira/secure/DeleteProject.jspa\"]", request);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------------------------------------------------

    private void assertFailed(String msg, MockRequest request)
    {
        MockResponse response = new MockResponse();

        assertFalse(filter.authenticate(request, response));
        // TODO: add assertion about listener
        final String error = (String) response.getHeaders().get(TrustedApplicationUtils.Header.Response.ERROR);
        System.out.println(error);
        assertNotNull(error);
        assertTrue("Expected: '" + msg + "' got:" + error, error.indexOf(msg) >= 0);
    }

    private void assertError(String msg, MockRequest request)
    {
        MockResponse response = new MockResponse();

        assertFalse(filter.authenticate(request, response));
        // TODO: add assertion about listener       
        final String error = (String) response.getHeaders().get(TrustedApplicationUtils.Header.Response.ERROR);
        System.out.println(error);
        assertNotNull(error);
        assertEquals(msg, error);
        //assertTrue("Expected: '" + msg + "' got:" + error, error.indexOf(msg) >= 0);
    }
}
