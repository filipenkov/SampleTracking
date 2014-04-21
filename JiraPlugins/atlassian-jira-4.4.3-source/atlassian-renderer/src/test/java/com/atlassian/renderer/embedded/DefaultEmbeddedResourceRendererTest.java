package com.atlassian.renderer.embedded;

import junit.framework.TestCase;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.C;
import com.atlassian.renderer.attachments.RendererAttachmentManager;
import com.atlassian.renderer.attachments.RendererAttachment;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderContextOutputType;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 15/12/2005
 * Time: 11:52:13
 */

public class DefaultEmbeddedResourceRendererTest extends TestCase
{
    private DefaultEmbeddedResourceRenderer renderer;
    private Mock mockAttachmentManager;

    public void setUp()
    {
        mockAttachmentManager = new Mock(RendererAttachmentManager.class);

        renderer = new DefaultEmbeddedResourceRenderer((RendererAttachmentManager) mockAttachmentManager.proxy());
    }

    public void testValidInternalImage()
    {
        String url = "/conf/image.jpg";

        RenderContext context = new RenderContext();
        context.setRenderingForWysiwyg(false);

        EmbeddedImage resource = new EmbeddedImage("image.jpg");

        RendererAttachment attachment = new RendererAttachment(1, "image.jpg", "image/jpeg", "john", "no comment", url, null, null, new Timestamp((new Date()).getTime()));

        mockAttachmentManager.expectAndReturn("getAttachment", C.args(C.eq(context), C.eq(resource)), attachment);
        mockAttachmentManager.expectAndReturn("getAttachment", C.args(C.eq(context), C.eq(resource)), attachment);

        String token = renderer.renderResource(resource, context);
        assertNotNull(token);

        String renderedText = (String) context.getRenderedContentStore().get(token);
        assertTrue(renderedText.indexOf("src=\"" + url) > -1);
    }

    // CONF-5293
    public void testValidInternalImageRenderingForWord()
    {
        String fileName = "image.jpg";
        String contextPath = "/conf";
        String url = contextPath + "/" + fileName;
        String baseUrl = "http://localhost" + contextPath;

        RenderContext context = new RenderContext();
        context.setRenderingForWysiwyg(false);
        context.setOutputType(RenderContextOutputType.WORD);
        context.setBaseUrl(baseUrl);
        context.setSiteRoot(contextPath);

        EmbeddedImage resource = new EmbeddedImage("image.jpg");

        RendererAttachment attachment = new RendererAttachment(1, fileName, "image/jpeg", "john", "no comment", url, null, null, new Timestamp((new Date()).getTime()));

        mockAttachmentManager.expectAndReturn("getAttachment", C.args(C.eq(context), C.eq(resource)), attachment);
        mockAttachmentManager.expectAndReturn("getAttachment", C.args(C.eq(context), C.eq(resource)), attachment);

        String token = renderer.renderResource(resource, context);
        assertNotNull(token);

        String renderedText = (String) context.getRenderedContentStore().get(token);
        assertTrue("Embedded image was expected to contain the full URL, but didn't.", renderedText.indexOf("src=\"" + baseUrl + "/" + fileName) > -1);
    }

    // CONF-5293
    public void testValidInternalImageRenderingForWordWithNullContextPath()
    {
        String fileName = "image.jpg";
        String url = "/" + fileName;
        String baseUrl = "http://localhost";

        RenderContext context = new RenderContext();
        context.setRenderingForWysiwyg(false);
        context.setOutputType(RenderContextOutputType.WORD);
        context.setBaseUrl(baseUrl);

        EmbeddedImage resource = new EmbeddedImage("image.jpg");

        RendererAttachment attachment = new RendererAttachment(1, fileName, "image/jpeg", "john", "no comment", url, null, null, new Timestamp((new Date()).getTime()));

        mockAttachmentManager.expectAndReturn("getAttachment", C.args(C.eq(context), C.eq(resource)), attachment);
        mockAttachmentManager.expectAndReturn("getAttachment", C.args(C.eq(context), C.eq(resource)), attachment);

        String token = renderer.renderResource(resource, context);
        assertNotNull(token);

        String renderedText = (String) context.getRenderedContentStore().get(token);
        assertTrue("Embedded image was expected to contain the full URL, but didn't.", renderedText.indexOf("src=\"" + baseUrl + "/" + fileName) > -1);
    }

    // CONF-5293
    public void testValidInternalImageRenderingForWordWithEmptyContextPath()
    {
        String fileName = "image.jpg";
        String url = "/" + fileName;
        String baseUrl = "http://localhost";

        RenderContext context = new RenderContext();
        context.setRenderingForWysiwyg(false);
        context.setSiteRoot("");
        context.setOutputType(RenderContextOutputType.WORD);
        context.setBaseUrl(baseUrl);

        EmbeddedImage resource = new EmbeddedImage("image.jpg");

        RendererAttachment attachment = new RendererAttachment(1, fileName, "image/jpeg", "john", "no comment", url, null, null, new Timestamp((new Date()).getTime()));

        mockAttachmentManager.expectAndReturn("getAttachment", C.args(C.eq(context), C.eq(resource)), attachment);
        mockAttachmentManager.expectAndReturn("getAttachment", C.args(C.eq(context), C.eq(resource)), attachment);

        String token = renderer.renderResource(resource, context);
        assertNotNull(token);

        String renderedText = (String) context.getRenderedContentStore().get(token);
        assertTrue("Embedded image was expected to contain the full URL, but didn't.", renderedText.indexOf("src=\"" + baseUrl + "/" + fileName) > -1);
    }

    public void testInvalidInternalImage()
    {
        RenderContext context = new RenderContext();
        context.setRenderingForWysiwyg(false);

        EmbeddedImage resource = new EmbeddedImage("image.jpg");

        // Covers when the attachment can't be found, or the user is not allowed to see it
        mockAttachmentManager.expectAndReturn("getAttachment", C.args(C.eq(context), C.eq(resource)), null);

        String output = renderer.renderResource(resource, context);

        // We expect an illegal argument exception to be thrown here
        assertTrue("Render output was expected to contain an error message, but didn't.", output.indexOf("<span class=\"error\">") > -1);
    }

    // CONF-4929
    public void testInvalidInternalImageForWysiwyg()
    {
        RenderContext context = new RenderContext();
        context.setRenderingForWysiwyg(true);

        EmbeddedImage resource = new EmbeddedImage("image.jpg");

        mockAttachmentManager.expectAndReturn("getAttachment", C.args(C.eq(context), C.eq(resource)), null);

        String output = renderer.renderResource(resource, context);
        String renderOutput = (String) context.getRenderedContentStore().get(output);

        assertTrue("Render output was expected to contain a placeholder image, but didn't.", renderOutput.indexOf("film.gif") > -1);
    }

    public void testValidInternalMovieForWysiwyg()
    {
        String fileName = "movie.mov";

        RenderContext context = new RenderContext();
        context.setRenderingForWysiwyg(true);

        EmbeddedQuicktime resource = new EmbeddedQuicktime(fileName);

        RendererAttachment attachment = new RendererAttachment(1, fileName, "video/quicktime", "john", "no comment", "http://fake.url/" + fileName, null, null, new Timestamp((new Date()).getTime()));

        mockAttachmentManager.expectAndReturn("getAttachment", C.args(C.eq(context), C.eq(resource)), attachment);

        String output = renderer.renderResource(resource, context);
        String renderOutput = (String) context.getRenderedContentStore().get(output);

        assertTrue("Render output was expected to contain a placeholder image, but didn't.", renderOutput.indexOf("film.gif") > -1);
    }

    public void testUnsupportedObject()
    {
        RenderContext context = new RenderContext();
        context.setRenderingForWysiwyg(false);

        FakeEmbeddedObject resource = new FakeEmbeddedObject("image.jpg");

        RendererAttachment attachment = new RendererAttachment(1, "image.jpg", "image/jpeg", "john", "no comment", "http://fake.url/image.jpg", null, null, new Timestamp((new Date()).getTime()));

        mockAttachmentManager.expectAndReturn("getAttachment", C.args(C.eq(context), C.eq(resource)), attachment);

        String output = renderer.renderResource(resource, context);

        // We expect an illegal argument exception to be thrown here
        assertTrue("Render output was expected to contain an error message, but didn't.", output.indexOf("Unsupported embedded resource type") > -1);
    }

    public void testUnsupportedObjectForWysiwyg()
    {
        RenderContext context = new RenderContext();
        context.setRenderingForWysiwyg(true);

        FakeEmbeddedObject resource = new FakeEmbeddedObject("image.jpg");

        RendererAttachment attachment = new RendererAttachment(1, "image.jpg", "image/jpeg", "john", "no comment", "http://fake.url/image.jpg", null, null, new Timestamp((new Date()).getTime()));

        mockAttachmentManager.expectAndReturn("getAttachment", C.args(C.eq(context), C.eq(resource)), attachment);

        String output = renderer.renderResource(resource, context);
        String renderOutput = (String) context.getRenderedContentStore().get(output);

        assertTrue("Render output was expected to contain a placeholder image, but didn't.", renderOutput.indexOf("film.gif") > -1);
    }
}
