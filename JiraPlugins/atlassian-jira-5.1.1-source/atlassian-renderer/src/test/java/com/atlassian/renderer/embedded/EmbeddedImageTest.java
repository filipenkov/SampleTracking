package com.atlassian.renderer.embedded;

import junit.framework.TestCase;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderContextOutputType;
import com.atlassian.renderer.attachments.RendererAttachmentManager;
import com.mockobjects.dynamic.Mock;

public class EmbeddedImageTest extends TestCase
{
    public void setUp() throws Exception
    {
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testThumbnail()
    {
        String s = "image.jpg|thumbnail";
        EmbeddedImage image = new EmbeddedImage(s);
        assertTrue(image.isThumbNail());
        assertFalse(image.getProperties().containsKey("thumbnail"));

        String s2 = "image.jpg";
        EmbeddedImage image2 = new EmbeddedImage(s2);
        assertFalse(image2.isThumbNail());
    }



    public void testExternal()
    {
        String s1 = "space:page^image.jpg";
        EmbeddedImage image1 = new EmbeddedImage(s1);
        assertFalse(image1.isExternal());

        String s2 = "http://www.host.com/path/to/image.gif";
        EmbeddedImage image2 = new EmbeddedImage(s2);
        assertTrue(image2.isExternal());
    }

    public void testBorderProperty()
    {
        String s1 = "image.jpg|border=5";
        EmbeddedImage image1 = new EmbeddedImage(s1);
        assertEquals("5", image1.getProperties().getProperty("border"));

        String s2 = "image.jpg";
        EmbeddedImage image2 = new EmbeddedImage(s2);
        assertEquals("0", image2.getProperties().getProperty("border"));
    }

    public void testAltProperty()
    {
        String alt1 = "image.jpg|alt=OneWord";
        EmbeddedImage image1 = new EmbeddedImage(alt1);
        assertEquals("OneWord", image1.getProperties().getProperty("alt"));

        String alt2 = "image.jpg|alt=Multiple words in alt property";
        EmbeddedImage image2 = new EmbeddedImage(alt2);
        assertEquals("Multiple words in alt property", image2.getProperties().getProperty("alt"));
    }

    public void testMultipleProperties()
    {
        String markup = "image.jpg|alt=Hello world,border=5";
        EmbeddedImage image = new EmbeddedImage(markup);
        assertEquals("Hello world", image.getProperties().getProperty("alt"));
        assertEquals("5", image.getProperties().getProperty("border"));
    }

    public void testJavascriptPropertiesNotDisplayed()
    {
        String markup = "image.jpg|onclick=myEvilScript()";
        EmbeddedImage image = new EmbeddedImage(markup);
        assertFalse(image.getProperties().containsKey("onclick"));
    }

    public void testOtherRandomPropertiesNotDisplayed()
    {
        String markup = "image.jpg|foo=myEvilScript()";
        EmbeddedImage image = new EmbeddedImage(markup);
        assertFalse(image.getProperties().containsKey("foo"));
    }

    /**
     * The parameters should not be escaped by the EmbeddedImage bean, but rather when they
     * are inserted into some HTML output.
     */
    public void testParametersAreNotEscaped()
    {
        String markup = "image.jpg|alt=greaterthan>quote',align=doublequote\"";
        EmbeddedImage image = new EmbeddedImage(markup);
        assertEquals("greaterthan>quote'", image.getProperties().getProperty("alt"));
    }
}
