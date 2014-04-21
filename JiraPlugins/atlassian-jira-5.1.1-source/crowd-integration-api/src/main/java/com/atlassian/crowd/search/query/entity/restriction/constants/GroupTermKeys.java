package com.atlassian.crowd.search.query.entity.restriction.constants;

import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyImpl;
import com.google.common.collect.ImmutableSet;

import java.util.Date;

/**
 * Represents attributes of a group.
 */
public class GroupTermKeys
{
    public static final Property<String> NAME = new PropertyImpl<String>("name", String.class);
    public static final Property<Boolean> ACTIVE = new PropertyImpl<Boolean>("active", Boolean.class);

    public static final Property<Date> CREATED_DATE = new PropertyImpl<Date>("createdDate", Date.class);
    public static final Property<Date> UPDATED_DATE = new PropertyImpl<Date>("updatedDate", Date.class);

    public static final Property<Boolean> LOCAL = new PropertyImpl<Boolean>("local", Boolean.class);

    public static final Iterable<Property<?>> ALL_GROUP_PROPERTIES;


    static
    {
        ALL_GROUP_PROPERTIES = ImmutableSet.<Property<?>>of(
                NAME, ACTIVE, CREATED_DATE, UPDATED_DATE, LOCAL);
    }

    private GroupTermKeys()
    {
    }
}
