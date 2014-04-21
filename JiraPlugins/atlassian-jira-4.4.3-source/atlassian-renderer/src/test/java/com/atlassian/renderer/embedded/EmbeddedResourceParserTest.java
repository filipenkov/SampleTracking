package com.atlassian.renderer.embedded;

import junit.framework.TestCase;

import java.util.Properties;

public class EmbeddedResourceParserTest extends TestCase
{

    public void setUp() throws Exception
    {
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testExternalResource()
    {
        String s = "http://some.host.com/path/to/image.jpg";
        EmbeddedResourceParser parser = new EmbeddedResourceParser(s);
        assertEquals(s, parser.getResource());
        assertEquals("image.jpg", parser.getFilename());
        assertEquals("image/jpeg", parser.getType());
    }

    // CONF-4849
    public void testExternalResourceWithParameters()
    {
        String s = "http://some.host.com/path/to/image.jpg?version=1";
        EmbeddedResourceParser parser = new EmbeddedResourceParser(s);
        assertEquals(s, parser.getResource());
        assertEquals("image.jpg", parser.getFilename());
        assertEquals("image/jpeg", parser.getType());
    }

    public void testTypeOverrideParameter()
    {
        String s = "foo?bar=fo&one=two|type=image/jpeg";
        EmbeddedResourceParser parser = new EmbeddedResourceParser(s);
        assertEquals("foo?bar=fo&one=two", parser.getResource());
        assertEquals("foo?bar=fo&one=two", parser.getFilename());
        assertEquals("image/jpeg", parser.getType());
    }

    public void testInternalResource()
    {
        String s = "image.jpg";
        EmbeddedResourceParser parser = new EmbeddedResourceParser(s);
        assertEquals(s, parser.getResource());
        assertEquals(s, parser.getFilename());
        assertEquals("image/jpeg", parser.getType());
    }

    // RNDR-6
    public void testBitmapResource()
    {
        String s = "image.bmp";
        EmbeddedResourceParser parser = new EmbeddedResourceParser(s);
        assertEquals(s, parser.getResource());
        assertEquals(s, parser.getFilename());
        assertEquals("image/x-ms-bmp", parser.getType());
    }

    public void testPageResource()
    {
        String s = "Page A^image.gif";
        EmbeddedResourceParser parser = new EmbeddedResourceParser(s);
        assertEquals("Page A^image.gif", parser.getResource());
        assertEquals("Page A", parser.getPage());
        assertEquals("image.gif", parser.getFilename());
        assertEquals("image/gif", parser.getType());
    }

    public void testSpaceResource()
    {
        String s = "Space A:Page A^image.jpg";
        EmbeddedResourceParser parser = new EmbeddedResourceParser(s);
        assertEquals("Space A:Page A^image.jpg", parser.getResource());
        assertEquals("Page A", parser.getPage());
        assertEquals("Space A", parser.getSpace());
        assertEquals("image.jpg", parser.getFilename());
        assertEquals("image/jpeg", parser.getType());
    }

    public void testResourceProperties()
    {
        String s1 = "http://some.host.com/path/to/image.jpg|property";
        String s2 = "image.jpg|propertyA, propertyB=B";

        EmbeddedResourceParser parser = new EmbeddedResourceParser(s1);
        assertEquals("http://some.host.com/path/to/image.jpg", parser.getResource());
        Properties p = parser.getProperties();
        assertEquals(1, p.size());
        assertEquals("property", p.propertyNames().nextElement());
        assertEquals("image.jpg", parser.getFilename());
        assertEquals("image/jpeg", parser.getType());

        parser = new EmbeddedResourceParser(s2);
        assertEquals("image.jpg", parser.getResource());
        p = parser.getProperties();
        assertEquals(2, p.size());
        assertTrue(p.containsKey("propertyA"));
        assertEquals("", p.getProperty("propertyA"));
        assertTrue(p.containsKey("propertyB"));
        assertEquals("B", p.getProperty("propertyB"));
        assertEquals("image.jpg", parser.getFilename());
        assertEquals("image/jpeg", parser.getType());
    }
}
