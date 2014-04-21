package com.atlassian.jira.security.xsrf;


import com.atlassian.jira.bc.license.JiraServerIdProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.security.random.DefaultSecureTokenGenerator;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.StringTokenizer;

/**
 * Simple implementation of XsrfTokenGenerator that stores a unique value in a cookie.
 *
 * @since v4.0
 */
public class SimpleXsrfTokenGenerator implements XsrfTokenGenerator
{
    private static final String SET_COOKIE_PENDING = "jira.xsrf.set.cookie.pending";
    private static final String LOGGED_IN = "lin";
    private static final String LOGGED_OUT = "lout";
    private final JiraAuthenticationContext authenticationContext;
    private final JiraServerIdProvider jiraServerIdProvider;

    public SimpleXsrfTokenGenerator(final JiraAuthenticationContext authenticationContext, final JiraServerIdProvider jiraServerIdProvider)
    {
        this.authenticationContext = authenticationContext;
        this.jiraServerIdProvider = jiraServerIdProvider;
    }

    public String generateToken(HttpServletRequest httpServletRequest)
    {
        return generateTokenImpl(httpServletRequest);
    }

    // we now use cookies instead of sessions and hence this velocity parameter is not needed
    // but left in for backwards compatibility reasons

    public String generateToken(final VelocityRequestContext request)
    {
        return generateTokenImpl(null);
    }

    public String generateToken()
    {
        return generateTokenImpl(null);
    }

    public String getXsrfTokenName()
    {
        return TOKEN_HTTP_SESSION_KEY;
    }

    public boolean validateToken(HttpServletRequest httpServletRequest, String token)
    {
        if (token != null && httpServletRequest != null)
        {
            final String pendingToken = getPendingToken(httpServletRequest);
            final String storedToken = getXsrfCookie(httpServletRequest, pendingToken);
            return token.equals(storedToken);
        }
        return false;
    }

    public boolean generatedByAuthenticatedUser(String token)
    {
        return StringUtils.isNotBlank(token) && token.endsWith("|" + LOGGED_IN);
    }

    private String generateTokenImpl(final HttpServletRequest httpServletRequest)
    {
        String token = null;
        HttpServletRequest safeHttpServletRequest = safeAccess(httpServletRequest);
        if (safeHttpServletRequest != null)
        {
            final boolean thereIsAnAuthenticatedUser = authenticationContext.getLoggedInUser() != null;
            final String pendingToken = getPendingToken(safeHttpServletRequest);

            token = getXsrfCookie(safeHttpServletRequest, pendingToken);

            if (StringUtils.isBlank(pendingToken) && !isValidServerSideToken(token, thereIsAnAuthenticatedUser))
            {
                return setXsrfCookie(safeHttpServletRequest, createToken(thereIsAnAuthenticatedUser));
            }
        }
        return token;
    }

    /**
     * This checks that the token is in a state that is fit for this JIRA.
     *
     * @param token the token in question
     * @param thereIsAnAuthenticatedUser true if there is a user in play right now
     * @return true if the token is ok to be used.
     */
    private boolean isValidServerSideToken(final String token, final boolean thereIsAnAuthenticatedUser)
    {
        if (token != null)
        {
            if (!isOurServerId(token))
            {
                return false;
            }
            // was it generated for a logged in user and hence can be used
            if (generatedByAuthenticatedUser(token))
            {
                return thereIsAnAuthenticatedUser; // not anonymous
            }
            else
            {
                return !thereIsAnAuthenticatedUser; // anonymous
            }
        }
        return false;
    }

    @Override
    public String getToken(HttpServletRequest request)
    {
        final Cookie[] cookies = request.getCookies();
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (TOKEN_HTTP_SESSION_KEY.equalsIgnoreCase(cookie.getName()))
                {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String getXsrfCookie(final HttpServletRequest httpServletRequest, final String pendingToken)
    {
        if (StringUtils.isNotBlank(pendingToken))
        {
            return pendingToken;
        }
        return getToken(httpServletRequest);
    }

    /**
     * Why might you ask that I am taking not taking in the HttpServletResponse?  Because of backward API
     * compatibility.
     * <p/>
     * The original API did not take a HttpServletResponse and now cant.
     *
     * @param httpServletRequest the request in play
     * @param token the xsrf value to store in a cookie
     */
    private String setXsrfCookie(final HttpServletRequest httpServletRequest, final String token)
    {
        final HttpServletResponse httpServletResponse = ExecutingHttpRequest.getResponse();
        // this is possible in a velocity context such as mail
        if (httpServletResponse != null)
        {
            addNewCookie(httpServletRequest, token, httpServletResponse);
        }
        return token;
    }

    private void addNewCookie(HttpServletRequest httpServletRequest, String token, HttpServletResponse httpServletResponse)
    {
        final Cookie cookie = new Cookie(TOKEN_HTTP_SESSION_KEY, token);
        cookie.setPath(getRequestContext(httpServletRequest));
        cookie.setMaxAge(-1);  // expire with the browser exit
        httpServletResponse.addCookie(cookie);
        httpServletRequest.setAttribute(SET_COOKIE_PENDING, token);
    }

    private String getRequestContext(HttpServletRequest httpServletRequest)
    {
        String contextPath = httpServletRequest.getContextPath();
        return StringUtils.isBlank(contextPath) ? "/" : contextPath;
    }

    private String getPendingToken(HttpServletRequest httpServletRequest)
    {
        return (String) httpServletRequest.getAttribute(SET_COOKIE_PENDING);
    }

    private boolean isOurServerId(String token)
    {
        StringTokenizer st = new StringTokenizer(token, "|");
        return StringUtils.defaultString(jiraServerIdProvider.getServerId()).equals(st.nextToken());
    }

    private String createToken(final boolean thereIsAnAuthenticatedUser)
    {
        // make it JIRA instance specific
        final String serverId = jiraServerIdProvider.getServerId();
        // synchronisation is now handled at a lower level
        final String crytoPart = DefaultSecureTokenGenerator.getInstance().generateToken();
        return serverId + "|" + crytoPart + "|" + (thereIsAnAuthenticatedUser ? LOGGED_IN : LOGGED_OUT);
    }

    private HttpServletRequest safeAccess(final HttpServletRequest httpServletRequest)
    {
        return httpServletRequest != null ? httpServletRequest : ExecutingHttpRequest.get();
    }
}
