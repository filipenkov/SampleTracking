package com.atlassian.renderer.links;

import java.util.List;

/**
 * A link that should not be rendered as a link, either because it
 * <ul>
 *     <li>attempts to point somewhere that doesn't exist
 *     <li>points somewhere the user has no access to
 *     <li>is the result of a parse error
 * </ul>
 */
public class UnresolvedLink extends Link
{
    private Link unresolvedLink;

    public UnresolvedLink(String originalLinkText)
    {
        this(originalLinkText, originalLinkText);
    }

    public UnresolvedLink(String originalLinkText, String linkBody)
    {
        super(originalLinkText);
        this.linkBody = linkBody;
    }

    public UnresolvedLink(String originalLinkText, Link unresolvedLink)
    {
        super(originalLinkText);
        this.unresolvedLink = unresolvedLink;
        this.linkBody = unresolvedLink.getLinkBody();
    }
    public boolean isRelativeUrl()
    {
        return false;
    }

    public String getTitle()
    {
        if (unresolvedLink == null)
        {
            return getOriginalLinkText();
        }
        return unresolvedLink.getTitle();
    }

    public String getTitleKey()
    {
        return (unresolvedLink == null) ? null : unresolvedLink.getTitleKey();
    }

    public List getTitleArgs()
    {
        return (unresolvedLink == null) ? null : unresolvedLink.getTitleArgs();
    }

    public String getUrl()
    {
        return "";
    }
}
