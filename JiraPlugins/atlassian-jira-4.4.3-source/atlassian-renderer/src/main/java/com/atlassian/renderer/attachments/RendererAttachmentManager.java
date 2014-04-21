package com.atlassian.renderer.attachments;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.embedded.EmbeddedResource;
import com.atlassian.renderer.embedded.EmbeddedImage;

/**
 * Defines a project agnostic attachment manager that will allow the embedded resources
 * to resolve and work with attachments.
 */
public interface RendererAttachmentManager
{
    public RendererAttachment getAttachment(RenderContext context, EmbeddedResource resource);

    public RendererAttachment getThumbnail(RendererAttachment attachment, RenderContext context, EmbeddedImage resource);

    public boolean systemSupportsThumbnailing();
}
