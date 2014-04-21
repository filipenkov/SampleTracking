package com.atlassian.crowd.plugin.rest.filter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Abstract implementation for a Basic Authentication filter.
 *
 * @since 2.2
 */
public abstract class AbstractBasicAuthenticationFilter implements Filter
{
    private static final String SERAPH_ALREADY_FILTERED = "os_securityfilter_already_filtered";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBasicAuthenticationFilter.class);

    public void init(final FilterConfig filterConfig) throws ServletException
    {
    }

    /**
     * Returns the Basic Auth credentials.
     *
     * @param request the request
     * @return basic authentication credentials
     */
    protected Credentials getBasicAuthCredentials(final HttpServletRequest request)
    {
        Credentials credentials = null;

        String header = request.getHeader("Authorization");

        if (header != null && header.substring(0, 5).equalsIgnoreCase("Basic"))
        {
            String base64Token = header.substring(6);
            String token = new String(Base64.decodeBase64(base64Token.getBytes()));

            final int delim = token.indexOf(":");

            if (delim != -1)
            {
                String name = token.substring(0, delim);
                String password = token.substring(delim + 1);

                credentials = new Credentials(name, password);
            }
        }

        return credentials;
    }

    /**
     * Consider the request as authenticated, if the entity name in it matches the entity
     * name saved in the {@link javax.servlet.http.HttpSession}.
     *
     * @param request HTTP servlet request possibly containing a HttpSession
     * @param credentials credentials sent with the request
     * @return <tt>true</tt> if the entity has already been authenticated
     */
    protected boolean isAuthenticated(final HttpServletRequest request, final Credentials credentials)
    {
        final String authenticatedEntity = getAuthenticatedEntity(request);
        return !StringUtils.isBlank(authenticatedEntity) && authenticatedEntity.equals(credentials.getName());
    }

    /**
     * Responds to request with a Basic Authentication challenge.
     *
     * @param response the HTTP response
     */
    protected void respondWithChallenge(final HttpServletResponse response) throws IOException
    {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "BASIC realm=\"" + getBasicRealm() + "\"");
        response.setHeader("Content-Type", "text/plain;charset=UTF-8");
        response.getOutputStream().write(getAuthenticationErrorMessage().getBytes("UTF-8"));
        response.flushBuffer();
    }

    public void destroy()
    {
    }

    /**
     * Ensures that Seraph simply forwards the request without processing it.
     *
     * @param request HttpServletRequest
     */
    protected void ensureSeraphForwardsRequest(final ServletRequest request)
    {
        request.setAttribute(SERAPH_ALREADY_FILTERED, Boolean.TRUE);
    }

    protected class Credentials
    {
        private final String name;
        private final String password;

        private Credentials(final String name, final String password)
        {
            this.name = name;
            this.password = password;
        }

        public String getName()
        {
            return name;
        }

        public String getPassword()
        {
            return password;
        }
    }

    /**
     * Returns the authenticated entity from the <code>request</code>, or <tt>null</tt> if there is no authenticated entity.
     *
     * @param request Request
     * @return authenticated entity from the <code>request</code>, or <tt>null</tt> if there is no authenticated entity.
     */
    protected String getAuthenticatedEntity(final HttpServletRequest request)
    {
        Validate.notNull(request);
        final HttpSession session = request.getSession(false);
        if (session == null)
        {
            return null;
        }
        final Object value = session.getAttribute(getEntityAttributeKey());
        if (value == null || !(value instanceof String))
        {
            return null;
        }
        return (String) value;
    }

    /**
     * Sets the authenticated entity.
     *
     * @param request Request
     * @param name the name of the authenticated entity
     */
    protected void setAuthenticatedEntity(final HttpServletRequest request, final String name)
    {
        Validate.notNull(request);
        Validate.notNull(name);
        request.getSession().setAttribute(getEntityAttributeKey(), name);
    }

    /**
     * Returns the HTTP request attribute key for the entity.
     *
     * @return attribute key for the entity
     */
    protected abstract String getEntityAttributeKey();

    /**
     * Returns the authentication error message.
     *
     * @return authentication error message
     */
    protected abstract String getAuthenticationErrorMessage();

    /**
     * Returns the Basic Realm in the WWW-Authenticate header.
     *
     * @return Basic Realm
     */
    protected abstract String getBasicRealm();
}
