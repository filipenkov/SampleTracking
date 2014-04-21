package com.atlassian.crowd.integration.http;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.crowd.exception.InvalidTokenException;
import com.atlassian.crowd.integration.http.util.CrowdHttpTokenHelper;
import com.atlassian.crowd.integration.http.util.CrowdHttpValidationFactorExtractor;
import com.atlassian.crowd.model.authentication.ValidationFactor;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.CrowdClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrowdHttpAuthenticatorImplTest
{
    private CrowdHttpAuthenticatorImpl crowdHttpAuthenticatorImpl;

    @Mock private CrowdClient client;
    @Mock private ClientProperties clientProperties;
    @Mock private CrowdHttpTokenHelper tokenHelper;
    @Mock private CrowdHttpValidationFactorExtractor crowdHttpValidationFactorExtractor;
    @Mock private ValidationFactor validationFactor;
    private List<ValidationFactor> validationFactors = Arrays.asList(validationFactor);

    private MockHttpServletRequest httpServletRequest;
    private MockHttpServletResponse httpServletResponse;

    @Before
    public void setUp()
    {
        httpServletRequest = new MockHttpServletRequest();
        httpServletResponse = new MockHttpServletResponse();
        crowdHttpAuthenticatorImpl = new CrowdHttpAuthenticatorImpl(client, clientProperties, tokenHelper);
    }

    @Test
    public void testIsAuthenticatedDoesNotCreateSessionOnUnauthenticatedRequestWithNoSessionAndNoToken() throws Exception
    {
        boolean isAuthenticated = crowdHttpAuthenticatorImpl.isAuthenticated(httpServletRequest, httpServletResponse);
        assertFalse(isAuthenticated);
        assertNull(httpServletRequest.getSession(false));
    }

    @Test
    public void testIsAuthenticatedDoesNotCreateSessionOnUnauthenticatedRequestWithNoSessionAndInvalidToken() throws Exception
    {
        when(tokenHelper.getCrowdToken(any(HttpServletRequest.class), anyString())).thenReturn("someToken");
        when(tokenHelper.getValidationFactorExtractor()).thenReturn(crowdHttpValidationFactorExtractor);
        when(crowdHttpValidationFactorExtractor.getValidationFactors(httpServletRequest)).thenReturn(validationFactors);
        doThrow(new InvalidTokenException()).when(client).validateSSOAuthentication("someToken", validationFactors);

        boolean isAuthenticated = crowdHttpAuthenticatorImpl.isAuthenticated(httpServletRequest, httpServletResponse);
        assertFalse(isAuthenticated);
        assertNull(httpServletRequest.getSession(false));
    }

    @Test
    public void tokenHelperIsUsedToSetTokenPropertiesWhenTokenIsValid() throws Exception
    {
        when(tokenHelper.getCrowdToken(any(HttpServletRequest.class), anyString())).thenReturn("someToken");
        when(tokenHelper.getValidationFactorExtractor()).thenReturn(crowdHttpValidationFactorExtractor);

        assertTrue(crowdHttpAuthenticatorImpl.isAuthenticated(httpServletRequest, httpServletResponse));
        verify(tokenHelper).setCrowdToken(httpServletRequest, httpServletResponse, "someToken", clientProperties, client.getCookieConfiguration());
    }
}
