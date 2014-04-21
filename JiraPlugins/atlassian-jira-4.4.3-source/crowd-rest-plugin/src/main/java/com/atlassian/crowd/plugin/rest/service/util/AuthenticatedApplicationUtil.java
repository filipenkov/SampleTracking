package com.atlassian.crowd.plugin.rest.service.util;

import org.apache.commons.lang.Validate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Utility for setting and retrieving the application name from the {@link HttpServletRequest}.
 *
 * @since v2.1
 */
public class AuthenticatedApplicationUtil
{
    public static final String APPLICATION_ATTRIBUTE_KEY = "com.atlassian.crowd.authenticated.application.name";

    private AuthenticatedApplicationUtil() {}

    /**
     * Returns the application name from the {@link HttpSession}, or <tt>null</tt> if no application name was found.
     * 
     * @param request HTTP servlet request
     * @return name of the authenticated application, <tt>null</tt> if no application name was found
     */
    public static String getAuthenticatedApplication(final HttpServletRequest request)
    {
        Validate.notNull(request);
        final HttpSession session = request.getSession(false);
        if (session == null)
        {
            return null;
        }
        final Object value = session.getAttribute(APPLICATION_ATTRIBUTE_KEY);
        return (value instanceof String ? (String) value : null);
    }

    /**
     * Sets the name of the authenticated application as an attribute of the {@link HttpSession}.
     *
     * @param request HTTP servlet request
     * @param applicationName name of the authenticated application
     */
    public static void setAuthenticatedApplication(final HttpServletRequest request, final String applicationName)
    {
        Validate.notNull(request);
        Validate.notNull(applicationName);
        request.getSession().setAttribute(APPLICATION_ATTRIBUTE_KEY, applicationName);
    }
}
