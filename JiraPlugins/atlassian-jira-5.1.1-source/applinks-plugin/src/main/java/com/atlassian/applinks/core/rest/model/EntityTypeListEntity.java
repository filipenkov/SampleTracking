package com.atlassian.applinks.core.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "entityTypes")
public class EntityTypeListEntity
{
    @XmlElement(name = "entityTypes")
    private List<EntityTypeEntity> entityTypes;

    @SuppressWarnings("unused")
    private EntityTypeListEntity()
    {
    }

    public EntityTypeListEntity(List<EntityTypeEntity> entityTypes)
    {
        this.entityTypes = entityTypes;
    }

    public List<EntityTypeEntity> getTypes()
    {
        return entityTypes;
    }
}
