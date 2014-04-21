package com.atlassian.applinks.core.link;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.core.property.PropertyService;

import java.net.URI;

import static com.atlassian.applinks.core.util.URIUtil.copyOf;

public class DefaultEntityLink implements EntityLink
{
    private final PropertyService propertyService;
    private final URI displayUrl;
    private final String key;
    private final EntityType type;
    private final ApplicationLink applicationLink;
    private final boolean primary;
    private final String name;

    DefaultEntityLink(final String key, final EntityType type, final String name, final URI displayUrl,
                             final ApplicationLink applicationLink,
                             final PropertyService propertyService, final boolean isPrimary)
    {
        this.name = name;
        this.propertyService = propertyService;
        this.displayUrl = displayUrl;
        this.key = key;
        this.type = type;
        this.applicationLink = applicationLink;
        this.primary = isPrimary;
    }

    public URI getDisplayUrl()
    {
        return copyOf(displayUrl);
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public EntityType getType()
    {
        return type;
    }

    public ApplicationLink getApplicationLink()
    {
        return applicationLink;
    }

    public Object getProperty(final String key)
    {
        return propertyService.getProperties(this).getProperty(key);
    }

    public Object putProperty(final String key, final Object value)
    {
        return propertyService.getProperties(this).putProperty(key, value);
    }

    public Object removeProperty(final String key)
    {
        return propertyService.getProperties(this).removeProperty(key);
    }

    public boolean isPrimary()
    {
        return primary;
    }

    @Override
    public String toString()
    {
        return String.format("%s - %s (%s)", getType().getClass().getSimpleName(), getKey(), getApplicationLink().getId());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        DefaultEntityLink that = (DefaultEntityLink) o;

        if (!applicationLink.equals(that.applicationLink))
        {
            return false;
        }
        if (!key.equals(that.key))
        {
            return false;
        }
        if (!type.equals(that.type))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = key.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + applicationLink.hashCode();
        return result;
    }
}
