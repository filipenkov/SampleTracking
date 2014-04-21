package com.atlassian.crowd.search.query.entity;

/**
 * Property type service.
 *
 * @since 2.2
 */
public interface PropertyTypeService
{
    /**
     * Returns the class type of the property, or <tt>null</tt> if the property type could not be determined.
     *
     * @param name property name
     * @return Class type of the property, or <tt>null</tt> if the property type could not be determined
     */
    Class getType(String name);
}
