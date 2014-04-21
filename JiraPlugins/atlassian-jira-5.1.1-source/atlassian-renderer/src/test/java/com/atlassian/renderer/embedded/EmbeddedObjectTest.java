package com.atlassian.renderer.embedded;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 22/09/2005
 * Time: 11:51:21
 */

public class EmbeddedObjectTest extends TestCase
{
    public void setUp() throws Exception
    {
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testQuicktime()
    {
        String s = "movie.mov|width=440";
        EmbeddedObject object = new EmbeddedObject(s);
        assertTrue(object.getProperties().containsKey("width"));
        assertTrue(object.isInternal());
        assertTrue(object.getType().indexOf("video") != -1);
    }

    public void testExternalObject()
    {
        String s1 = "space:page^movie.mov";
        EmbeddedObject movie1 = new EmbeddedObject(s1);
        assertFalse(movie1.isExternal());

        String s2 = "http://www.apple.com/movie.mov";
        EmbeddedObject movie2 = new EmbeddedObject(s2);
        assertTrue(movie2.isExternal());
        assertTrue(movie2.getType().indexOf("video") != -1);
    }
}
