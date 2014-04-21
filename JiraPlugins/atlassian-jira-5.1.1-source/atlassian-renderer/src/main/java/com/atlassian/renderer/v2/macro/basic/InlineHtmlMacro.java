package com.atlassian.renderer.v2.macro.basic;

import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.wysiwyg.WysiwygNodeConverter;
import com.atlassian.renderer.wysiwyg.WysiwygConverter;
import com.atlassian.renderer.wysiwyg.Styles;
import com.atlassian.renderer.wysiwyg.ListContext;
import com.opensymphony.util.TextUtils;

import java.util.Map;
import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

/**
 * A macro to include verbatim HTML in a page. <b>DO NOT</b> enable this macro on sites where you do not
 * trust your users absolutely, as it opens you up to a world of pain (and cross-site scripting security
 * issues)
 */
public class InlineHtmlMacro extends BaseMacro implements WysiwygNodeConverter
{
    public boolean isInline()
    {
        return false;
    }

    public boolean hasBody()
    {
        return true;
    }

    public RenderMode getBodyRenderMode()
    {
        return RenderMode.NO_RENDER;
    }

    public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException
    {
        if (renderContext.isRenderingForWysiwyg())
        {
            StringBuffer sb = new StringBuffer();
            sb.append("<div class=\"wikisrc\">\n{html}</div><div wysiwyg=\"macro:html\">").append(body).append("</div><div class=\"wikisrc\">{html}</div>");
            return sb.toString();
        }
        else
        {
            return body;
        }
    }

    public String convertXHtmlToWikiMarkup(Node previousSibling, Node node, WysiwygConverter helper, Styles styles, ListContext listContext, boolean inTable, boolean inListItem, boolean ignoreText)
    {
        return writeNodeListHTML(node.getChildNodes());
    }

    private String writeNodeListHTML(NodeList childNodes)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < childNodes.getLength(); ++i)
        {
            sb.append(writeNodeHTML(childNodes.item(i)));
        }
        return sb.toString();
    }

    private String writeNodeHTML(Node node)
    {
        if (node.getNodeType() == Node.TEXT_NODE)
        {
            return node.getNodeValue();
        }
        else
        {
            StringBuffer sb = new StringBuffer();
            return sb.append("<").append(node.getNodeName()).append(writeNodeAttributes(node)).append(">")
                    .append(writeNodeListHTML(node.getChildNodes()))
                    .append("</").append(node.getNodeName()).append(">").toString();
        }
    }

    private String writeNodeAttributes(Node node)
    {
        StringBuffer sb = new StringBuffer("");
        NamedNodeMap attrs = node.getAttributes();
        // getAttributes() can return null for nodes which are not elements
        if (attrs != null)
        {
            for (int i = 0; i < attrs.getLength(); ++i)
            {
                Node attr = attrs.item(i);
                if (TextUtils.stringSet(attr.getNodeValue()))
                {
                    sb.append(" ").append(attr.getNodeName()).append("=\"").append(attr.getNodeValue()).append("\"");
                }
                else
                {
                    sb.append(" ").append(attr.getNodeName());
                }
            }
        }
        return sb.toString();
    }

    public boolean suppressSurroundingTagDuringWysiwygRendering()
    {
        return true;
    }
}
