package com.atlassian.gadgets.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UriTemplateTest
{

    @Test
    public void matchesSimpleUri()
    {
        UriTemplate uriTemplate = new UriTemplate("/foo");
        assertTrue(uriTemplate.matches("/foo"));
    }
    
    @Test
    public void doesNotMatchNonMatchingUri()
    {
        UriTemplate uriTemplate = new UriTemplate("/foo");
        assertFalse(uriTemplate.matches("/bar"));
    }
    
    @Test
    public void matchesUriWithVariables()
    {
        UriTemplate uriTemplate = new UriTemplate("/foo/{bar}");
        assertTrue(uriTemplate.matches("/foo/baz"));
    }
    
    @Test
    public void matchesUriWithMultipleVariables()
    {
        UriTemplate uriTemplate = new UriTemplate("/foo/{one}/bar/{two}/baz");
        assertTrue(uriTemplate.matches("/foo/1/bar/2/baz"));
    }
    
    @Test
    public void extractsSingleVariable()
    {
        UriTemplate uriTemplate = new UriTemplate("/foo/{bar}");
        Map<String, String> expected = Collections.singletonMap("bar", "baz");
        assertEquals(expected, uriTemplate.extractParameters("/foo/baz"));
    }
    
    @Test
    public void extractsMultipleVariables()
    {
        UriTemplate uriTemplate = new UriTemplate("/foo/{one}/bar/{two}/baz");
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("one", "1");
        expected.put("two", "2");
        assertEquals(expected, uriTemplate.extractParameters("/foo/1/bar/2/baz"));
    }
}
