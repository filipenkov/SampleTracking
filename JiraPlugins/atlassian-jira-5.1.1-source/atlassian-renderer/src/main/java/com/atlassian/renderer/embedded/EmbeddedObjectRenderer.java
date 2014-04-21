package com.atlassian.renderer.embedded;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.attachments.RendererAttachment;
import com.atlassian.renderer.attachments.RendererAttachmentManager;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.v2.components.HtmlEscaper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 22/09/2005
 * Time: 12:35:13
 */

public class EmbeddedObjectRenderer implements EmbeddedResourceRenderer
{

    protected RendererAttachmentManager attachmentManager;

    // List of valid <object> parameters
    static protected List validObjectTags;

    // Compiled from http://www.htmlref.com/reference/appa/tag_object.htm
    static
    {
        validObjectTags = new ArrayList();

        validObjectTags.add("align");
        validObjectTags.add("archive");
        validObjectTags.add("border");
        validObjectTags.add("class");
        validObjectTags.add("classid");
        validObjectTags.add("codebase");
        validObjectTags.add("codetype");
        validObjectTags.add("data");
        validObjectTags.add("declare");
        validObjectTags.add("dir");
        validObjectTags.add("height");
        validObjectTags.add("hspace");
        validObjectTags.add("id");
        validObjectTags.add("lang");
        validObjectTags.add("name");
        validObjectTags.add("standby");
        validObjectTags.add("style");
        validObjectTags.add("tabindex");
        validObjectTags.add("title");
        validObjectTags.add("type");
        validObjectTags.add("usemap");
        validObjectTags.add("vspace");
        validObjectTags.add("width");
    }

    // List of valid <embed> tags
    static protected List validEmbedTags;

    static
    {
        validEmbedTags = new ArrayList();

        validEmbedTags.add("align");
        validEmbedTags.add("autostart");
        validEmbedTags.add("bgcolor");
        validEmbedTags.add("controller");
        validEmbedTags.add("controls"); // RM
        validEmbedTags.add("console"); // RM
        validEmbedTags.add("class");
        validEmbedTags.add("height");
        validEmbedTags.add("href");
        validEmbedTags.add("id");
        validEmbedTags.add("name");
        validEmbedTags.add("pluginspage");
        validEmbedTags.add("pluginurl");
        validEmbedTags.add("quality");
        validEmbedTags.add("showcontrols"); // WMP
        validEmbedTags.add("showtracker"); // WMP
        validEmbedTags.add("showdisplay"); // WMP
        validEmbedTags.add("src");
        validEmbedTags.add("target");
        validEmbedTags.add("type");
        validEmbedTags.add("width");
    }

    // List of valid <param> tags
    static protected List validParamTags;

    static
    {
        validParamTags = new ArrayList();

        validParamTags.add("animationatStart"); // WMP
        validParamTags.add("autoStart");
        validParamTags.add("controller");
        validParamTags.add("controls"); // RM
        validParamTags.add("console"); // RM
        validParamTags.add("data");
        validParamTags.add("fileName"); // WMP
        validParamTags.add("href");
        validParamTags.add("loop");
        validParamTags.add("menu");
        validParamTags.add("movie");
        validParamTags.add("quality");
        validParamTags.add("scale");
        validParamTags.add("showControls");
        validParamTags.add("src");
        validParamTags.add("target");
        validParamTags.add("transparentatStart"); // WMP
        validParamTags.add("type");
    }

    public EmbeddedObjectRenderer(RendererAttachmentManager attachmentManager)
    {
        this.attachmentManager = attachmentManager;
    }

    /**
     * Retrieves the attachment from an EmbeddedResource, resolving the absolute URL to the attachment and placing it into a ContextMap for use with Velocity.
     *
     * @param resource the EmbeddedResource object
     * @param context the RenderContext
     * @return ContextMap containing the embedded resource properties and URL
     */
    protected Map setupObjectProperties(EmbeddedResource resource, RenderContext context)
    {
        EmbeddedObject emObject = (EmbeddedObject) resource;

        RendererAttachment attachment = null;

        // Currently we refuse to display objects hosted elsewhere, because of XSS exploits
        attachment = attachmentManager.getAttachment(context, emObject);

        // If we couldn't find the attachment, or the user isn't allowed to view it, throw file not found
        if (attachment == null)
            throw new IllegalArgumentException("Unable to render embedded object: File (" + emObject.getFilename() + ") not found.");

        Map objectParams = new HashMap();
        objectParams.putAll(emObject.getProperties());
        objectParams.put("type", resource.getType());

        // Put together the URL for the object
        String objectUrl = attachment.getSrc();

        objectParams.put("object", objectUrl);

        // For parity between the two implementations, push the object across all tags
        objectParams.put("src", objectUrl);
        objectParams.put("data", objectUrl);

        return objectParams;
    }

    public String renderResource(EmbeddedResource resource, RenderContext context)
    {
        // Setup the properties and fetch the object URL
        Map contextMap = null;
        try
        {
            contextMap = setupObjectProperties(resource, context);
        }
        catch(RuntimeException re)
        {
            return context.addRenderedContent(RenderUtils.error(re.getMessage()));
        }

        String renderedObjectHtml = renderEmbeddedObject(contextMap);

        return renderEmbeddedObjectWrapper(renderedObjectHtml, contextMap);
    }

    protected String renderEmbeddedObjectWrapper(String renderedObjectHtml, Map contextMap)
    {
        StringBuffer sb = new StringBuffer();
        String classString = "embeddedObject";
        if(contextMap.containsKey("id"))
        {
            classString = classString + "-" + HtmlEscaper.escapeAll((String)contextMap.get("id"), true);
        }
        sb.append("<div class=\"");
        sb.append(classString);
        sb.append("\">");
        sb.append(renderedObjectHtml);
        sb.append("</div>");
        return sb.toString();
    }

    protected String renderEmbeddedObject(Map contextMap)
    {
        StringBuffer sb = new StringBuffer("<object ");
        for (Iterator iterator = validObjectTags.iterator(); iterator.hasNext();)
        {
            String tag = (String) iterator.next();
            if(contextMap.containsKey(tag))
            {
                sb.append(tag);
                sb.append("=\"");
                sb.append(HtmlEscaper.escapeAll((String) contextMap.get(tag), false));
                sb.append("\" ");
            }
        }
        sb.append(">");

        for (Iterator iterator = validParamTags.iterator(); iterator.hasNext();)
        {
            String paramTag = (String) iterator.next();
            if(contextMap.containsKey(paramTag))
            {
                sb.append("<param name=\"");
                sb.append(paramTag);
                sb.append("\" value=\"");
                sb.append(HtmlEscaper.escapeAll((String) contextMap.get(paramTag), false));
                sb.append("\"/>");
            }
        }

        sb.append("<embed ");
        for (Iterator iterator = validEmbedTags.iterator(); iterator.hasNext();)
        {
            String embedTag = (String) iterator.next();
            if(contextMap.containsKey(embedTag))
            {
                sb.append(embedTag);
                sb.append("=\"");
                sb.append(HtmlEscaper.escapeAll((String) contextMap.get(embedTag), false));
                sb.append("\" ");
            }
        }
        sb.append("/>");
        sb.append("</object>");
        return sb.toString();
    }
}
