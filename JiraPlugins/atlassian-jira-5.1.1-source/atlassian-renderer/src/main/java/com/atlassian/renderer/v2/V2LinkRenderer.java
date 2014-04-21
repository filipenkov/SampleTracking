package com.atlassian.renderer.v2;

import com.atlassian.renderer.links.UrlLink;
import com.atlassian.renderer.*;
import com.atlassian.renderer.links.*;
import com.atlassian.renderer.util.UrlUtil;
import com.atlassian.renderer.wysiwyg.WysiwygLinkHelper;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

public class V2LinkRenderer implements LinkRenderer
{
    SubRenderer subRenderer;
    IconManager iconManager;
    RendererConfiguration rendererConfiguration;

    public V2LinkRenderer() {}
    
    public V2LinkRenderer(SubRenderer subRenderer, IconManager iconManager, RendererConfiguration rendererConfiguration)
    {
        this.subRenderer = subRenderer;
        this.iconManager = iconManager;
        this.rendererConfiguration = rendererConfiguration;
    }

    public String renderLink(Link link, RenderContext renderContext)
    {
        StringBuffer buffer = new StringBuffer();
        if ((link instanceof UnresolvedLink || link instanceof UnpermittedLink) && !renderContext.isRenderingForWysiwyg())
        {
            buffer.append("<strike>").append(link.getLinkBody()).append("</strike>");
            return buffer.toString();
        }

        Icon icon = iconManager.getLinkDecoration(link.getIconName());

        if (icon.position != 0)
        {
            buffer.append("<span class=\"nobr\">");
        }
        buffer.append("<a href=\"");
        if (link.isRelativeUrl() && renderContext.getSiteRoot() != null)
        {
            buffer.append(renderContext.getSiteRoot());
        }
        buffer.append(unescapeEscapeSequences(UrlUtil.escapeSpecialCharacters(link.getUrl())));
        buffer.append("\"");

        // @HACK
        // The newline before the title parameter below fixes CONF-4562. I have absolutely no idea HOW it fixes
        // CONF-4562, but the simple fact that it does fix the problem indicates that I could spend my whole life
        // trying to work out why and be none the wiser. I suggest you don't think too hard about it either, and
        // instead contemplate the many joys that can be found in life -- the sunlight reflecting off Sydney
        // Harbour; walking through the Blue Mountains on a dew-laden Autumn morning; the love of a beautiful
        // woman -- this should in some way distract you from the insane ugliness of the code I am about to check
        // in.
        //
        // Oh, and whatever you do, don't remove the damn newline.
        //
        // -- Charles, November 09, 2005
        if (renderContext.isRenderingForWysiwyg())
            buffer.append("\n");

        if (StringUtils.isNotEmpty(link.getTitle()))
        {
            // Encode only the link title here, to avoid issues where quotes are included (CONF-4544)
            buffer.append(" title=\"" + TextUtils.htmlEncode(getLinkTitle(link)) + "\"");
        }

        // This allows a link type to append any parameters within the anchor tag that it needs to.
        buffer.append(link.getLinkAttributes());

        if (link instanceof UrlLink && rendererConfiguration.isNofollowExternalLinks())
        {
            buffer.append(" rel=\"nofollow\"");
        }
        if (renderContext.isRenderingForWysiwyg())
        {
            buffer.append(WysiwygLinkHelper.getLinkInfoAttributes(link));
        }
        buffer.append(">");
        if (icon.position == Icon.ICON_LEFT)
        {
            buffer.append(icon.toHtml(renderContext.getImagePath()));
        }

        //prevent emoticon rendering
        if (link.getLinkBody().equals(link.getUrl()) || link.getLinkBody().equals(link.getOriginalLinkText()))
        {
            buffer.append(subRenderer.render(link.getLinkBody(), renderContext, RenderMode.allow(RenderMode.F_HTMLESCAPE | RenderMode.F_BACKSLASH_ESCAPE)));
        }
        else
        {
            buffer.append(subRenderer.render(link.getLinkBody(), renderContext, RenderMode.PHRASES_IMAGES));
        }

        if (icon.position == Icon.ICON_RIGHT)
        {
            buffer.append(icon.toHtml(renderContext.getImagePath()));
        }

        buffer.append("</a>");

        if (icon.position != 0)
        {
            buffer.append("</span>");
        }
        if (renderContext.isRenderingForWysiwyg())
        {
            buffer.append("&#8201;");
        }

        return buffer.toString();
    }

    // Extracted method - override this to support i18n
    protected String getLinkTitle(Link link)
    {
        return link.getTitle();
    }

    /**
     * This method is used to unescape escape sequences within URLs.
     * <p/>
     * For example if the url is http://example.com/foo?path=\[path\] - we make it http://example.com/foo?path=[path]
     */
    private String unescapeEscapeSequences(String s)
    {
        if (s == null)
        {
            return "";
        }
        StringBuffer result = new StringBuffer(s.length());
        char[] chars = s.toCharArray();
        char prev = 0;

        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            if (c != '\\' || prev == '\\')
            {
                result.append(c);
            }

            prev = c;
        }

        return result.toString();
    }

    public void setSubRenderer(SubRenderer subRenderer)
    {
        this.subRenderer = subRenderer;
    }

    public void setIconManager(IconManager iconManager)
    {
        this.iconManager = iconManager;
    }

    public void setRendererConfiguration(RendererConfiguration rendererConfiguration)
    {
        this.rendererConfiguration = rendererConfiguration;
    }
}
