package com.atlassian.renderer;

import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.GenericLinkParser;

/**
 * Removes the initial ^ from the body of an attachment link, as per the ConfluenceLinkResolver
 */
public class SimpleAttachmentLink extends Link {
    public SimpleAttachmentLink(GenericLinkParser parser) {
        super(parser.getOriginalLinkText());
        title = parser.getLinkTitle();
        linkBody = parser.getLinkBody();
        if (linkBody == null)
        {
            linkBody = parser.getOriginalLinkText();
        }
        linkBody = linkBody.replaceFirst("^\\^", "");
        url = parser.getNotLinkBody();
    }
}
