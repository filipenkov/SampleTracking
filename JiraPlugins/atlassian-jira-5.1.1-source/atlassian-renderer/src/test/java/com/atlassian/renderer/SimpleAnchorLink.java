package com.atlassian.renderer;

import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.GenericLinkParser;

import java.text.ParseException;

/**
 * Removes the initial # from the body of an anchor link, as per the ConfluenceLinkResolver
 */
public class SimpleAnchorLink extends Link
{
    public SimpleAnchorLink(GenericLinkParser parser) throws ParseException
    {
        super(parser.getOriginalLinkText());
        title = parser.getLinkTitle();
        linkBody = parser.getLinkBody();
        if (linkBody == null)
        {
            linkBody = parser.getOriginalLinkText();
        }
        linkBody = linkBody.replaceFirst("^#", "");
        url = parser.getNotLinkBody();
    }
}
