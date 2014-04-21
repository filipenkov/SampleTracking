package com.atlassian.core.test.util;

import junit.framework.TestCase;
import com.atlassian.core.util.collection.EasyList;

import java.sql.SQLException;

public class TestDuckTypeProxy extends TestCase
{
    public interface MyInterface
    {
        String getString();
        Long getLong();
        Integer getInteger();
        String get(String string);
        String get(Integer integer);
        String getStuffed() throws SQLException;
    }

    public void testProxyReturns() throws Exception
    {
        Object obj = new Object()
        {
            public String getString()
            {
                return "who's you're daddy?";
            }
        };
        MyInterface impl = (MyInterface) DuckTypeProxy.getProxy(MyInterface.class, EasyList.build(obj), DuckTypeProxy.THROW);
        assertEquals("who's you're daddy?", impl.getString());
    }

    public void testProxyThrows() throws Exception
    {
        MyInterface impl = (MyInterface) DuckTypeProxy.getProxy(MyInterface.class, EasyList.build(new Object()), DuckTypeProxy.THROW);
        try
        {
            impl.getString();
            fail("should have thrown USOE");
        }
        catch (UnsupportedOperationException yay)
        {
        }
    }

    public void testProxyThrowsTarget() throws Exception
    {
        Object obj = new Object()
        {
            public String getStuffed() throws SQLException
            {
                throw new SQLException("bad, bad, bad");
            }
        };
        MyInterface impl = (MyInterface) DuckTypeProxy.getProxy(MyInterface.class, EasyList.build(obj));
        try
        {
            impl.getStuffed();
            fail("should have thrown USOE");
        }
        catch (SQLException yay)
        {
        }
    }

    public void testProxyDelegatesToSecond() throws Exception
    {
        Object obj = new Object()
        {
            public String getString()
            {
                return "who's you're daddy?";
            }
        };
        MyInterface impl = (MyInterface) DuckTypeProxy.getProxy(MyInterface.class, EasyList.build(new Object(), obj), DuckTypeProxy.THROW);
        assertEquals("who's you're daddy?", impl.getString());
    }

    public void testNotNullParameter() throws Exception
    {
        Object obj = new Object()
        {
            public String get(String string)
            {
                return "how about: " + string;
            }
        };
        MyInterface impl = (MyInterface) DuckTypeProxy.getProxy(MyInterface.class, EasyList.build(obj), DuckTypeProxy.THROW);
        assertEquals("how about: me", impl.get("me"));
    }

    public void testNullParameter() throws Exception
    {
        Object obj = new Object()
        {
            public String get(String string)
            {
                return "how about: " + string;
            }
        };
        MyInterface impl = (MyInterface) DuckTypeProxy.getProxy(MyInterface.class, EasyList.build(obj), DuckTypeProxy.THROW);
        assertEquals("how about: null", impl.get((String) null));
    }
}