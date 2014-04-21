package com.atlassian.crowd.search.query;

import junit.framework.TestCase;

public class QueryUtilsTest extends TestCase
{
    public void testAssignableFrom() throws Exception
    {
        assertEquals(Number.class, QueryUtils.checkAssignableFrom(Number.class, Number.class));
        assertEquals(Integer.class, QueryUtils.checkAssignableFrom(Integer.class, String.class, Number.class));
    }

    public void testAssignableFromErrorMessage() throws Exception
    {
        // test the failure case
        try
        {
            QueryUtils.checkAssignableFrom(Number.class, String.class, Integer.class);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals("Given type (java.lang.Number) must be assignable from one of [class java.lang.String, class java.lang.Integer]", ex.getMessage());
        }
    }
}