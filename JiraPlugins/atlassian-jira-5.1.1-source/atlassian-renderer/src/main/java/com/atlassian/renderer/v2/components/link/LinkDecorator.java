/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Nov 20, 2004
 * Time: 1:52:15 PM
 */
package com.atlassian.renderer.v2.components.link;

import com.atlassian.renderer.links.UrlLink;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.*;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.v2.Renderable;
import com.atlassian.renderer.v2.SubRenderer;

public class LinkDecorator implements Renderable
{
    Link link;

    public LinkDecorator(Link link)
    {
        this.link = link;
    }

    public void render(SubRenderer renderer, RenderContext context, StringBuffer buffer)
    {
        if (link instanceof UrlLink)
            context.addExternalReference(link);

        String renderedLink;

        if ((link instanceof UnresolvedLink || link instanceof UnpermittedLink) && !context.isRenderingForWysiwyg())
            renderedLink = RenderUtils.error(context,"&#91;" + link.getLinkBody() + "&#93;","&#91;" + link.getOriginalLinkText() + "&#93;",true);
        else
            renderedLink = context.getLinkRenderer().renderLink(link, context);

        buffer.append(renderedLink);
    }

}