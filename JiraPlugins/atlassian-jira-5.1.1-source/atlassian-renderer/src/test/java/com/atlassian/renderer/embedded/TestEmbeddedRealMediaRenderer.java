package com.atlassian.renderer.embedded;


import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderContextOutputType;
import com.atlassian.renderer.attachments.RendererAttachment;
import com.atlassian.renderer.attachments.RendererAttachmentManager;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import junit.framework.TestCase;

public class TestEmbeddedRealMediaRenderer extends TestCase
{
    private RenderContext context;
    private EmbeddedRealMediaRenderer renderer;
    private Mock attachmentManager;

    public void testIdIsEscaped()
    {
        String input = "video.rm|id=foo\" onmouseover=\"alert()";
        String expected = "id=\"foo&quot; onmouseover=&quot;alert()\"";
        EmbeddedObject resource = new EmbeddedRealMedia(input);
        RendererAttachment attachment = new RendererAttachment(0, null, null, null, null, "data", null, null, null);
        attachmentManager.expectAndReturn("getAttachment", C.args(C.isA(RenderContext.class), C.eq(resource)), attachment);

        String output = renderer.renderResource(resource, context);
        assertTrue("EmbeddedRealMediaRenderer should escape ids when rendering", output.contains(expected));
        attachmentManager.verify();
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        attachmentManager = new Mock(RendererAttachmentManager.class);
        context = new RenderContext();
        context.setRenderingForWysiwyg(false);
        context.setSiteRoot("");
        context.setOutputType(RenderContextOutputType.WORD);
        context.setBaseUrl("");
        renderer = new EmbeddedRealMediaRenderer((RendererAttachmentManager) attachmentManager.proxy());
    }
}
