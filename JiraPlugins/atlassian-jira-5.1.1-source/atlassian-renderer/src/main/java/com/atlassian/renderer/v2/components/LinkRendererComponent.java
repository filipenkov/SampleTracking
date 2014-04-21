/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 28, 2004
 * Time: 1:34:28 PM
 */
package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.LinkResolver;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.components.link.LinkDecorator;
import com.opensymphony.util.TextUtils;

public class LinkRendererComponent implements RendererComponent
{
    private LinkResolver linkResolver;
    public static final char START_LINK_CHAR = '[';
    private static final char ESCAPE_CHAR = '\\';
    private static final char END_LINK_CHAR = ']';
    private static final char NEW_LINE_CHAR = '\n';

    public LinkRendererComponent(LinkResolver linkResolver)
    {
        this.linkResolver = linkResolver;
    }

    public boolean shouldRender(RenderMode renderMode)
    {
        return renderMode.renderLinks();
    }

    public String render(String wiki, RenderContext context)
    {
        if (wiki == null || wiki.length() < 3)
        {
            return wiki;
        }

        StringBuffer result = new StringBuffer(wiki.length());

        char[] wikiChars = wiki.toCharArray();

        boolean inLink = false;
        StringBuffer linkText = new StringBuffer(20);
        char prev = 0;

        for (int i = 0; i < wikiChars.length; i++)
        {
            char c = wikiChars[i];
            if (START_LINK_CHAR == c)
            {
                if (inLink)
                {
                    if (prev == ESCAPE_CHAR) // if char was escaped, behave normally
                    {
                        linkText.append(c);
                    }
                    else
                    {
                        result.append(linkText);
                        linkText = new StringBuffer(20);
                        linkText.append(c);
                    }
                }
                else if (prev == ESCAPE_CHAR)
                {
                    result.append(c);
                }
                else
                {
                    inLink = true;
                    linkText.append(c);
                }
            }
            else if (END_LINK_CHAR == c && inLink)
            {
                if (prev == ESCAPE_CHAR)
                {
                    linkText.append(c);
                }
                else
                {
                    inLink = false;

                    if (linkText.length() == 1) // ie link is []
                    {
                        result.append(linkText);
                        result.append(c);
                    }
                    else
                    {
                        String linkBody = linkText.substring(1);

                        appendLink(result, context, linkBody);
                    }

                    linkText = new StringBuffer(20);
                }
            }
            else if (Character.isWhitespace(c) && START_LINK_CHAR == prev)
            {
                inLink = false;
                result.append(linkText);
                result.append(c);
                linkText = new StringBuffer(20);
            }
            else if (NEW_LINE_CHAR == c && inLink)
            {
                inLink = false;
                result.append(linkText);
                result.append(c);
                linkText = new StringBuffer(20);
            }
            else if (!inLink)
            {
                result.append(c);
            }
            else
            {
                linkText.append(c);
            }

            prev = c;
        }

        if (linkText.length() > 0)
        {
            result.append(linkText);
        }

        return result.toString();
    }

    private void appendLink(StringBuffer stringBuffer, RenderContext context, String linkText)
    {
        Link link = linkResolver.createLink(context, linkText);
        stringBuffer.append(context.getRenderedContentStore().addInline(new LinkDecorator(link)));
    }
}