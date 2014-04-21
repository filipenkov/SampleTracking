package com.atlassian.applinks.core.rest.model;

import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.core.rest.model.adapter.TypeIdAdapter;
import com.atlassian.applinks.spi.application.TypeId;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "entityType")
public class EntityTypeEntity
{
    @XmlJavaTypeAdapter(TypeIdAdapter.class)
    private TypeId typeId;

    @SuppressWarnings("unused")
    private EntityTypeEntity() {}

    public EntityTypeEntity(final EntityType type)
    {
        this.typeId = TypeId.getTypeId(type);
    }

    public TypeId getTypeId()
    {
        return typeId;
    }

}
