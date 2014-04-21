package com.atlassian.security.auth.trustedapps.filter;

import com.mockobjects.servlet.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

public class MockRequest extends MockHttpServletRequest
{

    private final Map<String, String> headers = new HashMap<String, String>();
    protected final String pathInfo;

    public MockRequest(final String pathInfo)
    {
        this.pathInfo = pathInfo;
    }

    public void addHeader(final String arg1, final String arg2)
    {
        headers.put(arg1, arg2);
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    @Override
    public String getHeader(final String key)
    {
        return headers.get(key);
    }

    @Override
    public String getRemoteAddr()
    {
        return "i.am.a.teapot";
    }

    @Override
    public String getPathInfo()
    {
        return pathInfo;
    }

}