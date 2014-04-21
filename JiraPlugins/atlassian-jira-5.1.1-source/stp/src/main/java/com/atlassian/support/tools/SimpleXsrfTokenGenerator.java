package com.atlassian.support.tools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.atlassian.security.random.DefaultSecureTokenGenerator;
import com.atlassian.xwork.interceptors.XsrfTokenInterceptor;

/* copied from com.atlassian.xwork.SimpleXSRFTokenGenerator */

/**
 * Simple implementation of XsrfTokenGenerator that stores a unique value in the session. The session ID
 * itself isn't used because we don't want to risk compromising the entire session in case we don't protect
 * the XSRF token diligently enough.
 *
 * <p>Tokens are chosen to be reasonably unique (60 bits) with reasonably short representations (base64 encoded).
 */
public class SimpleXsrfTokenGenerator 
{
    public static final String TOKEN_SESSION_KEY = "atlassian.xsrf.token";

    public String generateToken(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        String token = (String) session.getAttribute(TOKEN_SESSION_KEY);

        if (token == null)
        {
            token = createToken();
            session.setAttribute(TOKEN_SESSION_KEY, token);
        }

        return token;
    }
    
    public String getXsrfTokenName()
    {
        return XsrfTokenInterceptor.REQUEST_PARAM_NAME;
    }

    public boolean validateToken(HttpServletRequest request, String token)
    {
        return token != null && token.equals(request.getSession(true).getAttribute(TOKEN_SESSION_KEY));
    }

    private String createToken()
    {
        return DefaultSecureTokenGenerator.getInstance().generateToken();
    }
}

