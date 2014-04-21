package com.atlassian.applinks.host.spi;

import com.atlassian.applinks.api.EntityType;
import org.apache.commons.lang.StringUtils;

/**
 * Default implementation of {@link EntityReference} provided as a convenience to facilitate implementation of
 * {@link InternalHostApplication}
 *
 * @since 3.0
 */
public class DefaultEntityReference implements EntityReference
{
    private final String key;
    private final String name;
    private final EntityType type;

    public DefaultEntityReference(final String key, final String name, final EntityType type)
    {
        this.key = key;
        if (StringUtils.isEmpty(name))
        {
            this.name = key;
        }
        else
        {
            this.name = name;
        }
        this.type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultEntityReference that = (DefaultEntityReference) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return type + ":" + key;
    }
}
