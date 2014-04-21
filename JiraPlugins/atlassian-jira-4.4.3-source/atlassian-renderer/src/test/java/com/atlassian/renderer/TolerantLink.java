package com.atlassian.renderer;

import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.GenericLinkParser;

import java.text.ParseException;

/**
 * Created by IntelliJ IDEA.
 * User: Tomd
 * Date: 20/12/2005
 * Time: 15:13:21
 * To change this template use File | Settings | File Templates.
 */
public class TolerantLink extends Link
{
    public TolerantLink(GenericLinkParser parser) throws ParseException
    {
        super(parser.getOriginalLinkText());
        title = parser.getLinkTitle();
        linkBody = parser.getLinkBody();
        if (linkBody == null)
        {
            linkBody = parser.getOriginalLinkText();
        }
        url = "/foo/bar";
        if (parser.getSpaceKey() != null && parser.getSpaceKey().startsWith("invalid"))
        {
            throw new ParseException("invalid link",1);
        }
    }

    public boolean isRelativeUrl()
    {
        return false;
    }
}
