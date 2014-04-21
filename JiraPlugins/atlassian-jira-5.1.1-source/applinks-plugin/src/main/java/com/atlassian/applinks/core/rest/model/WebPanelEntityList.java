package com.atlassian.applinks.core.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "webPanels")
public class WebPanelEntityList
{
    @XmlElement(name = "webPanel")
    private List<WebPanelEntity> panels;

    public WebPanelEntityList()
    {
    }

    public WebPanelEntityList(final List<WebPanelEntity> panels)
    {
        this.panels = panels;
    }

    public List<WebPanelEntity> getWebPanels()
    {
        return panels;
    }
}