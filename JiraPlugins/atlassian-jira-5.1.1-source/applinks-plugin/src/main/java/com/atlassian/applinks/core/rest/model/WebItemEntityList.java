package com.atlassian.applinks.core.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "webItems")
public class WebItemEntityList
{
    @XmlElement(name = "webItem")
    private List<WebItemEntity> items;

    public WebItemEntityList()
    {
    }

    public WebItemEntityList(final List<WebItemEntity> items)
    {
        this.items = items;
    }

    public List<WebItemEntity> getItems()
    {
        return items;
    }
}