/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 28, 2004
 * Time: 7:59:10 PM
 */
package com.atlassian.renderer.v2;

import com.atlassian.renderer.links.Link;

public class MockLink extends Link
{
    public MockLink(String originalLinkText)
    {
        super(originalLinkText);
    }

    public void setLinkBody(String linkBody)
    {
        this.linkBody = linkBody;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

}