package com.atlassian.applinks.core.rest.model;

import com.atlassian.applinks.core.rest.model.adapter.OptionalURIAdapter;
import com.atlassian.applinks.core.rest.model.adapter.TypeIdAdapter;
import com.atlassian.applinks.host.spi.DefaultEntityReference;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.spi.application.TypeId;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "entities")
public class ReferenceEntityList
{
    private final List<ReferenceEntity> entity = new ArrayList<ReferenceEntity>();

    @SuppressWarnings("unused")
    private ReferenceEntityList()
    {
    }

    public ReferenceEntityList(final Iterable<EntityReference> entities)
    {
        Iterables.addAll(entity, Iterables.transform(entities, new Function<EntityReference, ReferenceEntity>()
        {
            public ReferenceEntity apply(final EntityReference from)
            {
                return new ReferenceEntity(
                        from.getKey(),
                        from.getName(),
                        TypeId.getTypeId(from.getType()),
                        from.getType().getIconUrl());
            }
        }));
    }

    public Iterable<EntityReference> getEntities(final InternalTypeAccessor typeAccessor)
    {
        return Iterables.transform(entity, new Function<ReferenceEntity, EntityReference>()
        {
            public EntityReference apply(final ReferenceEntity from)
            {
                return new DefaultEntityReference(from.key, from.name, typeAccessor.loadEntityType(from.typeId.get()));
            }
        });
    }

    public static class ReferenceEntity
    {
        @XmlAttribute
        private String key;
        @XmlAttribute
        private String name;
        @XmlAttribute
        @XmlJavaTypeAdapter(TypeIdAdapter.class)
        private TypeId typeId;
        @XmlAttribute
        @XmlJavaTypeAdapter(OptionalURIAdapter.class)
        @SuppressWarnings("unused") // used in the UI dropdowns
        private URI iconUrl;

        @SuppressWarnings("unused")
        private ReferenceEntity()
        {
        }

        private ReferenceEntity(final String key, final String name, final TypeId typeId,
                                final URI iconUrl)
        {
            this.key = key;
            this.name = name;
            this.typeId = typeId;
            this.iconUrl = iconUrl;
        }
    }

}