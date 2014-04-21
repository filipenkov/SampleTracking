package com.atlassian.renderer.embedded;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderContextOutputType;
import com.atlassian.renderer.attachments.RendererAttachmentManager;
import com.atlassian.renderer.attachments.RendererAttachment;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.Callable;
import com.mockobjects.dynamic.C;
import junit.framework.TestCase;

public class TestEmbeddedImageRenderer extends TestCase
{
    private RenderContext context;
    private EmbeddedImageRenderer renderer;
    private Mock attachmentManager;

    /**
     * Make sure the embedded image renderer properly escapes quotes
     * (http://jira.atlassian.com/browse/CONF-9209)
     */
    public void testExternalImagesEscapeQuotes()
    {
        // the input string contains quotes in an attempt to get the renderer to send it to the client unmodified
        String input = "http://www.google.com/noimage.jpg\"onerror=\"\"";
        String expected = "<img src=\"http://www.google.com/noimage.jpg&quot;onerror=&quot;&quot;\" align=\"absmiddle\" border=\"0\" />";
        EmbeddedImage resource = new EmbeddedImage(input);
        assertTrue(resource.isExternal());


        // render the content and look up the result using the token
        String token = renderer.renderResource(resource, context);
        String output = (String) context.getRenderedContentStore().get(token);
        assertEquals("EmbeddedImageRenderer should escape double quotes when rendering", expected, output);
    }

    public void testParametersAreEscaped()
    {
        // the input string contains quotes in an attempt to get the renderer to send it to the client unmodified
        String input = "http://somewhere/image.jpg|alt=greaterthan>quote',align=doublequote\"";
        String expected = "<img src=\"http://somewhere/image.jpg\" align=\"doublequote&quot;\" alt=\"greaterthan&gt;quote&#39;\" border=\"0\" />";
        EmbeddedImage resource = new EmbeddedImage(input);
        assertTrue(resource.isExternal());

        // render the content and look up the result using the token
        String token = renderer.renderResource(resource, context);
        String output = (String) context.getRenderedContentStore().get(token);
        assertEquals("EmbeddedImageRenderer should escape double quotes when rendering", expected, output);
    }

    public void testThumbnailImageParametersAreEscaped()
    {
        attachmentManager.matchAndReturn("systemSupportsThumbnailing", true);
        attachmentManager.matchAndReturn("getAttachment", C.ANY_ARGS, new RendererAttachment(0, null, null, null, null,
            "image.jpg", null, null, null));
        attachmentManager.matchAndReturn("getThumbnail", C.ANY_ARGS, new RendererAttachment(0, null, null, null, null,
            "image.jpg", null, null, null));

        // the input string contains quotes in an attempt to get the renderer to send it to the client unmodified
        String input = "image.jpg|thumbnail=true,alt=>'\"&<";
        String expected = "<img src=\"image.jpg\" align=\"absmiddle\" alt=\"&gt;&#39;&quot;&amp;&lt;\" border=\"0\" />";
        EmbeddedImage resource = new EmbeddedImage(input);

        // render the content and look up the result using the token
        String token = renderer.renderResource(resource, context);
        String output = (String) context.getRenderedContentStore().get(token);
        assertEquals(expected, output);
    }

    public void testNonExternalImageParametersAreEscaped()
    {
        attachmentManager.matchAndReturn("systemSupportsThumbnailing", true);
        attachmentManager.matchAndReturn("getAttachment", C.ANY_ARGS, new RendererAttachment(0, null, null, null, null,
            "image.jpg", null, null, null));
        attachmentManager.matchAndReturn("getThumbnail", C.ANY_ARGS, new RendererAttachment(0, null, null, null, null,
            "image.jpg", null, null, null));

        // the input string contains quotes in an attempt to get the renderer to send it to the client unmodified
        String input = "image.jpg|alt=>'\"&<";
        String expected = "<img src=\"image.jpg\" align=\"absmiddle\" alt=\"&gt;&#39;&quot;&amp;&lt;\" border=\"0\" />";
        EmbeddedImage resource = new EmbeddedImage(input);

        // render the content and look up the result using the token
        String token = renderer.renderResource(resource, context);
        String output = (String) context.getRenderedContentStore().get(token);
        assertEquals(expected, output);
    }

    public void testThumbnailImageCommentIsEscaped()
    {
        attachmentManager.matchAndReturn("systemSupportsThumbnailing", true);
        attachmentManager.matchAndReturn("getAttachment", C.ANY_ARGS, new RendererAttachment(0, null, null, null, "><",
            "image.jpg", null, null, null));
        attachmentManager.matchAndReturn("getThumbnail", C.ANY_ARGS, new RendererAttachment(0, null, null, null, null,
            "image.jpg", null, null, null));

        // the input string contains quotes in an attempt to get the renderer to send it to the client unmodified
        String input = "image.jpg|thumbnail=true,alt=>'\"&<";
        String expected = "<img src=\"image.jpg\" align=\"absmiddle\" alt=\"&gt;&#39;&quot;&amp;&lt;\" border=\"0\" title=\"&gt;&lt;\" />";
        EmbeddedImage resource = new EmbeddedImage(input);

        // render the content and look up the result using the token
        String token = renderer.renderResource(resource, context);
        String output = (String) context.getRenderedContentStore().get(token);
        assertEquals(expected, output);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        context = new RenderContext();
        context.setRenderingForWysiwyg(false);
        context.setSiteRoot("");
        context.setOutputType(RenderContextOutputType.WORD);
        context.setBaseUrl("");
        attachmentManager = new Mock(RendererAttachmentManager.class);
        renderer = new EmbeddedImageRenderer((RendererAttachmentManager) attachmentManager.proxy());
    }
}
