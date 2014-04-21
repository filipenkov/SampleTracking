package mock.servlet;

import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * @since v4.0
 */
public class MockHttpSession implements HttpSession
{
    private final Map<String, Object> backingMap;
    private final long creationTime;

    public MockHttpSession()
    {
        this.backingMap = new HashMap<String, Object>();
        this.creationTime = System.currentTimeMillis();
    }
    public MockHttpSession(Map<String,Object> backingMap)
    {
        this.backingMap = backingMap;
        this.creationTime = System.currentTimeMillis();
    }

    public long getCreationTime()
    {
        return creationTime;
    }

    public String getId()
    {
        return "session1234";
    }

    public long getLastAccessedTime()
    {
        return System.currentTimeMillis();
    }

    public ServletContext getServletContext()
    {
        return null;
    }

    public void setMaxInactiveInterval(final int i)
    {
    }

    public int getMaxInactiveInterval()
    {
        return 0;
    }

    public HttpSessionContext getSessionContext()
    {
        return null;
    }

    public Object getAttribute(final String s)
    {
        return backingMap.get(s);
    }

    public Object getValue(final String s)
    {
        return backingMap.get(s);
    }

    public Enumeration getAttributeNames()
    {
        return null;
    }

    public String[] getValueNames()
    {
        return new String[0];
    }

    public void setAttribute(final String s, final Object o)
    {
        backingMap.put(s,o);
    }

    public void putValue(final String s, final Object o)
    {
        backingMap.put(s,o);
    }

    public void removeAttribute(final String s)
    {
        backingMap.remove(s);
    }

    public void removeValue(final String s)
    {
        backingMap.remove(s);
    }

    public void invalidate()
    {
        backingMap.clear();
    }

    public boolean isNew()
    {
        return false;
    }
}
