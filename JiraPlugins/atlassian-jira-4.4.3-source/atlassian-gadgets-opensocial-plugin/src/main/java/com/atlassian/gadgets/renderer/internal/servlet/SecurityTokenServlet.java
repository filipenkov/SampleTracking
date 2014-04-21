package com.atlassian.gadgets.renderer.internal.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.sal.api.user.UserManager;

import com.google.common.collect.ImmutableMap;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.auth.SecurityTokenDecoder;
import org.apache.shindig.auth.SecurityTokenException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkNotNull;

public class SecurityTokenServlet extends HttpServlet
{
    private final SecurityTokenDecoder decoder;
    private final UserManager userManager;

    public SecurityTokenServlet(@Qualifier("nonExpirableBlobCrypterSecurityTokenDecoder") SecurityTokenDecoder decoder,
                                UserManager userManager)
    {
        this.decoder = checkNotNull(decoder, "decoder");
        this.userManager = checkNotNull(userManager, "userManager");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String user = userManager.getRemoteUsername(request);
        JSONObject updatedTokens = new JSONObject();
        
        int i = 0;
        String stParamKey = "st." + i;
        while (request.getParameter(stParamKey) != null)
        {
            SecurityToken token = decode(request.getParameter(stParamKey), request.getRequestURL().toString());
            if (token == null || !equal(user, token.getViewerId()))
            {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            try
            {
                updatedTokens.put(stParamKey, token.getUpdatedToken());
            }
            catch (JSONException e)
            {
                // Swallow it. It is impossible for the value to be a non-finite number or the key to be null so there
                // will never be an exception thrown.
            }
            stParamKey = "st." + (++i);
        }
        response.setContentType("application/json");
        try
        {
            updatedTokens.write(response.getWriter());
        }
        catch (JSONException e)
        {
            throw new ServletException(e);
        }
    }

    private SecurityToken decode(String securityToken, String activeUrl)
    {
        Map<String, String> tokenParameters = ImmutableMap.of(
            SecurityTokenDecoder.SECURITY_TOKEN_NAME, securityToken,
            SecurityTokenDecoder.ACTIVE_URL_NAME, activeUrl
        );
        try
        {
            return decoder.createToken(tokenParameters);
        }
        catch (SecurityTokenException e)
        {
            return null;
        }
    }
}
