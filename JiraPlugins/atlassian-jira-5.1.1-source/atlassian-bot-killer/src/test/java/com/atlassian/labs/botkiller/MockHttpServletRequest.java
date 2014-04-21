package com.atlassian.labs.botkiller;

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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 */
public class MockHttpServletRequest implements HttpServletRequest
{
    private final Map<String, Object> attributes = new HashMap<String, Object>();
    private MockHttpSession mockHttpSession;
    private String remoteUser;

    @Override
    public HttpSession getSession(boolean create)
    {
        if (mockHttpSession == null && create)
        {
            mockHttpSession = new MockHttpSession();
        }
        return mockHttpSession;
    }

    @Override
    public HttpSession getSession()
    {
        if (mockHttpSession == null)
        {
            mockHttpSession = new MockHttpSession();
        }
        return mockHttpSession;
    }

    @Override
    public String getRemoteUser()
    {
        return remoteUser;
    }
    
    public void setRemoteUser(String remoteUser)
    {
        this.remoteUser = remoteUser;
    }

    @Override
    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object o)
    {
        attributes.put(name, o);
    }

    @Override
    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }

    @Override
    public Enumeration getAttributeNames()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getAuthType()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Cookie[] getCookies()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long getDateHeader(String name)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getHeader(String name)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Enumeration getHeaders(String name)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Enumeration getHeaderNames()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getIntHeader(String name)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getMethod()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getPathInfo()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getPathTranslated()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getContextPath()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getQueryString()
    {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Override
    public boolean isUserInRole(String role)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Principal getUserPrincipal()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getRequestedSessionId()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getRequestURI()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public StringBuffer getRequestURL()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getServletPath()
    {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Override
    public boolean isRequestedSessionIdValid()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isRequestedSessionIdFromCookie()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isRequestedSessionIdFromURL()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isRequestedSessionIdFromUrl()
    {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Override
    public String getCharacterEncoding()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getContentLength()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getContentType()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getParameter(String name)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Enumeration getParameterNames()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String[] getParameterValues(String name)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map getParameterMap()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getProtocol()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getScheme()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getServerName()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getServerPort()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BufferedReader getReader() throws IOException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getRemoteAddr()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getRemoteHost()
    {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Override
    public Locale getLocale()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Enumeration getLocales()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isSecure()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getRealPath(String path)
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
