package com.atlassian.applinks.core.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "entities")
public class RestEntityLinkList
{
    @XmlElement(name = "entity")
    private List<EntityLinkEntity> entities;

    public RestEntityLinkList()
    {
    }

    public RestEntityLinkList(final List<EntityLinkEntity> entities)
    {
        this.entities = entities;
    }

    public List<EntityLinkEntity> getEntities()
    {
        return entities;
    }
}