package com.atlassian.renderer.embedded;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.attachments.RendererAttachmentManager;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 22/09/2005
 * Time: 09:47:49
 */

public class EmbeddedFlashRenderer extends EmbeddedObjectRenderer implements EmbeddedResourceRenderer
{
    public EmbeddedFlashRenderer(RendererAttachmentManager attachmentManager)
    {
        super(attachmentManager);
    }

    public String renderResource(EmbeddedResource resource, RenderContext context)
    {
        // Setup the properties and fetch the object URL
        Map contextMap = setupObjectProperties(resource, context);

        // Flash-specific stuff
        if (contextMap.get("movie") == null)
            contextMap.put("movie", contextMap.get("object"));

        String renderedObjectHtml = renderEmbeddedObject(contextMap);

        return renderEmbeddedObjectWrapper(renderedObjectHtml, contextMap);
    }
}
