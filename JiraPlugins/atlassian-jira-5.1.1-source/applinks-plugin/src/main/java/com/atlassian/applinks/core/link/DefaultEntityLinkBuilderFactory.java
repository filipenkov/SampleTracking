package com.atlassian.applinks.core.link;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.core.property.PropertyService;
import com.atlassian.applinks.spi.link.EntityLinkBuilderFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultEntityLinkBuilderFactory implements EntityLinkBuilderFactory
{
    private final PropertyService propertyService;

    public DefaultEntityLinkBuilderFactory(final PropertyService propertyService)
    {
        this.propertyService = propertyService;
    }

    public EntityLinkBuilder builder()
    {
        return new DefaultEntityLinkBuilder();
    }

    public class DefaultEntityLinkBuilder implements EntityLinkBuilder
    {
        private ApplicationLink applicationLink;
        private EntityType type;
        private String key;
        private String name;
        private boolean primary = false;

        public EntityLinkBuilder key(final String key)
        {
            this.key = key;
            return this;
        }

        public EntityLinkBuilder type(final EntityType type)
        {
            this.type = type;
            return this;
        }

        public EntityLinkBuilder applicationLink(final ApplicationLink applicationLink)
        {
            this.applicationLink = applicationLink;
            return this;
        }

        public EntityLinkBuilder primary(final boolean primary)
        {
            this.primary = primary;
            return this;
        }

        public EntityLinkBuilder name(final String name)
        {
            this.name = name;
            return this;
        }

        public EntityLink build()
        {
            // be nice - some entities keys will be equivalent to names
            if (name == null)
            {
                name = key;
            }

            return new DefaultEntityLink(checkNotNull(key, "key"), checkNotNull(type, "type"),
                    checkNotNull(name, "name"), type.getDisplayUrl(checkNotNull(applicationLink), key),
                    checkNotNull(applicationLink, "applicationLink"),
                    propertyService, primary);
        }
    }

}
