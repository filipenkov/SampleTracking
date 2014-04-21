package com.atlassian.crowd.search.query.entity.restriction;

/**
 * Property Values are accessible via {@link com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys},
 * {@link com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys}
 */
public interface Property<V>
{
    /**
     * Returns the name of the property.
     *
     * @return property name
     */
    String getPropertyName();

    /**
     * Returns the type of the property value.
     * 
     * @return class type of the property value
     */
    Class<V> getPropertyType();
}
