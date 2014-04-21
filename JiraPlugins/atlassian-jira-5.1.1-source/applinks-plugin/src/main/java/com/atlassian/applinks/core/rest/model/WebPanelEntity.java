package com.atlassian.applinks.core.rest.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "webPanel")
public class WebPanelEntity
{
    private String html;

    public WebPanelEntity()
    {
    }

    public WebPanelEntity(final String html)
    {
        this.html = html;
    }

    public String getHtml()
    {
        return html;
    }
}
