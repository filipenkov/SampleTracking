package com.atlassian.applinks.core.util;

import junit.framework.TestCase;

import java.net.URI;

public class URIUtilTest extends TestCase
{
    public void testUriConcatenation() throws Exception
    {
        assertEquals(new URI("http://host:80/foo/bar"), URIUtil.concatenate(new URI("http://host:80/foo"), new URI("bar")));
        assertEquals("http://host:80/foo/bar", URIUtil.concatenate("http://host:80/foo", "bar"));
        assertEquals("http://host:80/foo/bar/fubar", URIUtil.concatenate("http://host:80/foo", "bar", "fubar"));
        assertEquals("foo/bar", URIUtil.concatenate("foo", "bar"));
        assertEquals("foo/bar", URIUtil.concatenate("foo/", "bar"));
        assertEquals("foo/bar/", URIUtil.concatenate("foo/", "bar/"));
        assertEquals("foo/bar", URIUtil.concatenate("foo/", "/bar"));
        assertEquals("foo/bar", URIUtil.concatenate("foo//", "//bar"));
        assertEquals("foo/bar/fubar", URIUtil.concatenate("foo//", "//bar", "/fubar"));
        assertEquals("foo/", URIUtil.concatenate("foo/", ""));
        assertEquals("foo/", URIUtil.concatenate("foo/", "/"));
        assertEquals("foo/", URIUtil.concatenate("foo//", "/"));
        assertEquals("/", URIUtil.concatenate("", "/"));
        assertEquals("/", URIUtil.concatenate("/", ""));
    }
}
