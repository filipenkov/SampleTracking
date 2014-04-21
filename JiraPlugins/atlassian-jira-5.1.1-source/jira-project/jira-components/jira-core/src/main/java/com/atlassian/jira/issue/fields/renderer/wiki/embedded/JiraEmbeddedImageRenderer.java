package com.atlassian.jira.issue.fields.renderer.wiki.embedded;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.attachments.RendererAttachment;
import com.atlassian.renderer.embedded.EmbeddedImage;
import com.atlassian.renderer.embedded.EmbeddedImageRenderer;
import com.atlassian.renderer.embedded.EmbeddedResource;

public class JiraEmbeddedImageRenderer extends EmbeddedImageRenderer
{
    private long attachmentId;
    private String attachmentSrc;
    private String attachmentName;
    private boolean isThumbNail;

    public JiraEmbeddedImageRenderer(com.atlassian.renderer.attachments.RendererAttachmentManager attachmentManager)
    {
        super(attachmentManager);
    }

    public String renderResource(EmbeddedResource resource, RenderContext context)
    {
        EmbeddedImage image = (EmbeddedImage) resource;
        isThumbNail = image.isThumbNail();
        if (isThumbNail && !image.isExternal())
        {
            try
            {
                RendererAttachment attachment = getAttachment(context, resource);
                attachmentId = attachment.getId();
                attachmentSrc = attachment.getSrc();
                attachmentName = attachment.getFileName();
            }
            catch(RuntimeException re)
            {
            }
        }

        return super.renderResource(resource, context);
    }

    protected String writeImage(String imageTag, boolean centered)
    {
        if (isThumbNail)
        {
            //wrap the thumbnail image with the link to the actual attachment (the full-sized version) JRA-11198
            String result = "";
            result += "<a id=\"" + attachmentId + "_thumb\" href=\"" + attachmentSrc + "\" title=\"" + attachmentName + "\">";
            result += imageTag;
            result += "</a>";
            return super.writeImage(result, centered);
        }
        return super.writeImage(imageTag, centered);
    }
}
