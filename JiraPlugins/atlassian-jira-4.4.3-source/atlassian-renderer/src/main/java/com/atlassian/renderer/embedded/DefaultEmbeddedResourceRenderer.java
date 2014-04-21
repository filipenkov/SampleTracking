package com.atlassian.renderer.embedded;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.attachments.RendererAttachmentManager;
import com.atlassian.renderer.v2.RenderUtils;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 21/09/2005
 * Time: 17:22:10
 */

/**
 * Class that passes on the rendering of an EmbeddedResource to the correct renderer
 */
public class DefaultEmbeddedResourceRenderer implements EmbeddedResourceRenderer
{
    protected HashMap renderMap;
    protected RendererAttachmentManager attachmentManager;

    public DefaultEmbeddedResourceRenderer()
    {
    }

    public DefaultEmbeddedResourceRenderer(RendererAttachmentManager attachmentManager)
    {
        this.attachmentManager = attachmentManager;
    }

    public String renderResource(EmbeddedResource resource, RenderContext context)
    {
        try
        {
            // Before we continue, make sure the user can view the attachment (and that it exists)
            if(resource.isInternal() && attachmentManager.getAttachment(context, resource) == null)
            {
                // When rendering for WYSIWYG, we want to display a placeholder image, so that content isn't lost (CONF-4929)
                if (context.isRenderingForWysiwyg())
                    return new PlaceholderImageRenderer().renderResource(resource, context);

                // Otherwise if we're rendering it for normal use, be explicit and show the error text.
                throw new IllegalArgumentException("Unable to render embedded object: File (" + resource.getFilename() + ") not found.");
            }

            // When the resource is not supported
            if (!getRenderMap().containsKey(resource.getClass()))
            {
                // When rendering for WYSIWYG, we want to display a placeholder image, so that content isn't lost (CONF-4929)
                if (context.isRenderingForWysiwyg())
                    return new PlaceholderImageRenderer().renderResource(resource, context);

                throw new IllegalArgumentException("Unsupported embedded resource type: " + resource.getType());
            }

            EmbeddedResourceRenderer delegate = (EmbeddedResourceRenderer) getRenderMap().get(resource.getClass());

            // When the object is not an image, and we're rendering for WYSIWYG, put in a placeholder, otherwise data will be lost
            if (context.isRenderingForWysiwyg() && !EmbeddedImage.class.equals(resource.getClass()))
                delegate = new PlaceholderImageRenderer();

            return delegate.renderResource(resource, context);
        }
        // If there's an appropriate exception, return it as the output
        catch (Exception e)
        {
            return RenderUtils.error(e.getMessage());
        }
    }

    public RendererAttachmentManager getAttachmentManager()
    {
        return attachmentManager;
    }

    public void setAttachmentManager(RendererAttachmentManager attachmentManager)
    {
        this.attachmentManager = attachmentManager;
    }

    protected HashMap getRenderMap()
    {
        if (renderMap == null)
        {
            renderMap = new HashMap();
            EmbeddedObjectRenderer embeddedObjectRenderer = new EmbeddedObjectRenderer(attachmentManager);
            renderMap.put(EmbeddedFlash.class, new EmbeddedFlashRenderer(attachmentManager));
            renderMap.put(EmbeddedImage.class, new EmbeddedImageRenderer(attachmentManager));
            renderMap.put(EmbeddedRealMedia.class, new EmbeddedRealMediaRenderer(attachmentManager));
            renderMap.put(EmbeddedObject.class, embeddedObjectRenderer);
            renderMap.put(EmbeddedQuicktime.class, embeddedObjectRenderer);
            renderMap.put(EmbeddedWindowsMedia.class, embeddedObjectRenderer);
            renderMap.put(EmbeddedAudio.class, embeddedObjectRenderer);
            renderMap.put(UnembeddableObject.class, new UnembeddableObjectRenderer());
        }
        return renderMap;
    }
}
