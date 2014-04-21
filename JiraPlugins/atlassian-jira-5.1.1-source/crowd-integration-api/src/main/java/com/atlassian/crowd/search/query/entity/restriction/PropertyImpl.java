package com.atlassian.crowd.search.query.entity.restriction;

import org.apache.commons.lang.builder.ToStringBuilder;

public class PropertyImpl<V> implements Property<V>
{
    private final String propertyName;
    private final Class<V> propertyType;

    public PropertyImpl(final String propertyName, final Class<V> propertyType)
    {
        this.propertyName = propertyName;
        this.propertyType = propertyType;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public Class<V> getPropertyType()
    {
        return propertyType;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Property)) return false;

        Property property = (Property) o;

        if (propertyName != null ? !propertyName.equals(property.getPropertyName()) : property.getPropertyName() != null) return false;
        if (propertyType != null ? !propertyType.equals(property.getPropertyType()) : property.getPropertyType() != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = propertyName != null ? propertyName.hashCode() : 0;
        result = 31 * result + (propertyType != null ? propertyType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("propertyName", propertyName).
                append("propertyType", propertyType).
                toString();
    }
}