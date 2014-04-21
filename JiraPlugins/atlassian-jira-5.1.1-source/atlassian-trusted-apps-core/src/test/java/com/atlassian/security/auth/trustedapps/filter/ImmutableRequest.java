package com.atlassian.security.auth.trustedapps.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ImmutableRequest
{
    static HttpServletRequest wrap(final HttpServletRequest delegate)
    {
        return new HttpServletRequest()
        {
            final HttpServletRequest request = delegate;

            public Object getAttribute(final String arg0)
            {
                return request.getAttribute(arg0);
            }

            @SuppressWarnings("unchecked")
            public Enumeration getAttributeNames()
            {
                return request.getAttributeNames();
            }

            public String getAuthType()
            {
                return request.getAuthType();
            }

            public String getCharacterEncoding()
            {
                return request.getCharacterEncoding();
            }

            public int getContentLength()
            {
                return request.getContentLength();
            }

            public String getContentType()
            {
                return request.getContentType();
            }

            public String getContextPath()
            {
                return request.getContextPath();
            }

            public Cookie[] getCookies()
            {
                return request.getCookies();
            }

            public long getDateHeader(final String arg0)
            {
                return request.getDateHeader(arg0);
            }

            public String getHeader(final String arg0)
            {
                return request.getHeader(arg0);
            }

            @SuppressWarnings("unchecked")
            public Enumeration getHeaderNames()
            {
                return request.getHeaderNames();
            }

            @SuppressWarnings("unchecked")
            public Enumeration getHeaders(final String arg0)
            {
                return request.getHeaders(arg0);
            }

            public ServletInputStream getInputStream() throws IOException
            {
                return request.getInputStream();
            }

            public int getIntHeader(final String arg0)
            {
                return request.getIntHeader(arg0);
            }

            public Locale getLocale()
            {
                return request.getLocale();
            }

            @SuppressWarnings("unchecked")
            public Enumeration getLocales()
            {
                return request.getLocales();
            }

            public String getMethod()
            {
                return request.getMethod();
            }

            public String getParameter(final String arg0)
            {
                return request.getParameter(arg0);
            }

            @SuppressWarnings("unchecked")
            public Map getParameterMap()
            {
                return request.getParameterMap();
            }

            @SuppressWarnings("unchecked")
            public Enumeration getParameterNames()
            {
                return request.getParameterNames();
            }

            public String[] getParameterValues(final String arg0)
            {
                return request.getParameterValues(arg0);
            }

            public String getPathInfo()
            {
                return request.getPathInfo();
            }

            public String getPathTranslated()
            {
                return request.getPathTranslated();
            }

            public String getProtocol()
            {
                return request.getProtocol();
            }

            public String getQueryString()
            {
                return request.getQueryString();
            }

            public BufferedReader getReader() throws IOException
            {
                return request.getReader();
            }

            @SuppressWarnings("deprecation")
            public String getRealPath(final String arg0)
            {
                return request.getRealPath(arg0);
            }

            public String getRemoteAddr()
            {
                return request.getRemoteAddr();
            }

            public String getRemoteHost()
            {
                return request.getRemoteHost();
            }

            public String getRemoteUser()
            {
                return request.getRemoteUser();
            }

            public RequestDispatcher getRequestDispatcher(final String arg0)
            {
                return request.getRequestDispatcher(arg0);
            }

            public String getRequestedSessionId()
            {
                return request.getRequestedSessionId();
            }

            public String getRequestURI()
            {
                return request.getRequestURI();
            }

            public StringBuffer getRequestURL()
            {
                return request.getRequestURL();
            }

            public String getScheme()
            {
                return request.getScheme();
            }

            public String getServerName()
            {
                return request.getServerName();
            }

            public int getServerPort()
            {
                return request.getServerPort();
            }

            public String getServletPath()
            {
                return request.getServletPath();
            }

            public HttpSession getSession()
            {
                return request.getSession();
            }

            public HttpSession getSession(final boolean arg0)
            {
                return request.getSession(arg0);
            }

            public Principal getUserPrincipal()
            {
                return request.getUserPrincipal();
            }

            public boolean isRequestedSessionIdFromCookie()
            {
                return request.isRequestedSessionIdFromCookie();
            }

            @SuppressWarnings("deprecation")
            public boolean isRequestedSessionIdFromUrl()
            {
                return request.isRequestedSessionIdFromUrl();
            }

            public boolean isRequestedSessionIdFromURL()
            {
                return request.isRequestedSessionIdFromURL();
            }

            public boolean isRequestedSessionIdValid()
            {
                return request.isRequestedSessionIdValid();
            }

            public boolean isSecure()
            {
                return request.isSecure();
            }

            public boolean isUserInRole(final String arg0)
            {
                return request.isUserInRole(arg0);
            }

            public void removeAttribute(final String arg0)
            {
                throw new UnsupportedOperationException();
            }

            public void setAttribute(final String arg0, final Object arg1)
            {
                throw new UnsupportedOperationException();
            }

            public void setCharacterEncoding(final String arg0) throws UnsupportedEncodingException
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}