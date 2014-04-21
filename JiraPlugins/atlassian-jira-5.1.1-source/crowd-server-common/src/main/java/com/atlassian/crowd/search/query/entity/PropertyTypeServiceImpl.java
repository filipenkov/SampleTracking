package com.atlassian.crowd.search.query.entity;

import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Implements a PropertyTypeServiceImpl.
 *
 * @since 2.2
 */
public class PropertyTypeServiceImpl implements PropertyTypeService
{
    private final Map<String, Class> propertyTypeMap;

    /**
     * Constructs a new PropertyTypeService.
     *
     * @param propertyTypeMap property-type map
     */
    public PropertyTypeServiceImpl(final Map<String, Class> propertyTypeMap)
    {
        this.propertyTypeMap = ImmutableMap.copyOf(propertyTypeMap);
    }

    public Class getType(final String name)
    {
        return propertyTypeMap.get(name);
    }

    /**
     * Creates a new instance of <tt>PropertyTypeServiceImpl</tt> from a collection of <tt>Property<tt>.
     *
     * @param properties list of Property
     * @return new instance of <tt>PropertyTypeServiceImpl</tt>
     */
    public static PropertyTypeServiceImpl newInstance(Iterable<Property<?>> properties)
    {
        Map<String, Class> propertyTypeMap = Maps.newHashMap();
        for (Property property : properties)
        {
            propertyTypeMap.put(property.getPropertyName(), property.getPropertyType());
        }
        return new PropertyTypeServiceImpl(propertyTypeMap);
    }
}
