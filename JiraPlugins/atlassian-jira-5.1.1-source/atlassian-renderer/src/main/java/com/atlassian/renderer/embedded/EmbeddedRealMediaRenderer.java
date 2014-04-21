package com.atlassian.renderer.embedded;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.attachments.RendererAttachmentManager;
import com.atlassian.renderer.v2.components.HtmlEscaper;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Higgs
 * Date: 23/09/2005
 * Time: 17:05:07
 */

public class EmbeddedRealMediaRenderer extends EmbeddedObjectRenderer
{
    static public String DEFAULT_WIDTH = "320";
    static public String DEFAULT_HEIGHT = "240";

    static public String DEFAULT_CONTROLS_HEIGHT = "30";

    public EmbeddedRealMediaRenderer(RendererAttachmentManager attachmentManager)
    {
        super(attachmentManager);
    }

    public String renderResource(EmbeddedResource resource, RenderContext context)
    {
        Map contextMap = setupObjectProperties(resource, context);

        String origHeight = (String) contextMap.get("height");
        String origWidth = (String) contextMap.get("width");

        // The first time we render it, we display the actual media
        contextMap.put("controls", "imagewindow");
        contextMap.put("console", "video");

        if (origWidth == null)
            contextMap.put("width", DEFAULT_WIDTH);
        if (origHeight == null)
            contextMap.put("height", DEFAULT_HEIGHT);

        String videoContent = renderEmbeddedObject(contextMap);

        // The second time around, display the controls
        contextMap.put("controls", "ControlPanel");
        contextMap.put("console", "video");
        if (origWidth == null)
            contextMap.put("width", DEFAULT_WIDTH);
        if (origHeight == null)
            contextMap.put("height", DEFAULT_CONTROLS_HEIGHT);

        String playerContent = renderEmbeddedObject(contextMap);

        return renderRealMediaWrapper(videoContent, playerContent, contextMap);
    }

    private String renderRealMediaWrapper(String videoContent, String playerContent, Map contextMap)
    {
        StringBuffer sb = new StringBuffer();
        String classString = "embeddedObject";
        if(contextMap.containsKey("id"))
        {
            classString = classString + "-" + HtmlEscaper.escapeAll((String)contextMap.get("id"), true);
        }
        sb.append("<div class='");
        sb.append(classString);
        sb.append("'>");
        sb.append("<table border='0' cellpadding='0'>\n<tr>\n<td>\n");
        sb.append(videoContent);
        sb.append("</td>\n</tr>\n<tr>\n<td>");
        sb.append(playerContent);
        sb.append("</td>\n</tr>\n</table>\n</div>");
        return sb.toString();
    }
}
