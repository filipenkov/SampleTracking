package com.atlassian.labs.botkiller;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class MockHttpSession implements HttpSession
{
    private final Map<String, Object> attributes = new HashMap<String, Object>();
    private int maxInactiveInterval;

    @Override
    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        attributes.put(name,value);
    }

    @Override
    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }

    @Override
    public void setMaxInactiveInterval(int interval)
    {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval()
    {
        return this.maxInactiveInterval;
    }

    @Override
    public long getCreationTime()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getId()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long getLastAccessedTime()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServletContext getServletContext()
    {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Override
    public HttpSessionContext getSessionContext()
    {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Override
    public Object getValue(String name)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Enumeration getAttributeNames()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String[] getValueNames()
    {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Override
    public void putValue(String name, Object value)
    {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Override
    public void removeValue(String name)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void invalidate()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isNew()
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
