package com.atlassian.renderer.embedded;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderContextOutputType;
import com.atlassian.renderer.attachments.RendererAttachment;
import com.atlassian.renderer.attachments.RendererAttachmentManager;
import com.atlassian.renderer.util.UrlUtil;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.v2.components.HtmlEscaper;
import com.opensymphony.util.TextUtils;

import java.util.*;

public class EmbeddedImageRenderer implements EmbeddedResourceRenderer
{
    private RendererAttachmentManager attachmentManager;

    public EmbeddedImageRenderer(RendererAttachmentManager attachmentManager)
    {
        this.attachmentManager = attachmentManager;
    }

    public RendererAttachmentManager getAttachmentManager()
    {
        return attachmentManager;
    }

    public void setAttachmentManager(RendererAttachmentManager attachmentManager)
    {
        this.attachmentManager = attachmentManager;
    }

    public String renderResource(EmbeddedResource resource, RenderContext context)
    {
        String token;

        EmbeddedImage image = (EmbeddedImage) resource;

        RendererAttachment attachment = null;
        if (!image.isExternal())
        {
            try
            {
                attachment = getAttachment(context, resource);
            }
            catch(RuntimeException re)
            {
                return context.addRenderedContent(RenderUtils.error(re.getMessage()));
            }
        }

        Map imageParams = new HashMap();
        imageParams.putAll(image.getProperties());
        if (context.isRenderingForWysiwyg())
        {
            imageParams.put("imagetext", resource.getOriginalLinkText());
        }
        if (image.isThumbNail())
        {
            if (image.isExternal())
            {
                token = context.addRenderedContent(RenderUtils.error(context, "Can only create thumbnails for attached images", originalLink(resource), false));
            }
            else if (!attachmentManager.systemSupportsThumbnailing())
            {
                token = context.addRenderedContent(RenderUtils.error(context, "This installation can not generate thumbnails: no image support in Java runtime", originalLink(resource), false));
            }
            else if (attachment == null && !context.isRenderingForWysiwyg())
            {
                token = context.addRenderedContent(RenderUtils.error(context, "Attachment '" + image.getFilename() + "' was not found", originalLink(resource), false));
            }
            else
            {
                token = context.addRenderedContent(generateThumbnail(imageParams, attachment, context, image));
            }
        }
        else
        {
            String imageUrl = "";

            if (image.isExternal())
            {
                imageUrl = image.getUrl();
            }
            else
            {
                if (attachment != null)
                {
                    // For internal attachments, the src attribute in RenderAttachment is an absolute path, but without the domain name...
                    // When rendering for Word output, we need to prefix it with the full URL (CONF-5293)
                    if (context.getOutputType().equals(RenderContextOutputType.WORD))
                    {
                        // Grab the context path
                        String contextPath = context.getSiteRoot();
                        String domain = context.getBaseUrl();

                        // The baseUrl contains the context path, so strip it off (if it exists)
                        // This is quite dodgy, but fiddling with the URL passed into the RenderAttachment is going to be troublesome
                        if (contextPath != null && contextPath.length() != 0 && domain.indexOf(contextPath) != -1)
                            domain = domain.substring(0, domain.indexOf(contextPath));

                        imageUrl += domain;
                    }

                    imageUrl += attachment.getSrc();
                }
            }

            boolean centered = extractCenteredParam(imageParams);

            token = context.addRenderedContent(writeImage("<img src=\"" + HtmlEscaper.escapeAll(imageUrl, false) + "\" " + outputParameters(imageParams) + "/>", centered));
        }

        return token;
    }

    protected RendererAttachment getAttachment(RenderContext context, EmbeddedResource resource)
    {
        return attachmentManager.getAttachment(context, resource);
    }

    private String originalLink(EmbeddedResource resource)
    {
        return "!" + resource.getOriginalLinkText() + "!";
    }

    protected RendererAttachment getThumbnail(RendererAttachment attachment, RenderContext context, EmbeddedImage embeddedImage)
    {
        return attachmentManager.getThumbnail(attachment, context, embeddedImage);
    }

    private String generateThumbnail(Map imageParams, RendererAttachment attachment, RenderContext context, EmbeddedImage embeddedImage)
    {
        if (attachment != null && TextUtils.stringSet(attachment.getComment()) && !imageParams.containsKey("title") && !imageParams.containsKey("TITLE"))
        {
            imageParams.put("title", attachment.getComment());
        }

        RendererAttachment thumb = null;
        if (attachment != null)
        {
            try
            {
                thumb = getThumbnail(attachment, context, embeddedImage);
            }
            catch(RuntimeException re)
            {
                return context.addRenderedContent(RenderUtils.error(re.getMessage()));
            }
        }

        boolean centered = extractCenteredParam(imageParams);
        if (thumb != null)
        {
            return writeImage(thumb.wrapGeneratedElement("<img src=\"" + thumb.getSrc() + "\" " + outputParameters(imageParams) + "/>"), centered);
        }
        else
        {
            // even if the attachment is invalid, we still need to create some markup for it
            return writeImage("<img " + outputParameters(imageParams) + "/>", centered);
        }
    }

    protected String writeImage(String imageTag, boolean centered)
    {
        String result = "";

        if (centered)
        {
            result += "<div align=\"center\">";
        }

        result += imageTag;

        if (centered)
        {
            result += "</div>";
        }

        return result;
    }

    private boolean extractCenteredParam(Map imageParams)
    {
        boolean centered = "center".equalsIgnoreCase((String) imageParams.get("align")) || "centre".equalsIgnoreCase((String) imageParams.get("align"));
        if (centered)
        {
            imageParams.remove("align");
        }
        return centered;
    }

    private String outputParameters(Map params)
    {
        StringBuffer buff = new StringBuffer(20);

        SortedMap sortedParams = new TreeMap(params);

        for (Iterator iterator = sortedParams.keySet().iterator(); iterator.hasNext();)
        {
            String key = (String) iterator.next();
            buff.append(HtmlEscaper.escapeAll(key,true)).append("=\"").
                append(HtmlEscaper.escapeAll((String) sortedParams.get(key),true)).append("\" ");
        }

        if (buff.toString().indexOf("border=") == -1)
        {
            buff.append(" border='0' ");
        }
        return buff.toString();
    }

}
