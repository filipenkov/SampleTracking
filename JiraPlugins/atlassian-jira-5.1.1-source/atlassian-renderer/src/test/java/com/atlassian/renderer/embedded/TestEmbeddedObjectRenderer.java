package com.atlassian.renderer.embedded;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderContextOutputType;
import com.atlassian.renderer.attachments.RendererAttachment;
import com.atlassian.renderer.attachments.RendererAttachmentManager;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.Map;

public class TestEmbeddedObjectRenderer extends TestCase
{
    private RenderContext context;
    private EmbeddedObjectRenderer renderer;
    private Mock attachmentManager;

    public void testParametersAreEscaped()
    {
        // the input string contains quotes in an attempt to get the renderer to send it to the client unmodified
        String input = "image.jpg|alt=greaterthan>quote',align=doublequote\"";
        String expected = "<div class=\"embeddedObject\"><object align=\"doublequote&quot;\" data=\"quote&#39;\" type=\"image/jpeg\" ><param name=\"data\" value=\"quote&#39;\"/><param name=\"src\" value=\"quote&#39;\"/><param name=\"type\" value=\"image/jpeg\"/><embed align=\"doublequote&quot;\" src=\"quote&#39;\" type=\"image/jpeg\" /></object></div>";
        EmbeddedObject resource = new EmbeddedObject(input);
        RendererAttachment attachment = new RendererAttachment(0, null,null,null,null,"quote'",null,null,null);
        attachmentManager.expectAndReturn("getAttachment", C.args(C.isA(RenderContext.class),C.eq(resource)), attachment);
        
        String output = renderer.renderResource(resource, context);
        assertEquals("EmbeddedImageRenderer should escape double quotes when rendering", expected, output);
        attachmentManager.verify();
    }

    public void testIdIsEscaped()
    {
        String input = "image.jpg|id=foo\" onmouseover=\"alert()";
        String expected = "<div class=\"embeddedObject-foo&quot; onmouseover=&quot;alert()\"><object data=\"data\" id=\"foo&quot; onmouseover=&quot;alert()\" type=\"image/jpeg\" ><param name=\"data\" value=\"data\"/><param name=\"src\" value=\"data\"/><param name=\"type\" value=\"image/jpeg\"/><embed id=\"foo&quot; onmouseover=&quot;alert()\" src=\"data\" type=\"image/jpeg\" /></object></div>";
        EmbeddedObject resource = new EmbeddedObject(input);
        RendererAttachment attachment = new RendererAttachment(0, null, null, null, null, "data", null, null, null);
        attachmentManager.expectAndReturn("getAttachment", C.args(C.isA(RenderContext.class),C.eq(resource)), attachment);

        String output = renderer.renderResource(resource, context);
        assertEquals("EmbeddedImageRenderer should escape ids when rendering", expected, output);
        attachmentManager.verify();
    }

    // RNDR-24
    public void testParameterSyntax()
    {
        Map paramMap = Collections.singletonMap("data", "cheese");
        EmbeddedObjectRenderer renderer = new EmbeddedObjectRenderer(null);
        String renderedObject = renderer.renderEmbeddedObject(paramMap);
        String expectedParam = "<param name=\"data\" value=\"cheese\"/>";
        boolean objectContainsExpectedParam = renderedObject.indexOf(expectedParam) >= 0;
        assertTrue("actual output '" + renderedObject + "' contains correct parameter syntax", objectContainsExpectedParam);
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
        renderer = new EmbeddedObjectRenderer((RendererAttachmentManager) attachmentManager.proxy());
    }
}
