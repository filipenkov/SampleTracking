package com.atlassian.core.util.map;

import java.util.Map;
import junit.framework.TestCase;

public class EasyMapTest extends TestCase
{
    public void testEasyMapKeyValues() throws Exception
    {
        Map myMap = EasyMap.build("hello", "world", "hey", "there");
        assertEquals("world", myMap.get("hello"));
        assertEquals("there", myMap.get("hey"));

        try
        {
            myMap = EasyMap.build("hello", "world", "hey");
            fail("should have thrown a RuntimeException");
        }
        catch (Throwable e)
        {
            assertTrue(e instanceof RuntimeException);
            assertEquals("The number of parameters should be even when building a map", e.getMessage());
        }
    }
}
