package com.atlassian.gadgets.renderer.internal.servlet;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.sal.api.user.UserManager;

import com.google.common.collect.ImmutableMap;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.auth.SecurityTokenDecoder;
import org.apache.shindig.auth.SecurityTokenException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityTokenServletTest
{
    private static final String REQUEST_URL = "http://localhost/ifr";
    
    @Mock SecurityTokenDecoder decoder;
    @Mock UserManager userManager;
    
    SecurityTokenServlet servlet;
    
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    ByteArrayOutputStream responseOutputStream;
    
    @Before
    public void createSecurityTokenServlet() throws Exception
    {
        when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL));
        
        responseOutputStream = new ByteArrayOutputStream();
        when(response.getWriter()).thenReturn(new PrintWriter(responseOutputStream));
        
        servlet = new SecurityTokenServlet(decoder, userManager);
    }
    
    @Test
    public void assertThatAnInvalidTokenCausesBadRequestResponse() throws Exception
    {
        String securityToken = "st";
        mockParameters(ImmutableMap.of("st.0", securityToken));
        
        when(decoder.createToken(ImmutableMap.of(
            SecurityTokenDecoder.SECURITY_TOKEN_NAME, securityToken, SecurityTokenDecoder.ACTIVE_URL_NAME, REQUEST_URL
        ))).thenThrow(new SecurityTokenException("Bad token"));
        
        servlet.doPost(request, response);
        
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
    
    @Test
    public void assertThatTokenBelogingToADifferentUserCausesBadRequestResponse() throws Exception
    {
        String encodedSecurityToken = "st";
        mockParameters(ImmutableMap.of("st.0", encodedSecurityToken));
        SecurityToken securityToken = securityTokenBeloningTo("fred");

        when(decoder.createToken(ImmutableMap.of(
            SecurityTokenDecoder.SECURITY_TOKEN_NAME, encodedSecurityToken, SecurityTokenDecoder.ACTIVE_URL_NAME, REQUEST_URL
        ))).thenReturn(securityToken);
        when(userManager.getRemoteUsername(request)).thenReturn("barney");
        
        servlet.doPost(request, response);
        
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
    
    @Test
    public void asserThatValidTokensAreUpdated() throws Exception
    {
        String encodedSecurityToken = "st";
        mockParameters(ImmutableMap.of("st.0", encodedSecurityToken));
        SecurityToken securityToken = securityTokenBeloningTo("fred");

        when(decoder.createToken(ImmutableMap.of(
            SecurityTokenDecoder.SECURITY_TOKEN_NAME, encodedSecurityToken, SecurityTokenDecoder.ACTIVE_URL_NAME, REQUEST_URL
        ))).thenReturn(securityToken);
        when(userManager.getRemoteUsername(request)).thenReturn("fred");
        when(securityToken.getUpdatedToken()).thenReturn("st2");
        
        servlet.doPost(request, response);

        response.getWriter().flush();
        JSONObject json = new JSONObject(responseOutputStream.toString());
        assertThat(json.getString("st.0"), is(equalTo("st2")));
    }
    
    private SecurityToken securityTokenBeloningTo(String user)
    {
        SecurityToken st = mock(SecurityToken.class);
        when(st.getViewerId()).thenReturn(user);
        return st;
    }

    private void mockParameters(Map<String, String> params)
    {
        for (Map.Entry<String, String> param : params.entrySet())
        {
            when(request.getParameter(param.getKey())).thenReturn(param.getValue());
        }
        when(request.getParameterMap()).thenReturn(params);
    }
}
