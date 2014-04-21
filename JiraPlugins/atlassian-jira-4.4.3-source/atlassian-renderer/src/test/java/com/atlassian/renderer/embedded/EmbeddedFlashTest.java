package com.atlassian.renderer.embedded;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 22/09/2005
 * Time: 12:00:39
 */

public class EmbeddedFlashTest extends TestCase
{
    public void setUp() throws Exception
    {
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testFlash()
    {
        String s = "movie.swf|width=440";
        EmbeddedFlash object = new EmbeddedFlash(s);
        assertTrue(object.getProperties().containsKey("width"));
        assertTrue(object.getProperties().get("width").equals("440"));
        assertTrue(object.getProperties().containsKey("quality"));
        assertTrue(object.getProperties().get("quality").equals("high"));
        assertTrue(object.isInternal());
        assertTrue(object.getType().indexOf("flash") != -1);
    }

    public void testOverriddenDefaultProperties()
    {
        String s = "movie.swf|width=440,quality=low,";
        EmbeddedFlash object = new EmbeddedFlash(s);
        assertTrue(object.getProperties().containsKey("width"));
        assertTrue(object.getProperties().get("width").equals("440"));
        assertTrue(object.getProperties().containsKey("quality"));
        assertTrue(object.getProperties().get("quality").equals("low"));
        assertTrue(object.isInternal());
        assertTrue(object.getType().indexOf("flash") != -1);
    }

    public void testExternalFlash()
    {
        String s1 = "space:page^movie.swf";
        EmbeddedFlash movie1 = new EmbeddedFlash(s1);
        assertFalse(movie1.isExternal());

        String s2 = "http://www.apple.com/movie.swf";
        EmbeddedFlash movie2 = new EmbeddedFlash(s2);
        assertTrue(movie2.isExternal());
        assertTrue(movie2.getType().indexOf("flash") != -1);
    }
}
