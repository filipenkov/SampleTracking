package com.atlassian.renderer;

import com.atlassian.renderer.links.*;
import com.atlassian.renderer.util.UrlUtil;
import com.opensymphony.util.TextUtils;

import java.text.ParseException;
import java.util.List;
import java.util.Iterator;

/**
 * Resolves links using simple matches into a small number of Link types
 */
public class SimpleLinkResolver
    implements LinkResolver
{
    public Link createLink(RenderContext context, String linkText) {
        try {
            GenericLinkParser parser = new GenericLinkParser(linkText);

            if (!TextUtils.stringSet(parser.getNotLinkBody()))
                return new UnresolvedLink(linkText);

            if (isUrlLink(parser.getNotLinkBody()) ||
                parser.getNotLinkBody().startsWith("//") ||
                parser.getNotLinkBody().startsWith("\\\\"))
            {
                return new UrlLink(parser);
            }
            if (isInternalAnchorLink(parser.getNotLinkBody()))
            {
                return new SimpleAnchorLink(parser);
            }
            if (isAttachmentLink(parser.getNotLinkBody()))
            {
                return new SimpleAttachmentLink(parser);
            }

            parser.parseAsContentLink();
            return new TolerantLink(parser);
        }
        catch (ParseException e) {
            return new UnresolvedLink(linkText);
        }
    }

    private boolean isAttachmentLink(String linkDestination) {
        return linkDestination.startsWith("^");
    }

    private boolean isInternalAnchorLink(String linkDestination) {
        return linkDestination.startsWith("#");
    }

    public List extractLinks(RenderContext context, String links) {
        return null;
    }

    public List extractLinkTextList(String pageContent) {
        return null;
    }

    public String removeLinkBrackets(String linkText) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private boolean isUrlLink(String textWithoutTitle) {
        for (Iterator it = UrlUtil.URL_PROTOCOLS.iterator(); it.hasNext();) {
            String protocol = (String) it.next();
            if (textWithoutTitle.startsWith(protocol))
                return true;
        }

        return false;
    }

}
