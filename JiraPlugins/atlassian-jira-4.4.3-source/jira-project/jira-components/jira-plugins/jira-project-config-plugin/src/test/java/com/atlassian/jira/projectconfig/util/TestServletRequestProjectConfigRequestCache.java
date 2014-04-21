package com.atlassian.jira.projectconfig.util;

import com.atlassian.jira.project.MockProject;
import com.google.common.collect.Maps;
import org.junit.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertSame;

/**
 * @since v4.4
 */
public class TestServletRequestProjectConfigRequestCache
{
    @Test(expected = IllegalStateException.class)
    public void testProjectNotConfigured() throws Exception
    {
        ServletRequestProjectConfigRequestCache cache = getCache();
        cache.getProject();
    }

    @Test
    public void testProjectConfigured() throws Exception
    {
        MockProject project = new MockProject(67L);
        ServletRequestProjectConfigRequestCache cache = getCache();
        cache.setProject(project);
        assertSame(project, cache.getProject());
        assertSame(project, cache.getProject());
    }

    private ServletRequestProjectConfigRequestCache getCache()
    {
        final HttpServletRequest request = getRequest();
        return new ServletRequestProjectConfigRequestCache()
        {
            @Override
            HttpServletRequest getRequest()
            {
                return request;
            }
        };
    }

    private static HttpServletRequest getRequest()
    {
        final Map<String, Object> attributes = Maps.newHashMap();
        return new HttpServletRequest()
        {
            @Override
            public String getAuthType()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public Cookie[] getCookies()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public long getDateHeader(String name)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getHeader(String name)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public Enumeration getHeaders(String name)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public Enumeration getHeaderNames()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getIntHeader(String name)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getMethod()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getPathInfo()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getPathTranslated()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getContextPath()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getQueryString()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getRemoteUser()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isUserInRole(String role)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public Principal getUserPrincipal()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getRequestedSessionId()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getRequestURI()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public StringBuffer getRequestURL()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getServletPath()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public HttpSession getSession(boolean create)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public HttpSession getSession()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isRequestedSessionIdValid()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isRequestedSessionIdFromCookie()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isRequestedSessionIdFromURL()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isRequestedSessionIdFromUrl()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object getAttribute(String name)
            {
                return attributes.get(name);
            }

            @Override
            public Enumeration getAttributeNames()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getCharacterEncoding()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setCharacterEncoding(String env) throws UnsupportedEncodingException
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getContentLength()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getContentType()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public ServletInputStream getInputStream() throws IOException
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getParameter(String name)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public Enumeration getParameterNames()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String[] getParameterValues(String name)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map getParameterMap()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getProtocol()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getScheme()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getServerName()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getServerPort()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public BufferedReader getReader() throws IOException
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getRemoteAddr()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getRemoteHost()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setAttribute(String name, Object o)
            {
                attributes.put(name, o);
            }

            @Override
            public void removeAttribute(String name)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public Locale getLocale()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public Enumeration getLocales()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isSecure()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public RequestDispatcher getRequestDispatcher(String path)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getRealPath(String path)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getRemotePort()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getLocalName()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getLocalAddr()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getLocalPort()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}
