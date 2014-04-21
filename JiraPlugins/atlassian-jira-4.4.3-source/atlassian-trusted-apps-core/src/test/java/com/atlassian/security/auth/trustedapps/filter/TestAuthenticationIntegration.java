package com.atlassian.security.auth.trustedapps.filter;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.security.auth.trustedapps.ApplicationCertificate;
import com.atlassian.security.auth.trustedapps.BouncyCastleEncryptionProvider;
import com.atlassian.security.auth.trustedapps.DefaultTrustedApplication;
import com.atlassian.security.auth.trustedapps.EncryptedCertificate;
import com.atlassian.security.auth.trustedapps.EncryptionProvider;
import com.atlassian.security.auth.trustedapps.RequestConditions;
import com.atlassian.security.auth.trustedapps.TrustedApplication;
import com.atlassian.security.auth.trustedapps.TrustedApplicationUtils;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.security.auth.trustedapps.UserResolver;
import com.atlassian.security.auth.trustedapps.filter.Authenticator.Result;
import com.atlassian.security.auth.trustedapps.request.TrustedRequest;
import com.atlassian.security.auth.trustedapps.request.commonshttpclient.CommonsHttpClientTrustedRequest;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for creating and processing requests. End to end tests, as far as possible,
 * to make it easier to understand the impact of changes to the security protocol.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAuthenticationIntegration
{
    /* Crypto */
    private KeyPair keyPair;
    
    /* Trusted Apps */
    @Mock public TrustedApplicationsManager appManager;
    @Mock public AuthenticationController authenticationController;
    @Mock public TrustedApplication trustedApplication;

    @Before
    public void cryptoAndTrustedAppsMockBehaviour() throws NoSuchAlgorithmException
    {
        keyPair = generateSingleUseKeyPair();
        
        RequestConditions conditions = RequestConditions.builder().build();
        trustedApplication = new DefaultTrustedApplication(keyPair.getPublic(), "appId", conditions);
        
        when(appManager.getTrustedApplication("appId")).thenReturn(trustedApplication);
        ApplicationCertificate cert = mock(ApplicationCertificate.class);
        when(cert.getUserName()).thenReturn("User");
        
        when(authenticationController.canLogin(Mockito.<Principal>anyObject(), Mockito.<HttpServletRequest>anyObject())).thenReturn(true);
    }
    
    public static KeyPair generateSingleUseKeyPair() throws NoSuchAlgorithmException
    {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        return kpg.genKeyPair();
    }
    
    private EncryptedCertificate getEncryptedCertificate(String urlToSign)
    {
        EncryptionProvider provider;
        provider = new BouncyCastleEncryptionProvider();
        EncryptedCertificate certificate = provider.createEncryptedCertificate("user", keyPair.getPrivate(), "appId", urlToSign);
        return certificate;
    }
    
    public HttpServletRequest mockHttpServletRequestWithFieldsFromHttpMethod(HttpMethod method)
        throws URIException
    {
        HttpServletRequest hrq = mock(HttpServletRequest.class);

        final String requestUrl = method.getURI().toString();
        
        when(hrq.getRequestURL()).thenAnswer(new Answer<StringBuffer>()
        {
            public StringBuffer answer(InvocationOnMock invocation) throws Throwable
            {
                return new StringBuffer(requestUrl);
            }
        });
        
        String[] toMock = {
                "ID",
                "Cert",
                "Key",
                "Version",
                "Magic",
                "Signature"
        };
        
        for (String f : toMock)
        {
            String hn = "X-Seraph-Trusted-App-" + f;
            Header header = method.getRequestHeader(hn);
            if (header != null) {
                when(hrq.getHeader(hn)).thenReturn(header.getValue());
            }
        }

        return hrq;
    }
    
    public HttpMethod requestFor(String url) throws Exception
    {
        HttpMethod method = new GetMethod(url);
        TrustedRequest request = new CommonsHttpClientTrustedRequest(method);
        TrustedApplicationUtils.addRequestParameters(getEncryptedCertificate(url), request);
        return method;
    }
    
    @Test
    public void validRequestIsAccepted() throws Exception
    {
        /* Trusted Apps framework classes */
        DummyResolver resolver = new DummyResolver();
        
        HttpMethod method = requestFor("http://www.example.com/");
        
        TrustedApplicationFilterAuthenticator authenticator = new TrustedApplicationFilterAuthenticator(appManager, resolver, authenticationController);

        HttpServletRequest hrq = mockHttpServletRequestWithFieldsFromHttpMethod(method);
        
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        Result result = authenticator.authenticate(hrq, response);
        assertEquals("success", ((Object) result.getStatus()).toString());
        assertEquals("Trusted Apps enlowercases usernames", "appId/user", result.getUser().getName());
    }
    
    @Test
    public void errorWhenNoTrustedAppsHeadersArePresent()
    {
        UserResolver resolver = null;
        TrustedApplicationFilterAuthenticator authenticator = new TrustedApplicationFilterAuthenticator(appManager, resolver , authenticationController);
        
        HttpServletRequest hrq = mock(HttpServletRequest.class);
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        Result result = authenticator.authenticate(hrq, response);
        assertEquals("no attempt", ((Object) result.getStatus()).toString());
    }
    
    @Test
    public void errorWhenKeyIsModified() throws Exception
    {
        HttpMethod method = requestFor("http://www.example.com/");
        
        method.setRequestHeader("X-Seraph-Trusted-App-Key", "XXXX");
        
        TrustedApplicationFilterAuthenticator authenticator = new TrustedApplicationFilterAuthenticator(appManager, null, authenticationController);

        HttpServletRequest hrq = mockHttpServletRequestWithFieldsFromHttpMethod(method);
        
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        Result result = authenticator.authenticate(hrq, response);
        assertEquals("error", ((Object) result.getStatus()).toString());
    }
    
    @Test
    public void errorWhenUserIsUnknown() throws Exception
    {
        UserResolver resolver = mock(UserResolver.class);

        HttpMethod method = requestFor("http://www.example.com/");
        
        TrustedApplicationFilterAuthenticator authenticator = new TrustedApplicationFilterAuthenticator(appManager, resolver, authenticationController);

        HttpServletRequest hrq = mockHttpServletRequestWithFieldsFromHttpMethod(method);
        
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        Result result = authenticator.authenticate(hrq, response);
        assertEquals("failed", ((Object) result.getStatus()).toString());
    }
    
    @Test
    public void noSignedUrlWhenSignatureIsNotProvided() throws Exception
    {
        /* Trusted Apps framework classes */
        DummyResolver resolver = new DummyResolver();
        
        HttpMethod method = requestFor("http://www.example.com/");
        method.setRequestHeader("X-Seraph-Trusted-App-Version", "1");
        method.removeRequestHeader("X-Seraph-Trusted-App-Signature");
        
        TrustedApplicationFilterAuthenticator authenticator = new TrustedApplicationFilterAuthenticator(appManager, resolver, authenticationController);

        HttpServletRequest hrq = mockHttpServletRequestWithFieldsFromHttpMethod(method);
        
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        Result result = authenticator.authenticate(hrq, response);
        assertEquals("success", ((Object) result.getStatus()).toString());
        assertNull("No signed URL as no signature provided", ((Result.Success) result).getSignedUrl());
    }
    
    @Test
    public void errorWhenSignatureIsInvalid() throws Exception
    {
        /* Trusted Apps framework classes */
        DummyResolver resolver = new DummyResolver();
        
        HttpMethod method = requestFor("http://www.example.com/");
        
        TrustedApplicationFilterAuthenticator authenticator =
            new TrustedApplicationFilterAuthenticator(appManager, resolver, authenticationController);

        method.setRequestHeader("X-Seraph-Trusted-App-Signature", "XXXX");
        HttpServletRequest hrq = mockHttpServletRequestWithFieldsFromHttpMethod(method);
        
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        Result result = authenticator.authenticate(hrq, response);
        assertEquals("error", ((Object) result.getStatus()).toString());
    }
    
    @Test
    public void signedUrlIncludedInResultWhenSignatureIsValid() throws Exception
    {
        /* Trusted Apps framework classes */
        DummyResolver resolver = new DummyResolver();
        
        HttpMethod method = requestFor("http://www.example.com/");
        
        TrustedApplicationFilterAuthenticator authenticator =
            new TrustedApplicationFilterAuthenticator(appManager, resolver, authenticationController);

        HttpServletRequest hrq = mockHttpServletRequestWithFieldsFromHttpMethod(method);
        assertNotNull(method.getRequestHeader("X-Seraph-Trusted-App-Signature"));
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        Result result = authenticator.authenticate(hrq, response);
        assertEquals("success", ((Object) result.getStatus()).toString());
        assertEquals("http://www.example.com/", ((Result.Success) result).getSignedUrl());
    }
    
    @Test
    public void signingADifferentUrlCausesAnError() throws Exception
    {
        /* Trusted Apps framework classes */
        DummyResolver resolver = new DummyResolver();
        
        HttpMethod method = requestFor("http://www.example.com/");
        
        TrustedApplicationFilterAuthenticator authenticator =
            new TrustedApplicationFilterAuthenticator(appManager, resolver, authenticationController);

        HttpServletRequest hrq = mockHttpServletRequestWithFieldsFromHttpMethod(method);
        when(hrq.getRequestURL()).thenAnswer(new Answer<StringBuffer>(){
           public StringBuffer answer(InvocationOnMock invocation) throws Throwable
            {
               return new StringBuffer("http://unexpected.hostname.invalid/");
            } 
        });
        assertNotNull(method.getRequestHeader("X-Seraph-Trusted-App-Signature"));
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        Result result = authenticator.authenticate(hrq, response);
        assertEquals("error", ((Object) result.getStatus()).toString());
    }
    
    @Test
    public void v2RequestWithoutSignatureIsAnError() throws Exception
    {
        DummyResolver resolver = new DummyResolver();
        
        HttpMethod method = requestFor("http://www.example.com/");
        assertEquals("2", method.getRequestHeader("X-Seraph-Trusted-App-Version").getValue());
        method.removeRequestHeader("X-Seraph-Trusted-App-Signature");

        TrustedApplicationFilterAuthenticator authenticator =
            new TrustedApplicationFilterAuthenticator(appManager, resolver, authenticationController);

        HttpServletRequest hrq = mockHttpServletRequestWithFieldsFromHttpMethod(method);
        
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Result result = authenticator.authenticate(hrq, response);
        assertEquals("error", ((Object) result.getStatus()).toString());
    }
    
    static class DummyResolver implements UserResolver
    {
        public Principal resolve(ApplicationCertificate certificate)
        {
            Principal p = mock(Principal.class);
            when(p.getName()).thenReturn(certificate.getApplicationID() + "/" + certificate.getUserName());
            return p;
        }
    }
}
